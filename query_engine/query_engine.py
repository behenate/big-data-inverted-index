import os
import sqlite3


def search_docs_from_db(keyword, db_path='../databases/inverted_index.db'):
    """
    A function that searches documents in a SQLite database based on a given keyword.
    """
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    cursor.execute("SELECT doc_ids FROM inverted_index WHERE term = ?", (keyword,))
    result = cursor.fetchone()

    conn.close()

    if result:
        doc_ids = list(map(int, result[0].split(',')))
        return doc_ids
    else:
        return None


def fetch_books_details(doc_ids, db_path='../databases/books.db'):

    # Connection to database
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    placeholders = ', '.join('?' for _ in doc_ids)
    query = f"SELECT id, author, editor, release, language FROM books WHERE id IN ({placeholders})"
    cursor.execute(query, doc_ids)

    books = cursor.fetchall()

    conn.close()

    books_details = []
    for book in books:
        book_info = {
            'id': book[0],
            'author': book[1],
            'editor': book[2],
            'release': book[3],
            'language': book[4],
        }
        books_details.append(book_info)

    return books_details


word_to_search = input("Keyword to search: ")

doc_ids = search_docs_from_db(word_to_search)

if doc_ids:
    print(f"IDs found for word '{word_to_search}': {doc_ids}")

    books_details = fetch_books_details(doc_ids)

    if books_details:
        print("Books details:")
        for book in books_details:
            print(f"ID: {book['id']}, Author: {book['author']}, Edithor: {book['editor']}, "
                  f"Release date: {book['release']}, Language: {book['language']}")
    else:
        print("No books for found IDs.")
else:
    print(f"No results found for keyword: '{word_to_search}'")