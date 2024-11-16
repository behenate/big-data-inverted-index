package org.engine;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.engine.model.BookInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoQueryEngine extends QueryEngine {

    public static final String DB_PATH = "mongodb://localhost:27017";

    public Document fetchDocumentFromDatabase(String word) {
        MongoClient mongoClient = MongoClients.create(DB_PATH);
        MongoDatabase database = mongoClient.getDatabase("big_data");
        MongoCollection<Document> collection = database.getCollection("inverted_index");

        Document query = new Document("word", word);
        Document result = collection.find(query).projection(Projections.excludeId()).first();
        mongoClient.close();
        return result;
    }

    @Override
    public Map<Integer, BookInfo> getWordInfo(String word) {
        Document wordDocument = fetchDocumentFromDatabase(word);
        if (wordDocument == null) {
            System.out.println("Word not found in the database");
            return null;
        }

        Map<Integer, BookInfo> results = new HashMap<>();
        for (Map.Entry<String, Object> entry : wordDocument.entrySet()) {
            int bookId = Integer.parseInt(entry.getKey());
            Document bookDocument = (Document) entry.getValue();
            List<Integer> positions = bookDocument.getList("positions", Integer.class);
            double frequency = bookDocument.getDouble("frequency");
            results.put(bookId, new BookInfo(positions, frequency));
        }
        return results;
    }
}
