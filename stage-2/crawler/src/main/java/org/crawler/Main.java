package org.crawler;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {

  public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {
    Scraper scraper = new Scraper();
    // CHANGE THIS TO IP OF THE PC RUNNING RABBITMQ SERVER!
    CrawledBookProducer cp1 = new CrawledBookProducer("192.168.1.40");

    for (int i = 0; i < 2000; i++) {
      try {
        // Delay for demo purposes
        Thread.sleep(1500);
        scraper.downloadBatchWithRabbitPublish(cp1, i, i+1, 1);
        System.out.println("Crawled bookId: " + i);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}