package org.engine;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.engine.model.BookInfo;

import java.util.*;

public class MongoQueryEngine extends QueryEngine {

  public MongoQueryEngine() {
    super();
  }


  public Document fetchDocumentFromDatabase(String word, String author, Integer from, Integer to) {
    MongoCollection<Document> collection = this.database.getCollection("inverted_index");

    Document query = new Document("word", word);
    System.out.println("Query: " + query.toJson());

    Document result = collection.find(query).projection(Projections.excludeId()).first();
    if (result == null) {
      return null;
    }

    Document books = (Document) result.get("books");
    Document filteredBooks = new Document();

    for (Map.Entry<String, Object> entry : books.entrySet()) {
      String bookId = entry.getKey();
      if ("server_id".equals(bookId)) {
        continue;
      }

      Document bookDocument = (Document) entry.getValue();
      String bookAuthor = bookDocument.getString("author");

      if (author != null && !author.isEmpty() && !author.equals(bookAuthor)) {
        continue;
      }

      List<Integer> positions = bookDocument.getList("positions", Integer.class);
      List<Integer> filteredPositions = new ArrayList<>();
      for (Integer position : positions) {
        if ((from == null || position >= from) && (to == null || position <= to)) {
          filteredPositions.add(position);
        }
      }

      if (!filteredPositions.isEmpty()) {
        bookDocument.put("positions", filteredPositions);
        filteredBooks.put(bookId, bookDocument);
      }
    }

    return filteredBooks.isEmpty() ? null : filteredBooks;
  }

  @Override
  public Map<String, BookInfo> getWordInfo(String word) {
    Document wordDocument = fetchDocumentFromDatabase(word, null, null, null);
    if (wordDocument == null) {
      return null;
    }

    Map<String, BookInfo> results = new HashMap<>();
    for (Map.Entry<String, Object> entry : wordDocument.entrySet()) {
      String bookId = entry.getKey();
      Document bookDocument = (Document) entry.getValue();
      List<Integer> positions = bookDocument.getList("positions", Integer.class);
      Double frequency = bookDocument.getDouble("frequency");
      String title = bookDocument.getString("title");
      String author = bookDocument.getString("author");
      results.put(bookId, new BookInfo(positions, frequency, title, author));
    }
    return results;
  }

  public long getWordCount() {
    MongoCollection<Document> collection = this.database.getCollection("inverted_index");
    return collection.countDocuments();
  }
}
