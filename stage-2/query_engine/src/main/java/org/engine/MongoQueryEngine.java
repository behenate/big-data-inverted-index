package org.engine;

import org.engine.model.BookResult;

import java.util.ArrayList;
import java.util.List;

public class MongoQueryEngine extends QueryEngine{

    @Override
    public List<BookResult> searchForWord(String word) {
        return new ArrayList<>();
    }
}
