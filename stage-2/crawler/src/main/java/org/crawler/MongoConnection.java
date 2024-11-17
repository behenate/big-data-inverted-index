package org.crawler;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Indexes.ascending;

import java.util.List;

public class MongoConnection {
  public static final String DEFAULT_DATABASE_PATH = "mongodb://localhost:27017";
  public static final String BOOKS_DOCUMENT_NAME = "books";
  private final MongoDatabase database;

  public MongoConnection(String databasePath) {
    String path = databasePath == null ? DEFAULT_DATABASE_PATH : databasePath;

    ConnectionString connectionString = new ConnectionString(path);
    MongoClient mongoClient = MongoClients.create(connectionString);
    this.database = mongoClient.getDatabase("big_data");
    setupDatabaseIndex();
  }

  private void setupDatabaseIndex() {
    Bson index = ascending("id");
    IndexOptions indexOptions = new IndexOptions();
    indexOptions.unique(true);
    MongoCollection<Document> books = this.database.getCollection(BOOKS_DOCUMENT_NAME);
    books.createIndex(index, indexOptions);
  }

  public void saveBook(Book book) {
    MongoCollection<Document> booksCollection = this.database.getCollection(BOOKS_DOCUMENT_NAME);
    booksCollection.insertOne(book.toMongoDocument());
  }

  public void saveBooks(List<Book> books) {
    List<Document> documents = books.stream().map(Book::toMongoDocument).toList();
    MongoCollection<Document> booksCollection = this.database.getCollection(BOOKS_DOCUMENT_NAME);
    booksCollection.insertMany(documents);
  }
}
