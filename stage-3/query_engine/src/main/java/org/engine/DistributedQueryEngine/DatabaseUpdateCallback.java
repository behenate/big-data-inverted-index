package org.engine.DistributedQueryEngine;

import org.engine.model.BookInfo;

import java.util.Map;

@FunctionalInterface
public interface DatabaseUpdateCallback {
  public void onUpdateReceived(Map<String, Map<Integer, BookInfo>> update);
}
