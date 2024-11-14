import sqlite3
import json
import utils
import os

def connect_db(db_path):
    try:
        db = sqlite3.connect(db_path)
        print(f"Successfully connected to the SQLite database: {db_path}")
        return db
    except sqlite3.Error as e:
        print(f"An error occurred: {e}: {db_path}")
        return None


def save_to_json(inverted_index, file_path='../json/inverted_index.json'):
    try:
        directory = os.path.dirname(file_path)

        if not os.path.exists(directory):
            os.makedirs(directory, exist_ok=True)

        with open(file_path, 'w+', encoding='utf-8') as f:
            json.dump(inverted_index, f, ensure_ascii=False, indent=4)
        print(f"Inverted index saved to {file_path}")
    except IOError as e:
        print(f"An error occurred while writing to JSON: {e}")


def index_books(db):
    inverted_index = utils.index_documents(db)
    save_to_json(inverted_index)


def run_indexer():
    db = connect_db("../databases/books.db")
    if db is None:
        return None
    index_books(db)
    db.close()
    print("Successfully indexed")


if __name__ == '__main__':
    run_indexer()
