package com.swipecrowd.aigame;

import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class Population {
    @Getter
    CopyOnWriteArrayList<Dinosaur> dinosaurs = new CopyOnWriteArrayList<>();
    private final int size;

    public Population(final int size) {
        this.size = size;
        clear();
    }

    void clear() {
        dinosaurs.clear();
        IntStream.rangeClosed(1, size)
                .forEach(x -> dinosaurs.add(new Dinosaur()));
    }
}
