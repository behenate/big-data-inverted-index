package org.engine.model;

import java.util.List;

public record BookResult(List<Integer> positions, double frequency, BookMetadata bookMetadata) {
}
