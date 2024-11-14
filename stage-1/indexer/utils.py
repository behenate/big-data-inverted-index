import re

stop_words = {'a', 'an', 'the', 'and', 'or', 'but', 'if', 'then', 'else', 'when', 'at', 'by', 'for', 'with',
              'without', 'on', 'is', 'are', 'was', 'were', 'has', 'have', 'had', 'do', 'does', 'did', 'in', 'to',
              'of', 'it', 'its', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'these', 'those', 'this', 'that',
              'not', 'no'}

def tokenize(text):
    text = text.lower()
    text = re.sub(r'[^\w\s]', '', text)
    words = text.split()
    return words


def get_all_books(cursor):
    cursor.execute("SELECT * FROM books")
    return cursor.fetchall()


def index_documents(db):
    inverted_index = dict()
    cursor = db.cursor()
    books = get_all_books(cursor)
    for book in books:
        book_id = book[0]
        text = book[1]
        book_metadata = {
            "title": str(book[2]),
            "author": str(book[3]),
            "editor": str(book[4]),
            "release": str(book[5]),
            "language": str(book[6])
        }
        tokenized_text = tokenize(text)
        word_count = len(tokenized_text)
        position = -1
        position_key = "position"

        # {"word": {"book1": {position: [1, 3, 4, 5, 6], frequency: 3}, "book2": {position:[3,4,5,6], frequency: 4}}}

        for word in tokenized_text:
            position += 1
            if word in stop_words:
                continue
            if word not in inverted_index:
                inverted_index[word] = {}
            if book_id not in inverted_index[word].keys():
                inverted_index[word][book_id] = {position_key: [], "frequency": 0, "bookMetadata": book_metadata}
            inverted_index[word][book_id][position_key].append(position)

        for (word, books_per_word) in inverted_index.items():
            if word in stop_words:
                continue
            for b_id in books_per_word:
                frequency = len(inverted_index[word][b_id][position_key]) / word_count
                inverted_index[word][b_id]["frequency"] = frequency
    return inverted_index
