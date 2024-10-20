import pytest
import sys
import os


sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../indexer')))

from indexer import run_indexer


def test_indexing_time(benchmark):
    def run_index():
        run_indexer()

    benchmark.pedantic(run_index, iterations=1, rounds=1, warmup_rounds=0)
