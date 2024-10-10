import sqlite3
import re
import utils


def connect_db(db_path):
    try:
        db = sqlite3.connect(db_path)
        print(f"Successfully connected to the SQLite database: {db_path}")
        return db
    except sqlite3.Error as e:
        print(f"An error occurred: {e}: {db_path}")
        return None


def save_to_database(inverted_index):
    db = connect_db("../databases/inverted_index.db")
    cursor = db.cursor()

    cursor.execute('''
        CREATE TABLE IF NOT EXISTS inverted_index (
            term TEXT PRIMARY KEY,
            doc_ids TEXT
        )
    ''')

    for term, doc_ids in inverted_index.items():
        doc_ids_str = ','.join(map(str, doc_ids))

        cursor.execute('''
            INSERT INTO inverted_index (term, doc_ids) 
            VALUES (?, ?)
            ON CONFLICT(term) DO UPDATE SET doc_ids=excluded.doc_ids
        ''', (term, doc_ids_str))

    db.commit()
    db.close()


def index_books(db):
    inverted_index = utils.index_documents(db)
    save_to_database(inverted_index)


def run_indexer():
    db = connect_db("../databases/books.db")
    if db is None:
        return None
    index_books(db)
    db.close()
    print("Successfully indexed")


if __name__ == '__main__':
    run_indexer()
