package org.indexer;
import org.bson.Document;
import org.indexer.model.BookInfo;

import java.util.Map;

public class MongoIndexer extends Indexer {

    public MongoIndexer(){
        super();
    }

    @Override
    void save() {
        Document dataDocument = new Document();
        for (Map.Entry<String, Map<Integer, BookInfo>> entry : this.invertedIndex.entrySet()) {
            Document innerMapDocument = new Document();
            for (Map.Entry<Integer, BookInfo> innerEntry : entry.getValue().entrySet()) {
                innerMapDocument.append(innerEntry.getKey().toString(), innerEntry.getValue());
            }
            dataDocument.append(entry.getKey(), innerMapDocument);
        }
        this.database.getCollection("inverted_index").insertOne(dataDocument);
    }


    public static void main(String[] args) {
        Indexer mongoIndexer = new MongoIndexer();
        mongoIndexer.indexBooks();
    }
}