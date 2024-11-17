package org.example;

import org.indexer.MongoIndexer;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class MongoIndexerBenchmark {

    private MongoIndexer mongoIndexer;

    @Setup(Level.Trial)
    public void setup() {
        mongoIndexer = new MongoIndexer();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    @Fork(1)
    public void benchmarkIndexBooks() {
        mongoIndexer.indexBooks();
    }
}
