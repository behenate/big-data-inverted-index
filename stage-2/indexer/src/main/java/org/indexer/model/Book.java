package org.indexer.model;

public class Book {
    private String text;
    private int id;

    public Book(int id, String text){
        this.id = id;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getId() {
        return id;
    }
}