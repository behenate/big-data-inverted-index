package org.indexer.DistributedIndexer;

public class Timeout {
  long startTime = System.currentTimeMillis();
  int durationMs;

  Thread loopThread;
  public Timeout(int durationMs,Boolean repeat, Runnable onTimeout) {
    this.durationMs = durationMs;

    loopThread = new Thread(() -> {
      while (true) {
        if (System.currentTimeMillis() >= startTime + durationMs) {
          onTimeout.run();
          if (repeat) {
            startTime = System.currentTimeMillis();
          } else {
            return;
          }
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          System.out.println("Timeout thread interrupted");
        }
      }
    });
    loopThread.start();
  }

  public void cancel() {
    loopThread.interrupt();
  }

  public void reset() {
    startTime = System.currentTimeMillis();
  }
}
