package org.crawler;

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
  // This is a setup for the docker connection. For local database just use "mongodb://localhost:27017" as DEFAULT_DATABASE_PATH
  public static final String USERNAME = "root";
  public static final String PASSWORD = "123456";
  public static final String HOST = "mongo_db";
  public static final String PORT = "27017";
  public static final String DATABASE_NAME = "big_data";
  public static final String DEFAULT_DATABASE_PATH = "mongodb://" + USERNAME + ":" + PASSWORD + "@" + HOST + ":" + PORT + "/" + DATABASE_NAME + "?authSource=admin";
  public static final String BOOKS_DOCUMENT_NAME = "books";
  private final MongoDatabase database;

  public MongoConnection(String databasePath) {
    String path = databasePath == null ? DEFAULT_DATABASE_PATH : databasePath;

    System.out.println(path);
    MongoClient mongoClient = MongoClients.create(path);

    this.database = mongoClient.getDatabase(DATABASE_NAME);
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
