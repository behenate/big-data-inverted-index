package org.indexer.DistributedIndexer;

import com.rabbitmq.client.*;
import org.crawler.Book;
import org.indexer.DistributedIndexer.BookReceivedCallback;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

public class IndexerConsumer {
  private static final String SIGNAL_QUEUE = "hello_goodbye_queue";
  private static final String BOOK_QUEUE = "crawled_book_queue";

  private Channel channel;
  private Connection connection;

  public IndexerConsumer(String host, BookReceivedCallback bookReceivedCallback) throws IOException, TimeoutException {
    String _host = host != null ? host : "localhost";

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(_host);
    this.connection = factory.newConnection();
    this.channel = connection.createChannel();

    channel.queueDeclare(SIGNAL_QUEUE, true, false, false, null);
    channel.queueDeclare(BOOK_QUEUE, true, false, false, null);

    DeliverCallback bookCallback = (consumerTag, delivery) -> {
      byte[] compressed = delivery.getBody();
      byte[] decompressed = decompressData(compressed);
      Book book = new Book(decompressed);
      bookReceivedCallback.run(book);
    };

    // We can assume high throughput, don't really care about loss of data
    channel.basicQos(100);

    channel.basicConsume(BOOK_QUEUE, true, bookCallback, consumerTag -> {});
  }

  private static byte[] decompressData(byte[] compressedData) throws IOException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

      byte[] buffer = new byte[1024];
      int len;
      while ((len = gzipInputStream.read(buffer)) != -1) {
        byteArrayOutputStream.write(buffer, 0, len);
      }

      return byteArrayOutputStream.toByteArray();
    }
  }
}