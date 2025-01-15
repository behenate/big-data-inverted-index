package org.engine;

import org.bson.Document;
import org.bson.json.JsonObject;
import org.engine.DistributedQueryEngine.DistributedMongoQueryEngine;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static spark.Spark.*;

public class DistributedApp {
  public static void main(String[] args) throws IOException, TimeoutException {
    int _port = (args.length > 0) ? Integer.parseInt(args[0]) : 1221;
    String uuid = UUID.randomUUID().toString();
    // will Automatically receive updates and update the local index
    // REPLACE WITH THE IP OF PC RUNNING THE RABBIT INSTANCE
    MongoQueryEngine queryEngine = new DistributedMongoQueryEngine("192.168.1.134");
    port(_port);
    // CORS setup - Global before-filter
    before((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      response.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
      response.header("Access-Control-Allow-Credentials", "true");
    });

    get("/hello", (req, res) -> "Hello World");

    post("/echo", (req, res) -> {
      return req.body(); // Simple echo service
    });

    get("/documents/:words", (req, res) -> {
      String wordsString = req.params(":words");
      String[] words = wordsString.split("\\+");
      String from = req.queryParams("from");
      String to = req.queryParams("to");
      String author = req.queryParams("author");

      Integer fromPosition = from != null && !from.isEmpty() ? Integer.parseInt(from) : null;
      Integer toPosition = to != null && !to.isEmpty() ? Integer.parseInt(to) : null;

      Document mergedDocument = new Document();
      StringBuilder mergedText = new StringBuilder();

      for (String word : words) {
        Document info = queryEngine.fetchDocumentFromDatabase(word, author, fromPosition, toPosition);
        if (info != null) {
          String text = info.getString("text");
          if (text != null) {
            mergedText.append(text).append(" ");
          }
          info.forEach((key, value) -> {
            if (!key.equals("text")) { // Skip merging "text" again
              mergedDocument.put(key, value);
            }
          });
        }
      }
      if (!mergedText.isEmpty()) {
        mergedDocument.put("text", mergedText.toString().trim());
      }
      if (mergedDocument.isEmpty()) {
        return new Document().toJson();
      }
      mergedDocument.put("server_id", uuid);
      return mergedDocument.toJson();
    });

    get("/stats/:type", (req, res) -> {
      String type = req.params(":type");
      Document stats = new Document();

      if ("words".equals(type)) {
        long wordCount = queryEngine.getWordCount();
        System.out.println("Word count: " + wordCount);
        stats.append("word_count", wordCount);
      } else {
        res.status(400);
        return "Invalid stat type.";
      }

      return stats.toJson();
    });
  }
}