import sqlite3
import utils


def connect_db(db_path):
    try:
        db = sqlite3.connect(db_path)
        print(f"Successfully connected to the SQLite database: {db_path}")
        return db
    except sqlite3.Error as e:
        print(f"An error occurred: {e}: {db_path}")
        return None


def create_database_structure(db):
    cursor = db.cursor()
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Words (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        word TEXT UNIQUE
    )
    ''')

    cursor.execute('''
    CREATE TABLE IF NOT EXISTS WordOccurrences (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        word_id INTEGER,
        book_id INTEGER,
        frequency REAL,
        FOREIGN KEY (word_id) REFERENCES Words(id),
        FOREIGN KEY (book_id) REFERENCES Books(id)
    )
    ''')

    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Positions (
        occurrence_id INTEGER,
        position INTEGER,
        FOREIGN KEY (occurrence_id) REFERENCES WordOccurrences(id)
    )
    ''')

    db.commit()


def save_to_database(inverted_index):
    db = connect_db("../databases/inverted_index.db")
    create_database_structure(db)
    cursor = db.cursor()
    for word, books in inverted_index.items():
        cursor.execute("INSERT OR IGNORE INTO Words (word) VALUES (?)", (word,))
        cursor.execute("SELECT id FROM Words WHERE word = ?", (word,))
        word_id = cursor.fetchone()[0]

        for book_id, details in books.items():
            cursor.execute("INSERT INTO WordOccurrences (word_id, book_id, frequency) VALUES (?, ?, ?)",
                           (word_id, book_id, details["frequency"]))
            occurrence_id = cursor.lastrowid

            for position in details["position"]:
                cursor.execute("INSERT INTO Positions (occurrence_id, position) VALUES (?, ?)",
                               (occurrence_id, position))

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
