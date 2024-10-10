import re


def tokenize(text):
    stop_words = {'a', 'an', 'the', 'and', 'or', 'but', 'if', 'then', 'else', 'when', 'at', 'by', 'for', 'with',
                  'without', 'on', 'is', 'are', 'was', 'were', 'has', 'have', 'had', 'do', 'does', 'did', 'in', 'to',
                  'of', 'it', 'its', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'these', 'those', 'this', 'that',
                  'not', 'no'}

    text = text.lower()
    text = re.sub(r'[^\w\s]', '', text)
    words = text.split()
    filtered_words = [word for word in words if word not in stop_words]
    return filtered_words


def get_all_books(cursor):
    cursor.execute("SELECT id, text FROM books")
    return cursor.fetchall()


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
    return inverted_index
