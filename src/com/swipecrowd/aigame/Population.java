package com.swipecrowd.aigame;

import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public class Population {
    @Getter
    final CopyOnWriteArrayList<Dinosaur> dinosaurs;

    public Population(final int size) {
        dinosaurs = new CopyOnWriteArrayList<>();
        IntStream.rangeClosed(1, size)
                .forEach(x -> dinosaurs.add(new Dinosaur()));
    }
}
