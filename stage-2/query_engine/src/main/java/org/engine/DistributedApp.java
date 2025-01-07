package org.engine;

import org.bson.Document;
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
    MongoQueryEngine queryEngine = new DistributedMongoQueryEngine("192.168.1.40");
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

    get("/bye", (req, res) -> {
      return "Goodbye World!";
    });

    /*TODO:
    *  1. Razem z aktualizacjami bazy należy też wysyłać metadane książek dla każdego ID, żeby dało się aplikować filtry co oni chcą
    *  2. Z BookInfos trzeba wyciągnąć listę id, które spełniają warunek
    *  3. Jak jest poniżej dokument `info` to trzeba przefiltrować żeby były  w nim tylko klucze, które znależliśmy w 2.
    * */
    get("/documents/:words", (req, res) -> {
      String wordsString = req.params(":words");
      String[] words = wordsString.split("\\+");
      String from = req.queryParams("from");
      String to = req.queryParams("to");
      String author = req.queryParams("author");

      Document info = queryEngine.fetchDocumentFromDatabase(words[0]);
      info.put("server_id", uuid);

      return info.toJson();
    });

    /*TODO:
    *  Ten drugi endpoint z pdfa
    */
  }
}