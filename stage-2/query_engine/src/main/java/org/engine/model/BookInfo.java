package org.engine.model;

import org.bson.Document;

import java.util.List;

public record BookInfo(List<Integer> positions, double frequency) {}
