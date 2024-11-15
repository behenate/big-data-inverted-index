package org.engine;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.engine.model.BookMetadata;
import org.engine.model.BookResult;

abstract public class QueryEngine {
    static final String DATABASE_PATH = "mongodb://localhost:27017";

    protected final MongoDatabase database;

    public QueryEngine(){
        ConnectionString connectionString = new ConnectionString(DATABASE_PATH);
        MongoClient mongoClient = MongoClients.create(connectionString);
        this.database = mongoClient.getDatabase("big_data");
    }

    abstract public BookResult[] searchForWord(String word);


    public BookMetadata fetchBookMetadata(int bookId){
        MongoCollection<Document> collection = database.getCollection("books");
        Document query = new Document("_id", bookId);
        Document result = collection.find(query).first();

        if(result == null){
            return null;
        }
        String title = result.getString("title");
        String author = result.getString("author");
        String release = result.getString("release");
        String language = result.getString("language");
        String editor = result.getString("editor");

        return new BookMetadata(title, author, release, language, editor );
    }

}
