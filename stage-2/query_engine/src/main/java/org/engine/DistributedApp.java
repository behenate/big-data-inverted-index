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
    MongoQueryEngine queryEngine = new DistributedMongoQueryEngine("192.168.1.139");
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
    *  dodać filtry author,
    *  from -pozycja od której występuje slowo
    *  to - pozycja do ktorej ma wystepowac
    * */
    get("/documents/:words", (req, res) -> {
      String wordsString = req.params(":words");
      String[] words = wordsString.split("\\+");
      String from = req.queryParams("from");
      String to = req.queryParams("to");
      String author = req.queryParams("author");

      Document info = queryEngine.fetchDocumentFromDatabase(words[0], author);
      if(info == null){
        return (new Document()).toJson();
      }
      info.put("server_id", uuid);

      return info.toJson();
    });

    /*TODO:
    *  GET /stats/:type
    *  Define different types of stats and the expected json
    */
    get("/stats/:type", (req, res) -> {
      String type = req.params(":type");
      // type może być books albo words -> tj. ilość ksiązek/słów w bazie
      // if type == books -> ile ksiazek w bazie
      // if type == words -> ile słów
      return (new Document()).toJson();
    });
  }
}