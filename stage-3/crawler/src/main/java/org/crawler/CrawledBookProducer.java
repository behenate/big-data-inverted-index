package org.crawler;

import com.rabbitmq.client.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPOutputStream;

public class CrawledBookProducer {
  private static final String SIGNAL_QUEUE = "hello_goodbye_queue";
  private static final String BOOK_QUEUE = "crawled_book_queue";
  private final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
      .contentType("application/octet-stream")
      .deliveryMode(1) // Non-persistent messages for max speed
      .build();
  private Connection connection;
  private Channel channel;


  public CrawledBookProducer(String host) throws IOException, TimeoutException {
    String _host = host != null ? host : "localhost";
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(_host);

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    this.connection = connection;
    this.channel = channel;

    channel.queueDeclare(SIGNAL_QUEUE, true, false, false, null);
    channel.queueDeclare(BOOK_QUEUE, true, false, false, null);
  }

  public void publishBook(Book book) throws IOException {
    byte[] bytes = book.toProtoBytes();
    channel.basicPublish("", BOOK_QUEUE, properties, compressData(bytes));
  }

  private static byte[] compressData(byte[] data) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
      gzipOutputStream.write(data);
    }
    return byteArrayOutputStream.toByteArray();
  }
}