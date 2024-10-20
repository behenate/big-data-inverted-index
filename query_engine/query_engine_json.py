import json
import sqlite3


def load_from_json(file_path='../json/inverted_index.json'):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            inverted_index = json.load(f)
        return inverted_index
    except IOError as e:
        print(f"An error occurred while reading from JSON: {e}")
        return None


def search_word_in_index(word, inverted_index):
    return inverted_index.get(word, None)


def fetch_books_details(book_ids, db_path='../databases/books.db'):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    placeholders = ', '.join('?' for _ in book_ids)
    query = f"SELECT id, author, editor, release, language FROM books WHERE id IN ({placeholders})"
    cursor.execute(query, book_ids)

    books = cursor.fetchall()
    conn.close()

    books_details = {}
    for book in books:
        book_info = {
            'author': book[1],
            'editor': book[2],
            'release': book[3],
            'language': book[4],
        }
        books_details[str(book[0])] = book_info
    return books_details


def display_results(word, word_data, books_data):
    print(f"Results for word '{word}':")
    for book_id, details in word_data.items():
        book_info = books_data.get(str(book_id), {})
        author = book_info.get("author", "Unknown")
        editor = book_info.get("editor", "Unknown")
        release = book_info.get("release", "Unknown")
        language = book_info.get("language", "Unknown")
        frequency = details.get("frequency", "N/A")
        positions = details.get("position", [])
        print(f"\nBook ID: {book_id}")
        print(f"Author: {author}, Editor: {editor}, Release date: {release}, Language: {language}")
        print(f"Frequency: {frequency}, Positions: {positions}")


def search_and_display():
    inverted_index = load_from_json()
    if not inverted_index:
        print("Failed to load inverted index from JSON.")
        return

    while True:
        word_to_search = input("Keyword to search (or press Enter to quit): ")
        if not word_to_search:
            print("Search cancelled.")
            break

        word_data = search_word_in_index(word_to_search, inverted_index)

        if word_data:
            book_ids = list(map(int, word_data.keys()))
            books_details = fetch_books_details(book_ids)
            display_results(word_to_search, word_data, books_details)
            break
        else:
            print(f"No results found for keyword: '{word_to_search}'. Please try a different keyword.")


if __name__ == "__main__":
    search_and_display()
