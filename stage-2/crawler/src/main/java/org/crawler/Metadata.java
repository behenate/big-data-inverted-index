package org.crawler;

public class Metadata {
  public String title;
  public String author;
  public String editor;
  public String release;
  public String language;

  public Metadata() {}
  public Metadata(String title, String author, String editor, String release, String language) {
    this.title = title;
    this.author = author;
    this.editor = editor;
    this.release = release;
    this.language = language;
  }
}
