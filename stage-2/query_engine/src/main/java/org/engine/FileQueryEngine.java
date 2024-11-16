package org.engine;

import com.fasterxml.jackson.core.exc.StreamReadException;
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

public class FileQueryEngine extends QueryEngine {

    public static String getPath(String word) {
        String resourcesPath = "src/main/resources/";

        String initialLetter = word.substring(0, 1).toLowerCase();
        String directoryPath = resourcesPath + initialLetter + "/";
        if (word.length() > 1) {
            String twoLetters = word.substring(0, 2).toLowerCase();
            directoryPath += twoLetters + "/";
        }
        return directoryPath + word + ".json";
    }

    public Map<Integer, BookInfo> getWordInfo(String word) {
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath = getPath(word);

        try {
            File file = new File(filePath);
            if (file.exists()) {
                return objectMapper.readValue(file, Map.class);
            } else {
                System.out.println("File not found for word: " + word);
            }
        } catch (StreamReadException | DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.err.println("Error reading data from " + filePath);
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public List<BookResult> searchForWord(String word) {
        Map<Integer, BookInfo> wordInfo = getWordInfo(word);
        if (wordInfo == null) {
            return null;
        }
        List<BookResult> results = new ArrayList<>();
        for (Integer bookId : wordInfo.keySet()) {
            List<Integer> positions = wordInfo.get(bookId).positions();
            double frequency = wordInfo.get(bookId).frequency();
            BookMetadata metadata = fetchBookMetadata(bookId);
            results.add(new BookResult(positions, frequency, metadata));
        }

        return results;
    }
}
