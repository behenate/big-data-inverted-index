package org.example;

import org.indexer.FileIndexer;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class FileIndexerBenchmark {

    private FileIndexer fileIndexer;

    @Setup(Level.Trial)
    public void setup() {
        fileIndexer = new FileIndexer();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    @Fork(1)
    public void benchmarkIndexing() {
        fileIndexer.indexBooks();
    }
}
