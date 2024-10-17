import sqlite3


def search_docs_from_db(keyword, db_path='../databases/inverted_index.db'):
    """
    A function that searches documents in a SQLite database based on a given keyword.
    """
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    query = """
    SELECT w.word, wo.book_id, wo.frequency, p.position 
    FROM Words w
    JOIN WordOccurrences wo ON w.id = wo.word_id
    JOIN Positions p ON wo.id = p.occurrence_id
    WHERE w.word = ?
    """
    cursor.execute(query, (keyword,))
    result = cursor.fetchall()

    conn.close()

    if result:
        books_data = {}
        for word, book_id, frequency, position in result:
            if book_id not in books_data:
                books_data[book_id] = {'frequency': frequency, 'positions': []}
            books_data[book_id]['positions'].append(position)
        return books_data
    else:
        return None


def fetch_books_details(book_ids, db_path='../databases/books.db'):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    placeholders = ', '.join('?' for _ in book_ids)
    query = f"SELECT id, author, editor, release, language FROM books WHERE id IN ({placeholders})"
    cursor.execute(query, book_ids)

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


def search_and_display_results():
    while True:
        word_to_search = input("Keyword to search (or press Enter to quit): ")
        if not word_to_search:
            print("Search cancelled.")
            break

        books_data = search_docs_from_db(word_to_search)

        if books_data:
            book_ids = list(books_data.keys())
            print(f"IDs found for word '{word_to_search}': {book_ids}")

            books_details = fetch_books_details(book_ids)

            if books_details:
                print("Books details:")
                for book in books_details:
                    book_id = book['id']
                    frequency = books_data[book_id]['frequency']
                    positions = books_data[book_id]['positions']
                    print(f"\nBook ID: {book_id}")
                    print(f"Author: {book['author']}, Editor: {book['editor']}, Release date: {book['release']}, Language: {book['language']}")
                    print(f"Frequency: {frequency}, Positions: {positions}")
            else:
                print("No books found for the provided IDs.")
            break
        else:
            print(f"No results found for keyword: '{word_to_search}'. Please try a different keyword.")


search_and_display_results()
