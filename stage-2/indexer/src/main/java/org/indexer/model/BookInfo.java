package org.indexer.model;

import java.util.ArrayList;
import java.util.List;

public class BookInfo {
    private List<Integer> positions;
    private double frequency;

    public BookInfo() {
        this.positions = new ArrayList<>();
        this.frequency = 0.0;
    }


    public void addPosition(int position){
        this.positions.add(position);
    }

    public List<Integer> getPositions(){
        return this.positions;
    }


}