package org.engine.DistributedQueryEngine;

import com.rabbitmq.client.*;
import example.Update.*;
import org.engine.model.BookInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class DatabaseUpdateReceiver {
  private static final String EXCHANGE_NAME = "index_broadcast_exchange";
  private static final String UPDATE_QUEUE = "index_update_queue";

  private Channel channel;
  private Connection connection;

  public DatabaseUpdateReceiver(String host, DatabaseUpdateCallback databaseUpdateCallback) throws IOException, TimeoutException {
    String _host = host != null ? host : "localhost";

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(_host);
    this.connection = factory.newConnection();
    this.channel = connection.createChannel();

    channel.exchangeDeclare(EXCHANGE_NAME, "fanout", false);

    String queueName = channel.queueDeclare().getQueue();

    // Bind the queue to the exchange
    channel.queueBind(queueName, EXCHANGE_NAME, "");

    DeliverCallback updateCallback = (consumerTag, delivery) -> {
      byte[] compressed = delivery.getBody();
      byte[] decompressed = decompressData(compressed);
      ProtoUpdate protoUpdate = ProtoUpdate.parseFrom(decompressed);
      Map<String, ProtoIndexEntry> protoIndexEntryMap = protoUpdate.getInvertedIndexMap();
      Map<String, Map<Integer, BookInfo>> lol = protoIndexEntryMap
          .entrySet()
          .stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              v -> deconstructProtoIndexEntry(v.getValue().getMapMap())
          ));

      databaseUpdateCallback.onUpdateReceived(lol);
    };

    channel.basicQos(5);
    channel.basicConsume(
        queueName,
        true, // auto-ack
        updateCallback,
        consumerTag -> {}
    );
  }

  private Map<Integer, BookInfo> deconstructProtoIndexEntry(Map<Integer, ProtoBookInfo> entry) {
    return entry.entrySet().stream().collect(Collectors.toMap(
        Map.Entry::getKey,
        v -> new BookInfo(v.getValue())
    ));
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