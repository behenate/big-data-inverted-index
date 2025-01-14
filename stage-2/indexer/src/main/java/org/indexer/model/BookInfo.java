package org.indexer.model;

import example.Book;
import example.Update.*;

import java.util.ArrayList;
import java.util.List;

public class BookInfo {

  private List<Integer> positions;
  private Double frequency;
  private String title;
  private String author;

  public BookInfo(String title, String author) {
    this.positions = new ArrayList<>();
    this.frequency = 0.0;
    this.title = title;
    this.author = author;
  }

  public BookInfo(ProtoBookInfo protoBookInfo) {
    this.positions = new ArrayList<>(protoBookInfo.getPositionsList());
    this.frequency = protoBookInfo.getFrequency();
    this.title = protoBookInfo.getTitle();
    this.author = protoBookInfo.getAuthor();
  }

  public void addPosition(int position) {
    this.positions.add(position);
  }

  public List<Integer> getPositions() {
    return this.positions;
  }

  public void setFrequency(double frequency) {
    this.frequency = frequency;
  }

  public Double getFrequency() {
    return frequency;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public ProtoBookInfo toProtoBookInfo() {
    ProtoBookInfo.Builder builder = ProtoBookInfo.newBuilder();
    builder.setFrequency(this.frequency);
    builder.addAllPositions(positions);
    builder.setTitle(this.title);
    builder.setAuthor(this.author);
    return builder.build();
  }
}