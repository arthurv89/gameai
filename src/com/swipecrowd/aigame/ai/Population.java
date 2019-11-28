package com.swipecrowd.aigame.ai;

import com.swipecrowd.aigame.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class Population {
    @Getter
    ArrayList<Player> pop = new ArrayList<>();
    private final int size;
    private boolean massExtinctionEvent = false;
    ArrayList<Player> genPlayers = new ArrayList<Player>();
    ArrayList<ConnectionHistory> innovationHistory = new ArrayList<ConnectionHistory>();
    private int gen;
    ArrayList<Species> species = new ArrayList<Species>();
    int populationLife = 0;
    int bestScore =0;//the score of the best ever player
    private Player bestPlayer;

    public Population(final int size) {
        this.size = size;
        createPopulation();
    }

    private void createPopulation() {
        IntStream.rangeClosed(1, size)
                .forEach(x -> {
                    final Player player = new Player();
                    pop.add(player);
                });
    }

    public void naturalSelection() {
        speciate();//seperate the population into species
        calculateFitness();//calculate the fitness of each player
        sortSpecies();//sort the species to be ranked in fitness order, best first
        if (massExtinctionEvent) {
            massExtinction();
            massExtinctionEvent = false;
        }
        cullSpecies();//kill off the bottom half of each species
        setBestPlayer();//save the best player of this gen
        killStaleSpecies();//remove species which haven't improved in the last 15(ish) generations
        killBadSpecies();//kill species which are so bad that they cant reproduce


        System.out.printf("generation: %d. Number of mutations: %d. species: %d. <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<%n",
                gen,
                innovationHistory.size(),
                species.size());


        double averageSum = getAvgFitnessSum();
        ArrayList<Player> children = new ArrayList<>();//the next generation
        System.out.println("Species:");
        for (Species value : species) {//for each species

            System.out.println("best unadjusted fitness: " + value.bestFitness);
            for (int i = 0; i < value.players.size(); i++) {
                final Player player = value.players.get(i);
                System.out.println(String.format("player %d fitness: %s score %d ", i, player.fitness, player.score));
            }
            System.out.println();
            children.add(value.champ.clone());//add champion without any mutation

            int NoOfChildren = (int) (Math.floor(value.averageFitness / averageSum * pop.size()) - 1);//the number of children this species is allowed, note -1 is because the champ is already added
            for (int i = 0; i < NoOfChildren; i++) {//get the calculated amount of children from this species
                children.add(value.giveMeBaby(innovationHistory));
            }
        }

        while (children.size() < pop.size()) {//if not enough babies (due to flooring the number of children to get a whole int)
            children.add(species.get(0).giveMeBaby(innovationHistory));//get babies from the best species
        }
        pop.clear();
        pop = new ArrayList<>(children); //set the children as the current population
        gen+=1;
        for (Player player : pop) {//generate networks for each of the children
            player.brain.generateNetwork();
        }

        populationLife = 0;
    }
    void setBestPlayer() {
        Player tempBest =  species.get(0).players.get(0);
        tempBest.gen = gen;


        //if best this gen is better than the global best score then set the global best as the best this gen

        if (tempBest.score > bestScore) {
            genPlayers.add(tempBest.cloneForReplay());
            System.out.println(String.format("old best: %s", bestScore));
            System.out.println(String.format("new best: %s", tempBest.score));
            bestScore = tempBest.score;
            bestPlayer = tempBest.cloneForReplay();
        }
    }


    void speciate() {
        for (Species s : species) {//empty species
            s.players.clear();
        }
        for (int i = 0; i< pop.size(); i++) {//for each player
            boolean speciesFound = false;
            for (Species s : species) {//for each species
                if (s.sameSpecies(pop.get(i).brain)) {//if the player is similar enough to be considered in the same species
                    s.addToSpecies(pop.get(i));//add it to the species
                    speciesFound = true;
                    break;
                }
            }
            if (!speciesFound) {//if no species was similar enough then add a new species with this as its champion
                species.add(new Species(pop.get(i)));
            }
        }
        species.removeIf(species -> species.players.size() == 0);
    }
        //------------------------------------------------------------------------------------------------------------------------------------------
    //calculates the fitness of all of the players
    void calculateFitness() {
        for (int i =1; i<pop.size(); i++) {
            final Player player = pop.get(i);
            player.calculateFitness();
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------
    //sorts the players within a species and the species by their fitnesses
    void sortSpecies() {
        //sort the players within a species
        for (Species s : species) {
            s.sortSpecies();
        }

        //sort the species by the fitness of its best player
        //using selection sort like a loser
        ArrayList<Species> temp = new ArrayList<Species>();
        for (int i = 0; i < species.size(); i ++) {
            double max = 0;
            int maxIndex = 0;
            for (int j = 0; j< species.size(); j++) {
                if (species.get(j).bestFitness > max) {
                    max = species.get(j).bestFitness;
                    maxIndex = j;
                }
            }
            temp.add(species.get(maxIndex));
            species.remove(maxIndex);
            i--;
        }
        species = (ArrayList)temp.clone();
    }
    //------------------------------------------------------------------------------------------------------------------------------------------
    //kills all species which haven't improved in 15 generations
    void killStaleSpecies() {
        for (int i = 2; i< species.size(); i++) {
            if (species.get(i).staleness >= 15) {
                species.remove(i);
                i--;
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------
    //if a species sucks so much that it wont even be allocated 1 child for the next generation then kill it now
    void killBadSpecies() {
        double averageSum = getAvgFitnessSum();

        for (int i = 1; i< species.size(); i++) {
            if (species.get(i).averageFitness/averageSum * pop.size() < 1) {//if wont be given a single child
                species.remove(i);//sad
                i--;
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------
    //returns the sum of each species average fitness
    double getAvgFitnessSum() {
        double averageSum = 0;
        for (Species s : species) {
            averageSum += s.averageFitness;
        }
        return averageSum;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------
    //kill the bottom half of each species
    void cullSpecies() {
        for (Species s : species) {
            s.cull(); //kill bottom half
            s.fitnessSharing();//also while we're at it lets do fitness sharing
            s.setAverage();//reset averages because they will have changed
        }
    }


    void massExtinction() {
        for (int i =5; i< species.size(); i++) {
            species.remove(i);//sad
            i--;
        }
    }

}
