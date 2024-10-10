import string

import requests
from multiprocessing import Pool
import json
import re
import sqlite3


def extract_metadata(book_text: str) -> object:
    metadata = {
        "author": None,
        "editor": None,
        "release": None,
        "language": None
    }

    footer_indicator = "*** START OF THE PROJECT"

    lines = book_text.split('\n')
    for line in lines:
        if footer_indicator in line:
            break  # Stop processing once we reach the end of the actual content

        if 'Author:' in line and not metadata["author"]:
            metadata["author"] = line.split('Author:')[1].strip()
        elif 'Editor:' in line and not metadata["editor"]:
            metadata["editor"] = line.split('Editor:')[1].strip()
        elif 'Release date:' in line and not metadata["release"]:
            release_date = line.split('Release date:')[1].split('[')[0].strip()
            metadata["release"] = release_date
        elif 'Language:' in line and not metadata["language"]:
            metadata["language"] = line.split('Language:')[1].strip()
    return metadata


def extractBook(text, footer_indicator = "*** START OF THE PROJECT GUTENBERG EBOOK"):
    if text:
        match = re.search(f"{re.escape(footer_indicator)}.*$", text, re.MULTILINE)

        if match:
            # Find the starting index of the next line after matching the line with the footer_indicator
            start_index = match.end() + 1
            # Extract text from this index to the end of the string
            return text[start_index:].strip()
        else:
            # Return an empty string if the footer indicator line is not found
            return ""
    return ""


def download_gutenberg_text(url):
    """
    This function downloads a plain text file from a Project Gutenberg URL.
    """
    response = requests.get(url)
    if response.status_code == 200:
        # The text is successfully retrieved from the server
        return response.text
    else:
        # Something went wrong with the request (e.g. 404 error, etc.)
        return None


def download(book_id: int) -> str:
    book_url = f"https://www.gutenberg.org/cache/epub/{book_id}/pg{book_id}.txt"
    return download_gutenberg_text(book_url)

def download_and_save(book_id: int):
    text = download(book_id)
    book_text = extractBook(text)
    book_metadata = extract_metadata(text)
    if book_text:
        # Save the content to a local file
        with open(f'../databases/{book_id}.txt', 'w', encoding='utf-8') as file:
            file.write(book_text)
        print(f"Book {book_id} downloaded and saved successfully.")
    else:
        print("Failed to download the book.")

    if book_metadata:
        # Save the content to a local file
        book_metadata_text = json.dumps(book_metadata)
        with open(f'../databases/{book_id}.json', 'w', encoding='utf-8') as file:
            file.write(book_metadata_text)
        print("Book downloaded and saved successfully.")
    else:
        print(f"Failed to download the book {book_id} metadata.")


def create_database():
    conn = sqlite3.connect('../databases/books.db')
    c = conn.cursor()
    c.execute('''
        CREATE TABLE IF NOT EXISTS books (
            id INTEGER PRIMARY KEY,
            text TEXT,
            author TEXT,
            editor TEXT,
            release TEXT,
            language TEXT
        )
    ''')

    conn.commit()
    conn.close()

def download_and_save_to_db(book_id: int):
    text = download(book_id)
    if text == None:
        return
    book_text = extractBook(text)
    book_metadata = extract_metadata(text)

    # Connect to the SQLite database
    conn = sqlite3.connect('../databases/books.db')
    c = conn.cursor()

    if book_text and book_metadata:
        # Prepare the data tuple
        data = (
            book_id,
            book_text,
            book_metadata.get('author'),
            book_metadata.get('editor'),
            book_metadata.get('release'),
            book_metadata.get('language')
        )

        # Insert the data into the books table
        c.execute('''
            INSERT INTO books (id, text, author, editor, release, language)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', data)

        # Save (commit) the changes
        conn.commit()
        print(f"Book {book_id} and its metadata downloaded and saved successfully in the database.")
    else:
        print("Failed to download the book or extract metadata.")


    # Close the connection to the database
    conn.close()


"""
Downloads a batch of books and saves them into the books directory. 
from_id - start id - inclusive
to_id - end id - non-inclusive
"""
def download_batch(from_id: int, to_id: int, pool_size: int = 20):
    ids = range(from_id, to_id)
    with Pool(processes=pool_size) as pool:
        pool.map(download_and_save_to_db, ids)

if __name__ == "__main__":
    create_database()
    download_batch(0, 50, 100)


