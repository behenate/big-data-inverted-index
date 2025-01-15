package org.indexer.DistributedIndexer;

import com.rabbitmq.client.*;
import example.Update.*;
import org.indexer.model.BookInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;


public class DatabaseUpdateProducer {
  private static final String EXCHANGE_NAME = "index_broadcast_exchange";
  private final AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
      .contentType("application/octet-stream")
      .deliveryMode(1) // Non-persistent messages for max speed
      .build();
  private final Connection connection;
  private final Channel channel;


  public DatabaseUpdateProducer(String host) throws IOException, TimeoutException {
    String _host = host != null ? host : "localhost";
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(_host);

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    this.connection = connection;
    this.channel = channel;

    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, false);
  }

  public void publishUpdate(Map<String, Map<Integer, BookInfo>> update) throws IOException {
    ProtoUpdate protoUpdate = toProtoUpdate(update);
    channel.basicPublish(
        EXCHANGE_NAME,
        "", // routing key is empty for fanout
        null,
        compressData(protoUpdate.toByteArray()));
  }

  private ProtoUpdate toProtoUpdate(Map<String, Map<Integer, BookInfo>> index) {
    Map<String, ProtoIndexEntry> protoMap = index
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            stringMapEntry -> toProtoIndexEntry(stringMapEntry.getValue())
        ));
    return ProtoUpdate.newBuilder().putAllInvertedIndex(protoMap).build();
  }

  private ProtoIndexEntry toProtoIndexEntry(Map<Integer, BookInfo> entry) {
    Map<Integer, BookInfo> filtered = entry.entrySet().stream().filter(v -> !v.getValue().getPositions().isEmpty()).collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
    ));

    Map<Integer, ProtoBookInfo> protoMap = filtered
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            value -> value.getValue().toProtoBookInfo()
        ));
    return ProtoIndexEntry.newBuilder().putAllMap(protoMap).build();
  }

  private static byte[] compressData(byte[] data) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
      gzipOutputStream.write(data);
    }
    return byteArrayOutputStream.toByteArray();
  }
}