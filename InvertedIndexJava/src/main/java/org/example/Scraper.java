package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;

public class Scraper {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/books.db";

    public static void createDatabaseIfNotExists() {
        File dbDirectory = new File("src/main/resources");

        File dbFile = new File(dbDirectory, "books.db");

        if (!dbFile.exists()) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                if (conn != null) {
                    System.out.println("Database has been created: " + dbFile.getAbsolutePath());
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error creating database: " + e.getMessage());
            }
        } else {
            System.out.println("Database already exists.");
        }
    }

    public static void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS Books ("
                + "id INTEGER PRIMARY KEY, "
                + "title TEXT, "
                + "author TEXT, "
                + "editor TEXT, "
                + "release TEXT, "
                + "language TEXT);";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File("src/main/resources/books.db").getAbsolutePath());
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Table has been created or already exists.");
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public static String downloadBookText(int bookId) throws Exception {
        String[] possibleUrls = {
                "https://www.gutenberg.org/cache/epub/" + bookId + "/pg" + bookId + ".txt",
                "https://www.gutenberg.org/cache/epub/" + bookId + "/" + bookId + ".txt",
                "https://www.gutenberg.org/cache/epub/" + bookId + "/pg" + bookId + ".epub",
                "https://www.gutenberg.org/cache/epub/" + bookId + "/" + bookId + ".epub",
                "https://www.gutenberg.org/cache/epub/" + bookId + "/pg" + bookId + ".mobi",
                "https://www.gutenberg.org/cache/epub/" + bookId + "/" + bookId + ".mobi"
        };

        for (String url : possibleUrls) {
            if (urlExists(url)) {
                System.out.println("Downloading from: " + url);
                try {
                    URL bookUrl = new URL(url);
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(bookUrl.openStream()))) {
                        StringBuilder bookText = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            bookText.append(line).append("\n");
                        }
                        return bookText.toString();
                    }
                } catch (IOException e) {
                    System.out.println("Download failed for URL: " + url + " - " + e.getMessage());
                }
            }
        }

        throw new Exception("No valid URL found for book ID " + bookId);
    }

    private static boolean urlExists(String urlString) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection httpUrlConn = (HttpURLConnection) new URL(urlString).openConnection();
            httpUrlConn.setRequestMethod("HEAD");
            return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            return false;
        }
    }

    public static Metadata extractMetadata(String bookText) {
        Metadata metadata = new Metadata();
        String footerIndicator = "*** START OF THE PROJECT GUTENBERG EBOOK";

        String[] lines = bookText.split("\n");
        for (String line : lines) {
            if (line.contains(footerIndicator)) {
                break;
            }

            if (line.contains("Author:") && metadata.author == null) {
                metadata.author = line.split("Author:")[1].trim();
            } else if (line.contains("Editor:") && metadata.editor == null) {
                metadata.editor = line.split("Editor:")[1].trim();
            } else if (line.contains("Release date:") && metadata.release == null) {
                String releaseDate = line.split("Release date:")[1].split("\\[")[0].trim();
                metadata.release = releaseDate;
            } else if (line.contains("Language:") && metadata.language == null) {
                metadata.language = line.split("Language:")[1].trim();
            } else if (line.contains("Title:") && metadata.title == null) {
                metadata.title = line.split("Title:")[1].trim();
            }
        }
        return metadata;
    }

    public static void saveBookToDatabase(int bookId, Metadata metadata) {
        String insertSQL = "INSERT INTO Books (id, title, author, editor, release, language) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + new File("src/main/resources/books.db").getAbsolutePath());
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            pstmt.setInt(1, bookId);
            pstmt.setString(2, metadata.title);
            pstmt.setString(3, metadata.author);
            pstmt.setString(4, metadata.editor);
            pstmt.setString(5, metadata.release);
            pstmt.setString(6, metadata.language);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving to the database: " + e.getMessage());
        }
    }

    public static class Metadata {
        String title;
        String author;
        String editor;
        String release;
        String language;
    }

    public static void downloadAndSaveToDatabase(int bookId) {
        try {
            String bookText = downloadBookText(bookId);
            Metadata metadata = extractMetadata(bookText);

            saveBookToDatabase(bookId, metadata);

        } catch (Exception e) {
            System.out.println("Error downloading book ID " + bookId + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite driver loaded.");
        } catch (ClassNotFoundException e) {
            System.out.println("Error loading SQLite driver: " + e.getMessage());
            return;
        }

        createDatabaseIfNotExists();
        createTable();

        for (int bookId = 501; bookId <= 2500; bookId++) {
            downloadAndSaveToDatabase(bookId);
        }
    }
}
