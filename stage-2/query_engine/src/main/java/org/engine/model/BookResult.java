package org.engine.model;

import org.engine.model.BookMetadata;

public class BookResult {
    int[] positions;
    double frequency;

    BookMetadata bookMetadata;

    public BookResult(int[] positions, double frequency, BookMetadata bookMetadata) {
        this.positions = positions;
        this.frequency = frequency;
        this.bookMetadata = bookMetadata;
    }
}
