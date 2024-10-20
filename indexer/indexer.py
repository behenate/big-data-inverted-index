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

    cursor.execute('''
        CREATE TABLE IF NOT EXISTS Books (
            id INTEGER PRIMARY KEY,
            title TEXT,
            author TEXT,
            editor TEXT,
            release TEXT,
            language TEXT
        )
    ''')

    cursor.execute("CREATE INDEX IF NOT EXISTS wordIndex ON Words(word);")
    cursor.execute("CREATE INDEX IF NOT EXISTS word_occurrences_word_id_index on WordOccurrences(word_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS word_occurrences_book_id_index on WordOccurrences(book_id);")
    cursor.execute("CREATE INDEX IF NOT EXISTS positions_occurrence_id_index on Positions(occurrence_id);")

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
            book_metadata = details["bookMetadata"]
            insert_data = (book_id, book_metadata["title"], book_metadata["author"], book_metadata["editor"], book_metadata["release"], book_metadata["language"])
            book_query = f"INSERT OR IGNORE INTO Books (id, title, author, editor, release, language) VALUES {insert_data}"
            cursor.execute(book_query)

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
