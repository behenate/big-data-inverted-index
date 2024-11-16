package org.example;

import org.crawler.Scraper;
import org.openjdk.jmh.annotations.*;

import java.sql.SQLException;

@State(Scope.Thread)
public class Main {

    private final Scraper scraper = new Scraper();

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @Warmup(iterations = 0)
    @Measurement(iterations = 2)
    public void benchmarkDownloadBatch() throws SQLException {
        scraper.downloadBatchMutlithreadedWithSave(501, 600, 100);
    }
}
