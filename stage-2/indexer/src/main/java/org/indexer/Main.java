package org.indexer;

import org.indexer.DistributedIndexer.DistributedIndexer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {
  public static void main(String[] args) {
    // CHANGE THIS TO THE IP OF THE RABBIT_MQ SERVER
    try {
      DistributedIndexer indexer = new DistributedIndexer("192.168.1.40");

    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
    }
  }
}
