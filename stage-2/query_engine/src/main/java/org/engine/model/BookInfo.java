package org.engine.model;

import org.bson.Document;

import java.util.List;

public record BookInfo(List<Integer> positions, double frequency) {
    public static BookInfo fromDocument(Document document) {
        List<Integer> positions = document.getList("positions", Integer.class);
        double frequency = document.getDouble("frequency");

        return new BookInfo(positions, frequency);
    }
}
