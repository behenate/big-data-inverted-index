package org.example;

import org.crawler.CrawledBookProducer;
import org.crawler.Scraper;
import org.engine.DistributedQueryEngine.DistributedMongoQueryEngine;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CommunicationTests {
  public static void main(String[] args) throws IOException, TimeoutException {
    String host = "localhost";
    Scraper testScraper = new Scraper();
    Scraper testScraper2 = new Scraper();

    CrawledBookProducer cp1 = new CrawledBookProducer(host);
    CrawledBookProducer cp2 = new CrawledBookProducer(host);
    DistributedMongoQueryEngine queryEngine = new DistributedMongoQueryEngine(host);


    try {
      Thread.sleep(100);
      new Thread(() -> {
        try {
          testScraper.downloadBatchWithRabbitPublish(cp1, 0, 500, 50);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
      new Thread(() -> {
        try {
          testScraper2.downloadBatchWithRabbitPublish(cp2, 500, 1000, 50);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
