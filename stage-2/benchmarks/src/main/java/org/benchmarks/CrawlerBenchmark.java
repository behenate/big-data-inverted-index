package org.benchmarks;

import org.crawler.MongoConnection;
import org.crawler.Scraper;
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
public class CrawlerBenchmark {

    private Scraper scraper;

    @Setup(Level.Trial)
    public void setup() {
        MongoConnection mongoConnection = new MongoConnection("mongodb://localhost:27017");
        scraper = new Scraper();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    @Fork(1)
    public void benchmarkDownloadBatch() throws InterruptedException {
        int poolSize = Runtime.getRuntime().availableProcessors() * 2;
        scraper.downloadBatchMutlithreadedWithSave(1, 1000, poolSize);
    }
}
