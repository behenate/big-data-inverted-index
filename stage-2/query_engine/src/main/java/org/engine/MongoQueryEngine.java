package org.engine;

import org.engine.model.BookResult;

public class MongoQueryEngine extends QueryEngine{

    @Override
    public BookResult[] searchForWord(String word) {
        return new BookResult[1];
    }
}
