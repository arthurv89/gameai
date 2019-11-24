package com.swipecrowd.aigame;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Population {
    @Getter
    final List<Dinosaur> dinosaurs;

    public Population(final int size) {
        dinosaurs = new ArrayList<>(size);
        IntStream.rangeClosed(1, size)
                .forEach(x -> dinosaurs.add(new Dinosaur()));
    }
}
