package org.indexer;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.indexer.model.BookInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MongoIndexer extends Indexer {

    public MongoIndexer() {
        super();
    }

    @Override
    void save() {
        int chunkSize = 50000;
        MongoCollection<Document> collection = this.database.getCollection("inverted_index");

        for (Map.Entry<String, Map<Integer, BookInfo>> entry : this.invertedIndex.entrySet()) {
            List<Document> chunkedDocuments = new ArrayList<>();
            Document innerMapDocument = new Document();

            for (Map.Entry<Integer, BookInfo> innerEntry : entry.getValue().entrySet()) {
                innerMapDocument.append(innerEntry.getKey().toString(), innerEntry.getValue());

                if (innerMapDocument.size() >= chunkSize) {
                    chunkedDocuments.add(new Document(entry.getKey(), innerMapDocument));
                    innerMapDocument = new Document();
                }
            }

            if (!innerMapDocument.isEmpty()) {
                chunkedDocuments.add(new Document(entry.getKey(), innerMapDocument));
            }

            for (Document doc : chunkedDocuments) {
                doc.append("_id", new ObjectId());
                collection.insertOne(doc);
            }
        }
    }

    public static void main(String[] args) {
        Indexer mongoIndexer = new MongoIndexer();
        mongoIndexer.indexBooks();
    }
}