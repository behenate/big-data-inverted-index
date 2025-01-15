package org.crawler;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@FunctionalInterface
interface DownloadCallback {
  void run(Book book);
}

public class Scraper {
  private int counter = 0;
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

  public ArrayList<Book> downloadBatchMultithreaded(int from, int to, int poolSize) throws InterruptedException, ExecutionException {
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

    ArrayList<Book> results = new ArrayList<>();
    executor.shutdown();


    if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
      executor.shutdownNow();
    }

    for (Future<Book> future : futures) {
      Book book = future.get();
      if (book == null) {
        continue;
      }
      results.add(book);
    }


    return results;
  }

  public void downloadBatchMutlithreadedWithCallback(int from, int to, int poolSize, DownloadCallback callback) throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(poolSize);

    for (int i = from; i <= to + 1; i++) {
      int finalI = i;
      executor.submit(() -> {
        try {
          Book book = downloadBook(finalI);
          callback.run(book);
          return book;
        } catch (Exception e) {
          System.out.println("Failed to download book id: " + finalI + " " + e);
        }
        return null;
      });
    }

    executor.shutdown();

    if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
      executor.shutdownNow();
    }
  }

  public void downloadBatchWithRabbitPublish(CrawledBookProducer producer, int from, int to, int poolSize) throws InterruptedException {
    downloadBatchMutlithreadedWithCallback(from, to, poolSize, (book) -> {
      try {
        counter++;
        producer.publishBook(book);
      } catch (Exception e) {
        System.out.println("Failed to publish the book: " + book.id + " " + e);
      }
    });
  }
}
