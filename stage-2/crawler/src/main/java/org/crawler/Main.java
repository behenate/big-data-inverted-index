package org.crawler;

import java.sql.SQLException;

public class Main {

  public static void main(String[] args) throws InterruptedException {
    Scraper scraper = new Scraper();
    scraper.downloadBatchMutlithreadedWithSave(501, 600, 100);
  }
}