package org.engine;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.engine.model.BookInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MongoQueryEngine extends QueryEngine {
    public MongoQueryEngine(){
        super();
    }

    public Document fetchDocumentFromDatabase(String word) {
        MongoCollection<Document> collection = this.database.getCollection("inverted_index");

        Document query = new Document("word", word);
        Document result = collection.find(query).projection(Projections.excludeId()).first();
        if(result == null){
            return null;
        }
        return (Document) result.get("books");
    }

    @Override
    public Map<String, BookInfo> getWordInfo(String word) {
        Document wordDocument = fetchDocumentFromDatabase(word);
        if (wordDocument == null) {
            return null;
        }

        Map<String, BookInfo> results = new HashMap<>();
        for (Map.Entry<String, Object> entry : wordDocument.entrySet()) {
            String bookId = entry.getKey();
            Document bookDocument = (Document) entry.getValue();
            List<Integer> positions = bookDocument.getList("positions", Integer.class);
            Double frequency = bookDocument.getDouble("frequency");
            results.put(bookId, new BookInfo(positions, frequency));
        }
        return results;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a word you want to find:");
        String userWord = scanner.nextLine();
        MongoQueryEngine mongoQueryEngine = new MongoQueryEngine();
        mongoQueryEngine.searchForWord(userWord);

        scanner.close();
    }
}
