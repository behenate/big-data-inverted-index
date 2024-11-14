import pytest
import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../query_engine')))

from query_engine import search_docs_from_db
from query_engine_json import load_from_json, search_word_in_index

common_words = [
    "the", "and", "that", "have", "for", "not", "with", "you", "this", "but",
    "his", "from", "they", "say", "her", "she", "will", "one", "all", "would",
    "there", "their", "what", "out", "about", "who", "get", "which", "when", "make",
    "can", "like", "time", "just", "him", "know", "take", "person", "into", "year",
    "your", "good", "some", "could", "them", "see", "other", "than", "then", "now",
    "look", "only", "come", "its", "over", "think", "also", "back", "after", "use",
    "how", "our", "work", "first", "well", "way", "even", "new", "want", "because",
    "any", "these", "give", "day", "most"
]


def test_json_search_time(benchmark):
    def run_test():
        index = load_from_json()
        for word in common_words:
            _ = search_word_in_index(word, index)

    benchmark.pedantic(run_test, iterations=1, rounds=5, warmup_rounds=3)


def test_sqlite_search_time(benchmark):
    def run_test():
        for word in common_words:
            search_docs_from_db(word)

    benchmark.pedantic(run_test, iterations=1, rounds=5, warmup_rounds=3)

