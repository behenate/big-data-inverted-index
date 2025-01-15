package org.engine.DistributedQueryEngine;

import org.engine.MongoQueryEngine;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class DistributedMongoQueryEngine extends MongoQueryEngine {
  private DatabaseUpdater updater;

  public DistributedMongoQueryEngine(String host) throws IOException, TimeoutException {
    this.updater = new DatabaseUpdater(host);
  }
}
