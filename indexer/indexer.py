import sqlite3
import re


def connect_db(db_path):
    try:
        db = sqlite3.connect(db_path)
        print(f"Successfully connected to the SQLite database: {db_path}")
        return db
    except sqlite3.Error as e:
        print(f"An error occurred: {e}: {db_path}")
        return None


def get_all_books(cursor):
    cursor.execute("SELECT id, text FROM books")
    return cursor.fetchall()


def tokenize(text):
    stop_words = {'a', 'an', 'the', 'and', 'or', 'but', 'if', 'then', 'else', 'when', 'at', 'by', 'for', 'with',
                  'without', 'on', 'is', 'are', 'was', 'were', 'has', 'have', 'had', 'do', 'does', 'did', 'in', 'to',
                  'of', 'it', 'its', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'these', 'those', 'this', 'that'}

    text = text.lower()
    text = re.sub(r'[^\w\s]', '', text)
    words = text.split()
    filtered_words = [word for word in words if word not in stop_words]
    return filtered_words


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


def index_documents(db):
    inverted_index = dict()
    cursor = db.cursor()
    books = get_all_books(cursor)
    for book in books:
        book_id = book[0]
        text = book[1]
        tokenized_text = tokenize(text)

        for word in tokenized_text:
            if word not in inverted_index:
                inverted_index[word] = []
            if book_id not in inverted_index[word]:
                inverted_index[word].append(book_id)

    save_to_database(inverted_index)


def run_indexer():
    db = connect_db("../databases/books.db")
    if db is None:
        return None
    index_documents(db)
    db.close()
    print("Successfully indexed")


if __name__ == '__main__':
    run_indexer()
