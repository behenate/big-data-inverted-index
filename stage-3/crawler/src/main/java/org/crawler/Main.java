package org.crawler;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Main {

  public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {
    Scraper scraper = new Scraper();
    // CHANGE THIS TO IP OF THE PC RUNNING RABBITMQ SERVER!
    CrawledBookProducer cp1 = new CrawledBookProducer("192.168.1.134");
    scraper.downloadBatchWithRabbitPublish(cp1, 0, 2000, 16);
  }
}