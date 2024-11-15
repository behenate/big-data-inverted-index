package org.engine.model;

public class BookMetadata {
    String title;
    String author;

    String editor;

    String release;
    String language;

    public BookMetadata(String title, String author, String release, String language, String editor) {
        this.title = title;
        this.author = author;
        this.editor = editor;
        this.release = release;
        this.language = language;
    }
}
