package org.engine.model;

import example.Update;
import org.bson.Document;

import java.util.List;

public record BookInfo(List<Integer> positions, double frequency) {
  public BookInfo(Update.ProtoBookInfo protoBookInfo) {
    this(protoBookInfo.getPositionsList(), protoBookInfo.getFrequency());
  }
}
