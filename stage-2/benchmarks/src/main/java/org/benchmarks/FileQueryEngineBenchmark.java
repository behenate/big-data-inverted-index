package org.benchmarks;

import org.engine.FileQueryEngine;
import org.engine.model.BookInfo;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class FileQueryEngineBenchmark {

    private FileQueryEngine fileQueryEngine;

    // List of common words for benchmarking
    private final List<String> commonWords = List.of(
            "that", "have", "for", "not", "with", "you", "this", "but", "his", "most",
            "from", "they", "say", "her", "she", "will", "one", "all", "would", "day",
            "there", "their", "what", "out", "about", "who", "get", "which", "when", "make",
            "can", "like", "time", "just", "him", "know", "take", "person", "into", "year",
            "your", "good", "some", "could", "them", "see", "other", "than", "then", "now",
            "look", "only", "come", "its", "over", "think", "also", "back", "after", "use",
            "how", "our", "work", "first", "well", "way", "even", "new", "want", "because",
            "any", "these", "give"
    );

    @Setup(Level.Trial)
    public void setup() {
        fileQueryEngine = new FileQueryEngine();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    @Fork(1)
    public void benchmarkSearchCommonWords() {
        for (String word : commonWords) {
            Map<String, BookInfo> wordInfo = fileQueryEngine.getWordInfo(word);
        }
    }
}
