package org.crawler;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Scraper {
  private static final String DB_URL = "jdbc:sqlite:/src/main/resources/books.db";

  private final Connection databaseConnection;

  public Scraper() throws SQLException {
    File dbDirectory = new File("src/main/resources");
    File dbFile = new File(dbDirectory, "books.db");
    databaseConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    createTable();
  }

  void createTable() throws SQLException {
    String createTableSQL = "CREATE TABLE IF NOT EXISTS Books ("
        + "id INTEGER PRIMARY KEY, "
        + "text TEXT, "
        + "title TEXT, "
        + "author TEXT, "
        + "editor TEXT, "
        + "release TEXT, "
        + "language TEXT);";


    Statement stmt = databaseConnection.createStatement();
    stmt.execute(createTableSQL);
  }

  public Book downloadBook(int id) throws URISyntaxException, IOException {
    String url = "https://www.gutenberg.org/cache/epub/" + id + "/pg" + id + ".txt";
    return downloadBookFromUrl(id, url);
  }

  public Book downloadBookFromUrl(int id, String url) throws URISyntaxException, IOException {
    URL bookUrl = new URI(url).toURL();

    if (!Scraper.urlExists(url)) {
      throw new RuntimeException("The URI is invalid.");
    }
    String rawText = new String(bookUrl.openStream().readAllBytes(), StandardCharsets.UTF_8);

    return new Book(id, rawText);
  }

  private static boolean urlExists(String urlString) {
    try {
      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection httpUrlConn = (HttpURLConnection) new URL(urlString).openConnection();
      httpUrlConn.setRequestMethod("HEAD");
      return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
    } catch (IOException e) {
      return false;
    }
  }

  public ArrayList<Book> downloadBatchMultithreaded(int from, int to, int poolSize) {
    ExecutorService executor = Executors.newFixedThreadPool(poolSize);

    List<Future<Book>> futures = new ArrayList<>();

    for (int i = from; i <= to + 1; i++) {
      int finalI = i;
      futures.add(executor.submit(() -> {
        try {
          Book book = downloadBook(finalI);
          System.out.println("Downloaded book id: " + finalI);
          return book;
        } catch (Exception e) {
          System.out.println("Failed to download book id: " + finalI + " " + e.getMessage());
        }
        return null;
      }));
    }

    // Array to store results
    ArrayList<Book> results = new ArrayList<>();

    // Shutdown the executor
    executor.shutdown();

    try {
      if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
        executor.shutdownNow();
      }

      for (int i = 0; i < futures.size(); i++) {
        Book book = futures.get(i).get();
        if (book == null) {
          continue;
        }
        results.add(book);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return results;
  }

  public void downloadBatchMutlithreadedWithSave(int from, int to, int poolSize) {
    ExecutorService executor = Executors.newFixedThreadPool(poolSize);

    for (int i = from; i <= to + 1; i++) {
      int finalI = i;
      executor.submit(() -> {
        try {
          Book book = downloadBook(finalI);
          book.saveToDatabase(databaseConnection);
          System.out.println("Downloaded book id: " + finalI);
          return book;
        } catch (Exception e) {
          System.out.println("Failed to download book id: " + finalI + " " + e.getMessage());
        }
        return null;
      });
    }

    // Shutdown the executor
    executor.shutdown();

    try {
      if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
        executor.shutdownNow();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
