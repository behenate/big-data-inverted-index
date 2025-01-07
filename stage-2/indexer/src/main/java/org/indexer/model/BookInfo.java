package org.indexer.model;

import example.Book;
import example.Update.*;

import java.util.ArrayList;
import java.util.List;

public class BookInfo {
    private List<Integer> positions;
    private Double frequency;

    public BookInfo() {
        this.positions = new ArrayList<>();
        this.frequency = 0.0;
    }

    public BookInfo(ProtoBookInfo protoBookInfo) {
        this.positions = new ArrayList<>(protoBookInfo.getPositionsList());
        this.frequency = protoBookInfo.getFrequency();
    }

    public void addPosition(int position){
        this.positions.add(position);
    }

    public List<Integer> getPositions(){
        return this.positions;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public Double getFrequency() {
        return frequency;
    }

    public ProtoBookInfo toProtoBookInfo() {
        ProtoBookInfo.Builder builder = ProtoBookInfo.newBuilder();
        builder.setFrequency(this.frequency);
        builder.addAllPositions(positions);
        return builder.build();
    }
}