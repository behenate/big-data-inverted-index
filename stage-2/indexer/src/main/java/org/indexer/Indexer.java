package org.indexer;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.indexer.model.Book;
import org.indexer.model.BookInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class Indexer {

    protected final MongoDatabase database;

    protected final Map<String, Map<Integer, BookInfo>> invertedIndex;

    private final List<String> STOP_WORDS =
            Arrays.asList("a", "an", "the", "and", "or", "but", "if", "then", "else",
                    "when", "at", "by", "for", "with", "without", "on", "is", "are", "was", "were", "has", "have",
                    "had", "do", "does", "did", "in", "to", "of", "it", "its", "1", "2", "3", "4", "5", "6", "7", "8",
                    "9", "these", "those", "this", "that", "not", "no");

    public Indexer() {
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        MongoClient mongoClient = MongoClients.create(connectionString);
        this.invertedIndex = new HashMap<>();
        this.database = mongoClient.getDatabase("big_data");
    }

    public void fetchBooks() {
        MongoCursor<Document> cursor = this.database.getCollection("books").find().iterator();

        try {
            while (cursor.hasNext()) {
                Document document = cursor.next();
                Book currBook = new Book(document.getInteger("id"), document.getString("text"));
                processBookText(currBook);
            }
        } finally {
            cursor.close();
        }
    }

    public List<String> tokenize(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("[^\\w\\s]", ""); // Removes non-word and non-whitespace characters
        String[] words = text.split("\\s+"); // Splits text by whitespace

        return new ArrayList<>(Arrays.asList(words));
    }

    private void processBookText(Book book) {
        int bookId = book.getId();
        String text = book.getText();

        List<String> tokenizedText = tokenize(text);
        int wordCount = tokenizedText.size();
        int position = 0;
        for (String word : tokenizedText) {
            position++;
            if (STOP_WORDS.contains(word)) {
                continue;
            }
            invertedIndex.putIfAbsent(word, new HashMap<>());
            if (!invertedIndex.get(word).containsKey(bookId)) {
                BookInfo bookInfo = new BookInfo();
                invertedIndex.get(word).put(bookId, bookInfo);
            }
            invertedIndex.get(word).get(bookId).addPosition(position);
        }

        for (Map.Entry<String, Map<Integer, BookInfo>> entry : invertedIndex.entrySet()) {
            String word = entry.getKey();
            if (STOP_WORDS.contains(word)) {
                continue;
            }
            for (Map.Entry<Integer, BookInfo> bookEntry : entry.getValue().entrySet()) {
                List<Integer> positions = bookEntry.getValue().getPositions();
                BookInfo info = bookEntry.getValue();
                info.setFrequency((double) (positions.size()) / wordCount);
            }
        }
    }

    abstract void save();

    public void indexBooks(){
        fetchBooks();
        save();
    }
}
