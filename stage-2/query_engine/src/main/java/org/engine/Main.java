package org.engine;

import org.engine.model.BookResult;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a word you want to find:");
        String userWord = scanner.nextLine();

//        FileQueryEngine fileQueryEngine = new FileQueryEngine();
//        fileQueryEngine.searchForWord(userWord);

        MongoQueryEngine mongoQueryEngine = new MongoQueryEngine();
        mongoQueryEngine.searchForWord(userWord);

        scanner.close();
    }
}