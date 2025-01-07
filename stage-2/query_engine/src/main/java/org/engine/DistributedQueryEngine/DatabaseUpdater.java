package org.engine.DistributedQueryEngine;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.crawler.MongoConnection;
import org.engine.model.BookInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Indexes.ascending;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DatabaseUpdater {
  private final String uuid = UUID.randomUUID().toString();
  private final DatabaseUpdateReceiver receiver;
  private final MongoDatabase database;
  MongoCollection<Document> collection;

  // For running in non-docker see comment in MongoConnection
  static final String DATABASE_PATH = MongoConnection.DEFAULT_DATABASE_PATH;

  public DatabaseUpdater(String host) throws IOException, TimeoutException {
    this.receiver = new DatabaseUpdateReceiver(host, (update -> {
      appendUpdate(update);
      System.out.println(uuid + " processed an update of size " + update.size());
    }));
    ConnectionString connectionString = new ConnectionString(DATABASE_PATH);
    CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(BookInfo.class).build();
    CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    MongoClientSettings settings = MongoClientSettings.builder()
        .applyConnectionString(connectionString)
        .codecRegistry(pojoCodecRegistry)
        .applyToConnectionPoolSettings(builder ->
            builder.maxSize(1000)  // Adjust based on your needs
                .minSize(20)
                .maxWaitTime(1000, TimeUnit.MILLISECONDS))
        .build();
    MongoClient mongoClient = MongoClients.create(settings);
    this.database = mongoClient.getDatabase("big_data");
    this.collection = this.database.getCollection("inverted_index").withWriteConcern(WriteConcern.UNACKNOWLEDGED);
    setupDatabaseIndex();
  }

  private void setupDatabaseIndex() {
    Bson index = ascending("word");
    Bson booksIdIndex = ascending("books.bookId");
    IndexOptions indexOptions = new IndexOptions();
    MongoCollection<Document> invertedIndex = this.database.getCollection("inverted_index");
    invertedIndex.createIndex(index, indexOptions);
    invertedIndex.createIndex(booksIdIndex, indexOptions);
  }


  void appendUpdate(Map<String, Map<Integer, BookInfo>> update) {
    List<WriteModel<Document>> bulkUpdates = new ArrayList<>(update.size() * 2);

    for (Map.Entry<String, Map<Integer, BookInfo>> wordEntry : update.entrySet()) {
      String word = wordEntry.getKey();
      Map<Integer, BookInfo> books = wordEntry.getValue();

      Document booksDocument = new Document();
      for (Map.Entry<Integer, BookInfo> bookEntry : books.entrySet()) {
        Integer bookId = bookEntry.getKey();
        BookInfo bookInfo = bookEntry.getValue();
        Document bookInfoDocument = new Document()
            .append("positions", bookInfo.positions())
            .append("frequency", bookInfo.frequency());
        booksDocument.append("books." + bookId, bookInfoDocument);
      }

      Document filter = new Document("word", word);
      Document update2 = new Document("$set", booksDocument);
      bulkUpdates.add(new UpdateOneModel<>(filter, update2, new UpdateOptions().upsert(true)));
    }

    executeBulkWrite(collection, bulkUpdates);
    bulkUpdates.clear();
  }

  private void executeBulkWrite(MongoCollection<Document> collection, List<WriteModel<Document>> bulkUpdates) {
    try {
      BulkWriteOptions options = new BulkWriteOptions().ordered(false);
      collection.bulkWrite(bulkUpdates, options);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
