package org.crawler;

import com.google.protobuf.AbstractMessage;
import example.Book.ProtoMetadata;
import org.bson.Document;

public class Metadata {
  public String title;
  public String author;
  public String editor;
  public String release;
  public String language;

  public Metadata() {}

  public Metadata(ProtoMetadata protoMetadata) {
    this.title = protoMetadata.getTitle();
    this.author = protoMetadata.getAuthor();
    this.editor = protoMetadata.getEditor();
    this.release = protoMetadata.getRelease();
    this.language = protoMetadata.getLanguage();
  }

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

  public ProtoMetadata toProtoMetadata() {
    ProtoMetadata.Builder builder = ProtoMetadata.newBuilder();

    if (this.title != null) {
      builder.setTitle(title);
    }
    if (this.author != null) {
      builder.setAuthor(author);
    }
    if (this.editor != null) {
      builder.setEditor(editor);
    }
    if (this.release != null) {
      builder.setRelease(release);
    }
    if (this.language != null) {
      builder.setLanguage(language);
    }

    return builder.build();
  }
}
