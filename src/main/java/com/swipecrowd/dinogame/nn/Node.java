package com.swipecrowd.dinogame.nn;

import lombok.ToString;

import java.util.ArrayList;

import static java.lang.Math.pow;

@ToString
public class Node {
    final int number;
    public int layer;
    private final String name;
    double inputSum = 0;
    final ArrayList<ConnectionGene> outputConnections = new ArrayList<ConnectionGene>();
    public double outputValue;

    public Node(final int number, final int layer, final String name) {
        this.number = number;
        this.layer = layer;
        this.name = name;
    }

    public void engage() {
        if (layer!=0) {//no sigmoid for the inputs and bias
            outputValue = sigmoid(inputSum);
        }

        for (int i = 0; i< outputConnections.size(); i++) {//for each connection
            if (outputConnections.get(i).enabled) {//dont do shit if not enabled
                outputConnections.get(i).toNode.inputSum += outputConnections.get(i).weight * outputValue;//add the weighted output to the sum of the inputs of whatever node this node is connected to
            }
        }
    }

    double sigmoid(double x) {
        return 1 / (1 + pow(Math.E, -4.9*x));
    }

    boolean isConnectedTo(Node node) {
        if (node.layer == layer) {//nodes in the same layer cannot be connected
            return false;
        }

        //you get it
        if (node.layer < layer) {
            for (int i = 0; i < node.outputConnections.size(); i++) {
                if (node.outputConnections.get(i).toNode == this) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < outputConnections.size(); i++) {
                if (outputConnections.get(i).toNode == node) {
                    return true;
                }
            }
        }

        return false;
    }

    public Node clone() {
        return new Node(number, layer, name);
    }
}
