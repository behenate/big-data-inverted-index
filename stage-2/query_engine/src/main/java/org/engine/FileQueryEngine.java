package org.engine;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.engine.model.BookInfo;
import org.engine.model.BookMetadata;
import org.engine.model.BookResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FileQueryEngine extends QueryEngine {

    public FileQueryEngine() {
        super();
    }

    public static String getPath(String word) {
        String resourcesPath = "books/";

        String initialLetter = word.substring(0, 1).toLowerCase();
        String directoryPath = resourcesPath + initialLetter + "/";
        if (word.length() > 1) {
            String twoLetters = word.substring(0, 2).toLowerCase();
            directoryPath += twoLetters + "/";
        }
        return directoryPath + word + ".json";
    }

    @Override
    public Map<String, BookInfo> getWordInfo(String word) {
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = getPath(word);

        try {
            File file = new File(filePath);
            if (file.exists()) {
                return objectMapper.readValue(file, new TypeReference<Map<String, BookInfo>>() {});
            }
        } catch (StreamReadException | DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("Error reading data from " + filePath);
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a word you want to find:");
        String userWord = scanner.nextLine();
        FileQueryEngine fileQueryEngine = new FileQueryEngine();
        fileQueryEngine.searchForWord(userWord);

        scanner.close();
    }
}
