package org.engine;

import org.engine.model.BookResult;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter a word you want to find:");
        String userWord = scanner.nextLine();

        MongoQueryEngine mongoQueryEngine = new MongoQueryEngine();
        BookResult[] allResults = mongoQueryEngine.searchForWord(userWord);
        System.out.println("Results for word "+userWord+": ");
        for(BookResult book: allResults){
            System.out.println("Title: "+ book.bookMetadata.title +", author: "+book.bookMetadata.author+" ...");
        }

        scanner.close();
    }
}