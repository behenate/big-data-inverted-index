package org.indexer.DistributedIndexer;

import org.indexer.BaseIndexer;
import org.indexer.IndexerConsumer;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * An indexer, that instead of saving the changes locally, distributes them as an update to all active queryEngines.
 * Each query engine holds its own database
 */
public class DistributedIndexer extends BaseIndexer {
  /**
  * How many books should be in the index in order to send an database update message to the query engine.
  */
  private static int BOOK_BATCH_SIZE = 2;
  /**
   * If this time has passed since the last received book the update will be sent no matter BOOK_BATCH_SIZE param.
   */
  private static int INDEX_STALE_TIME_MS = 10000;

  private String uuid = UUID.randomUUID().toString();
  private int booksInIndex = 0;

  private final Timeout staleTimeTimeout = new Timeout(INDEX_STALE_TIME_MS, true, () -> {
    if (!invertedIndex.isEmpty()) {
      distributeUpdate();
    }
  });

  private BookReceivedCallback bookCallback = (book) -> {
    System.out.println(uuid + " received book: " + book.metadata.title + " " + System.currentTimeMillis());
    processBookText(book);
    booksInIndex += 1;
    staleTimeTimeout.reset();

    if (booksInIndex >= BOOK_BATCH_SIZE) {
      distributeUpdate();
    }
  };

  private IndexerConsumer consumer;
  private DatabaseUpdateProducer producer;

  public DistributedIndexer(String host) throws IOException, TimeoutException {
    this.consumer = new IndexerConsumer(host, bookCallback);
    this.producer = new DatabaseUpdateProducer(host);
  }

  private void distributeUpdate() {
    try {
      System.out.println("DistributeUpdate() of size: " + invertedIndex.size());
      producer.publishUpdate(invertedIndex);
      invertedIndex.clear();
      booksInIndex = 0;
    } catch (IOException e) {
      System.out.println("Failed to distribute the update: " + e);
    }
  }
}

