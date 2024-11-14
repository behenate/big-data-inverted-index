package org.crawler;

import java.sql.SQLException;

public class Main {

  public static void main(String[] args) throws SQLException {
    Scraper scraper = new Scraper();
    scraper.downloadBatchMutlithreadedWithSave(501, 600, 100);
//    List<Book> testBooks = scraper.downloadBatchMultithreaded(1, 500, 100, null);

    // Output results
//    for (Book result : testBooks) {
//      System.out.println(result.metadata.title);
//    }
  }
}