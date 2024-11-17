package org.crawler;

import org.bson.Document;

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

  /*
  * We should make this class a proper bson document but I can't be bothered, so we just map this.
  */
  public Metadata(Document document) {
    this.title = document.getString("title");
    this.author = document.getString("author");
    this.editor = document.getString("editor");
    this.release = document.getString("release");
    this.language = document.getString("language");
  }
}
