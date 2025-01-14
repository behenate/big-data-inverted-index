package org.indexer;

import org.crawler.Book;
import org.indexer.model.BookInfo;

import java.util.*;

/**
 * An indexer that only saves the data into it's own in-memory index
 */
public abstract class BaseIndexer {

  protected final Map<String, Map<Integer, BookInfo>> invertedIndex = new HashMap<>();

  private final List<String> STOP_WORDS =
      Arrays.asList("a", "an", "the", "and", "or", "but", "if", "then", "else",
          "when", "at", "by", "for", "with", "without", "on", "is", "are", "was", "were", "has", "have",
          "had", "do", "does", "did", "in", "to", "of", "it", "its", "1", "2", "3", "4", "5", "6", "7", "8",
          "9", "these", "those", "this", "that", "not", "no");


  protected List<String> tokenize(String text) {
    text = text.toLowerCase();
    text = text.replaceAll("[^\\w\\s]", "");
    String[] words = text.split("\\s+");

    return new ArrayList<>(Arrays.asList(words));
  }

  protected void processBookText(Book book) {
    int bookId = book.id;
    String text = book.text;

    List<String> tokenizedText = tokenize(text);
    int wordCount = tokenizedText.size();
    int position = 0;
    for (String word : tokenizedText) {
      position++;
      if (STOP_WORDS.contains(word)) {
        continue;
      }
      // Filter out one letter words
      if (word.length() == 1) {
        continue;
      }
      // Many of the books contain multiple checksums/weird character combinations
      // This should filter out most of them at a cost of not allowing words with numbers in the database
      if (word.matches(".*\\d+.*")) {
        continue;
      }
      invertedIndex.putIfAbsent(word, new HashMap<>());
      if (!invertedIndex.get(word).containsKey(bookId)) {
        BookInfo bookInfo = new BookInfo(book.metadata.title, book.metadata.author);
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

  public Map<String, Map<Integer, BookInfo>> getInvertedIndex() {
    return invertedIndex;
  }
}
