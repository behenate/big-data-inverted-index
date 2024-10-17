import pytest
import sys
import os


sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../indexer')))

from indexer_json import run_indexer  # Upewnij się, że importujesz poprawnie

def test_indexing_time(benchmark):
    # Funkcja uruchamiająca proces indeksowania
    def run_index():
        run_indexer()  # Uruchamiamy funkcję, która indeksuje książki

    # Mierzymy czas wykonania funkcji run_index
    benchmark(run_index)  # Używamy benchmark bez przypisywania wyniku
