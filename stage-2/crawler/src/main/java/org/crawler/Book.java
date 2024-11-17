package org.crawler;

import org.bson.Document;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Book implements Serializable {
  final String FOOTER_INDICATOR = "*** START OF THE PROJECT GUTENBERG EBOOK";

  public int id;
  public String text;
  public Metadata metadata;

  public Book(int id, String text, Metadata metadata) {
    this.id = id;
    this.text = text;
    this.metadata = metadata;
  }

  public Book(int id, String rawText) {
    this.id = id;
    this.metadata = extractMetadata(rawText);
    this.text = extractText(rawText);
  }

  public Book(Document document) {
    this.id = document.getInteger("id");
    this.text = document.getString("text");
    this.metadata = new Metadata(document);
  }

  public void saveToDatabase(Connection connection) {
    String insertSQL = "INSERT INTO Books (id, text, title, author, editor, release, language) VALUES (?, ?, ?, ?, ?, ?, ?)";

    try {
      PreparedStatement statement = connection.prepareStatement(insertSQL);
      statement.setInt(1, id);
      statement.setString(2, text);
      statement.setString(3, metadata.title);
      statement.setString(4, metadata.author);
      statement.setString(5, metadata.editor);
      statement.setString(6, metadata.release);
      statement.setString(7, metadata.language);

      statement.executeUpdate();
    } catch (SQLException e) {
      System.out.println("Error saving to the database: " + e.getMessage());
    }
  }

  private String extractText(String rawText) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    String patternString = Pattern.quote(FOOTER_INDICATOR) + ".*$";
    Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(text);

    if (matcher.find()) {
      int startIndexOfText = matcher.end() + 1;

      if (startIndexOfText < text.length()) {
        return text.substring(startIndexOfText).trim();
      }
    }

    return "";
  }

  public Document toMongoDocument() {
    Document document = new Document();
    document.put("id", this.id);
    document.put("text", this.text);
    document.put("title",this.metadata.title);
    document.put("author",this.metadata.author);
    document.put("editor",this.metadata.editor);
    document.put("release",this.metadata.release);
    document.put("language",this.metadata.language);
    return document;
  }

  private Metadata extractMetadata(String rawText) {
    Metadata metadata = new Metadata();
    String footerIndicator = "*** START OF THE PROJECT GUTENBERG EBOOK";

    String[] lines = rawText.split("\n");
    for (String line : lines) {
      if (line.contains(footerIndicator)) {
        break;
      }

      if (line.contains("Author:") && metadata.author == null) {
        metadata.author = line.split("Author:")[1].trim();
      } else if (line.contains("Editor:") && metadata.editor == null) {
        metadata.editor = line.split("Editor:")[1].trim();
      } else if (line.contains("Release date:") && metadata.release == null) {
        metadata.release = line.split("Release date:")[1].split("\\[")[0].trim();
      } else if (line.contains("Language:") && metadata.language == null) {
        metadata.language = line.split("Language:")[1].trim();
      } else if (line.contains("Title:") && metadata.title == null) {
        metadata.title = line.split("Title:")[1].trim();
      }
    }
    return metadata;
  }
}
