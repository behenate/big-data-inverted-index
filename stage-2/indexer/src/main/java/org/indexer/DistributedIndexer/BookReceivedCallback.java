package org.indexer.DistributedIndexer;

import org.crawler.Book;

@FunctionalInterface
public interface BookReceivedCallback {
  void run(Book book);
}
