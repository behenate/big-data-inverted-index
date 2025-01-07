package org.indexer;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.crawler.Book;
import org.crawler.MongoConnection;
import org.indexer.model.BookInfo;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Indexes.ascending;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * An indexer that uses a local data source to fetch books from
 */
abstract public class LocalIndexer extends BaseIndexer {
    protected final MongoDatabase database;

    // For running in non-docker see comment in MongoConnection
    static final String DATABASE_PATH = MongoConnection.DEFAULT_DATABASE_PATH;

    public LocalIndexer() {
        ConnectionString connectionString = new ConnectionString(DATABASE_PATH);
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register(BookInfo.class).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(pojoCodecRegistry)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        this.database = mongoClient.getDatabase("big_data");
    }

    private void setupDatabaseIndex() {
        Bson index = ascending("name");
        IndexOptions indexOptions = new IndexOptions();
        indexOptions.unique(true);
        MongoCollection<Document> books = this.database.getCollection("inverted_index");
        books.createIndex(index, indexOptions);
    }

    public void fetchBooks() {
        MongoCursor<Document> cursor = this.database.getCollection("books").find().iterator();

        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Book currBook = new Book(document);
                processBookText(currBook);
            }
        } finally {
            cursor.close();
        }
    }

    abstract void save();

    public void indexBooks() {
        fetchBooks();
        save();
    }
}
