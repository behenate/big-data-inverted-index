package org.indexer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.indexer.model.BookInfo;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileIndexer extends Indexer {

    public FileIndexer(){
        super();
    }

    @Override
    void save() {
        ObjectMapper objectMapper = new ObjectMapper();
        String resourcesPath = "src/main/resources/";

        for (Map.Entry<String, Map<Integer, BookInfo>> entry : this.invertedIndex.entrySet()) {
            String word = entry.getKey();
            String initialLetter = word.substring(0, 1).toLowerCase();
            String directoryPath = resourcesPath + initialLetter + "/";
            if(word.length() > 1){
                String twoLetters = word.substring(0, 2).toLowerCase();
                directoryPath += twoLetters + "/";
            }
            String filePath = directoryPath + word + ".json";

            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            try {
                objectMapper.writeValue(new File(filePath), entry.getValue());
                System.out.println("Data saved to " + filePath);
            } catch (IOException e) {
                System.err.println("Error saving data to " + filePath);
            }
        }
    }

    public static void main(String[] args) {
        Indexer fileIndexer = new FileIndexer();
        fileIndexer.indexBooks();
    }
}
