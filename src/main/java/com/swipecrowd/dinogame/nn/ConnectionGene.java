package com.swipecrowd.dinogame.nn;

public class ConnectionGene {
    Node fromNode;
    Node toNode;
    double weight;
    boolean enabled = true;
    int innovationNo;//each connection is given a innovation number to compare genomes
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    ConnectionGene(Node from, Node to, double w, int inno) {
        fromNode = from;
        toNode = to;
        weight = w;
        innovationNo = inno;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //changes the weight
    void mutateWeight() {
        double rand2 = Random2.random();
        if (rand2 < 0.1) {//10% of the time completely change the weight
            weight = Random2.random() * 2 - 1;
        } else {//otherwise slightly change it
            weight += new Random2().nextGaussian()/50;
            //keep weight between bounds
            if(weight > 1){
                weight = 1;
            }
            if(weight < -1){
                weight = -1;

            }
        }
    }

    //----------------------------------------------------------------------------------------------------------
    //returns a copy of this ConnectionGene
    ConnectionGene clone(Node from, Node  to) {
        ConnectionGene clone = new ConnectionGene(from, to, weight, innovationNo);
        clone.enabled = enabled;

        return clone;
    }
}
