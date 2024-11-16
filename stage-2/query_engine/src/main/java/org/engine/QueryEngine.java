package org.engine;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.engine.model.BookMetadata;
import org.engine.model.BookResult;

import java.util.List;

abstract public class QueryEngine {

    static final String DATABASE_PATH = "mongodb://localhost:27017";

    protected final MongoDatabase database;

    public QueryEngine() {
        ConnectionString connectionString = new ConnectionString(DATABASE_PATH);
        MongoClient mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase("big_data");
    }

    public BookMetadata fetchBookMetadata(int bookId) {
        MongoCollection<Document> collection = database.getCollection("books");
        Document query = new Document("_id", bookId);
        Document result = collection.find(query).first();

        if (result == null) {
            return null;
        }
        String title = result.getString("title");
        String author = result.getString("author");
        String release = result.getString("release");
        String language = result.getString("language");
        String editor = result.getString("editor");

        return new BookMetadata(title, author, release, language, editor);
    }

    abstract public List<BookResult> searchForWord(String word);

    public void printResults(String word, List<BookResult> results) {
        System.out.println("Results for word " + word + ":");
        System.out.println();

        for (BookResult result : results) {
            System.out.println("Book: " + result.bookMetadata().title());
            System.out.println("Positions: " + result.positions());
            System.out.println("Frequency: " + result.frequency());
            System.out.println("Book info");
            System.out.println(
                    "author: " + result.bookMetadata().author() + ", editor: " + result.bookMetadata().editor() +
                            ", language: " + result.bookMetadata().language() + ", release: " +
                            result.bookMetadata().release());
            System.out.println("---------------------");
        }
    }
}
