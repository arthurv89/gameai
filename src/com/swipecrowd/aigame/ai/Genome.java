package com.swipecrowd.aigame.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.swipecrowd.aigame.Action;
import com.swipecrowd.aigame.Emulation;
import com.swipecrowd.aigame.JumpAction;
import com.swipecrowd.aigame.NullAction;
import com.swipecrowd.aigame.Obstacle;
import com.swipecrowd.aigame.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Genome {
    private static final int inputs = 8;
    private static final int outputs = 2;
    private static int layers = 2;
    private static int biasNode = inputs;

    ArrayList<ConnectionGene> genes = new ArrayList<>();//a list of connections between nodes which represent the NN

    @Getter
    @VisibleForTesting
    private final List<Node> nodes;
    private ArrayList<Node> network = new ArrayList<>();//a list of the nodes in the order that they need to be considered in the NN
    private int nextConnectionNo = 1000;

    public Genome() {
        nodes = createNodes();
        generateNetwork();
    }

    public void generateNetwork() {
        connectNodes();
        network = new ArrayList<>();
        //for each layer add the node in that layer, since layers cannot connect to themselves there is no need to order the nodes within a layer

        //for each layer
        IntStream.range(0, layers).forEach(l -> {
            for (Node node : nodes) {//for each node
                if (node.layer == l) {//if that node is in that layer
                    network.add(node);
                }
            }
        });
    }

    private void connectNodes() {
        for (Node node : nodes) {//clear the connections
            node.outputConnections.clear();
        }

        for (ConnectionGene gene : genes) {//for each ConnectionGene
            gene.fromNode.outputConnections.add(gene);//add it to node
        }
    }

    private ImmutableList<Node> createNodes() {
        return Streams.concat(
                createInputNodes(),
                Stream.of(createBiasNode()),
                createOutputNodes()
        ).collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
    }
    private Stream<Node> createInputNodes() {
        return intStream(inputs).map(j -> new Node(j, 0, "Input node " + j));
    }

    private Node createBiasNode() {
        return new Node(inputs, 0, "Bias node");
    }


    private Stream<Node> createOutputNodes() {
        return intStream(outputs).map(j -> new Node(j + inputs + 1, 1, "Output node " + j));
    }

    private Stream<Integer> intStream(final int genomeInputs) {
        return IntStream.rangeClosed(0, genomeInputs-1).boxed();
    }

    public Action calculateAction(Player dino, final Emulation emulation) {
        //get the output of the neural network
        final double[] decision = feedForward(getVision(dino, emulation));

        double max = Integer.MIN_VALUE;
        Integer maxIndex = null;
        for (int i = 0; i < decision.length; i++) {
            if (decision[i] > max) {
                max = decision[i];
                maxIndex = i;
            }
        }

        switch(maxIndex) {
            case 0:
                return new NullAction();
        }
        return new JumpAction();
    }

    private double[] getVision(final Player dino, final Emulation emulation) {
        final double speed = Emulation.X_SPEED;
        final Optional<Obstacle> closestObstacle = emulation.getObstacles().stream()
                .min((x, y) -> (int) (x.getXPos() - y.getXPos()));
        return new double[] {
            speed,
            dino.getYPos(),
            dino.isJumping() ? 1 : 0,
            closestObstacle.isPresent() ? 1 : 0,
            closestObstacle.map(x -> x.getXPos()).orElse(0.0),
            closestObstacle.map(x -> x.getYPos()).orElse(0.0),
            closestObstacle.map(x -> x.getHeight()).orElse(0.0),
            closestObstacle.map(x -> x.getWidth()).orElse(0.0)
        };
    }


//    Action calculateAction(final Dinosaur dino) {
//        if(Math.random() < 0.5) {
//            return new JumpAction();
//        } else {
//            return new NullAction();
//        }
//    }

    double[] feedForward(double[] inputValues) {
        Preconditions.checkArgument(inputValues.length == inputs);

        //set the outputs of the input nodes
        for (int i =0; i < inputs; i++) {
            nodes.get(i).outputValue = inputValues[i];
        }
        nodes.get(biasNode).outputValue = 1;//output of bias is 1

        //for each node in the network engage it(see node class for what this does)
        network.forEach(Node::engage);

        //the outputs are nodes[inputs] to nodes [inputs+outputs-1]
        double[] outs = new double[outputs];
        for (int i = 0; i < outputs; i++) {
            outs[i] = nodes.get(inputs + i).outputValue;
        }

        for (Node node : nodes) {//reset all the nodes for the next feed forward
            node.inputSum = 0;
        }

        return outs;
    }

    void mutate(ArrayList<ConnectionHistory> innovationHistory) {
        if (genes.size() ==0) {
            addConnection(innovationHistory);
        }

        double rand1 = Math.random();
        if (rand1<0.8) { // 80% of the time mutate weights
            genes.forEach(ConnectionGene::mutateWeight);
        }
        //5% of the time add a new connection
        double rand2 = Math.random();
        if (rand2<0.08) {
            addConnection(innovationHistory);
        }


        //1% of the time add a node
        double rand3 = Math.random();
        if (rand3<0.02) {
            addNode(innovationHistory);
        }
    }

    //mutate the NN by adding a new node
    //it does this by picking a random connection and disabling it then 2 new connections are added
    //1 between the input node of the disabled connection and the new node
    //and the other between the new node and the output of the disabled connection
    void addNode(ArrayList<ConnectionHistory> innovationHistory) {
        //pick a random connection to create a node between
        if (genes.size() ==0) {
            addConnection(innovationHistory);
            return;
        }
        int randomConnection = (int) Math.floor(Math.random() * genes.size());

        while (genes.get(randomConnection).fromNode == nodes.get(biasNode) && genes.size() !=1 ) {//dont disconnect bias
            randomConnection = (int) Math.floor(Math.random() * genes.size());
        }

        genes.get(randomConnection).enabled = false;//disable it

        final Node newNode = new Node(nodes.size(), 0, "New Node"); // Layer will be set later.
        nodes.add(newNode);
        //add a new connection to the new node with a weight of 1
        int connectionInnovationNumber = getInnovationNumber(innovationHistory, genes.get(randomConnection).fromNode, newNode);
        genes.add(new ConnectionGene(genes.get(randomConnection).fromNode, newNode, 1, connectionInnovationNumber));


        connectionInnovationNumber = getInnovationNumber(innovationHistory, newNode, genes.get(randomConnection).toNode);
        //add a new connection from the new node with a weight the same as the disabled connection
        genes.add(new ConnectionGene(newNode, genes.get(randomConnection).toNode, genes.get(randomConnection).weight, connectionInnovationNumber));
        newNode.layer = genes.get(randomConnection).fromNode.layer +1;


        connectionInnovationNumber = getInnovationNumber(innovationHistory, nodes.get(biasNode), newNode);
        //connect the bias to the new node with a weight of 0
        genes.add(new ConnectionGene(nodes.get(biasNode), newNode, 0, connectionInnovationNumber));

        //if the layer of the new node is equal to the layer of the output node of the old connection then a new layer needs to be created
        //more accurately the layer numbers of all layers equal to or greater than this new node need to be incrimented
        if (newNode.layer == genes.get(randomConnection).toNode.layer) {
            for (int i = 0; i< nodes.size() -1; i++) {//dont include this newest node
                if (nodes.get(i).layer >= newNode.layer) {
                    nodes.get(i).layer ++;
                }
            }
            layers++;
        }
        connectNodes();
    }

    void addConnection(ArrayList<ConnectionHistory> innovationHistory) {
        //cannot add a connection to a fully connected network
        if (fullyConnected()) {
            System.out.println("connection failed");
            return;
        }


        //get random nodes
        int randomNode1 = (int) Math.floor(Math.random() * nodes.size());
        int randomNode2 = (int) Math.floor(Math.random() * nodes.size());
        while (randomConnectionNodesAreShit(randomNode1, randomNode2)) {//while the random nodes are no good
            //get new ones
            randomNode1 = (int) Math.floor(Math.random() * nodes.size());
            randomNode2 = (int) Math.floor(Math.random() * nodes.size());
        }
        int temp;
        if (nodes.get(randomNode1).layer > nodes.get(randomNode2).layer) {//if the first random node is after the second then switch
            temp = randomNode2  ;
            randomNode2 = randomNode1;
            randomNode1 = temp;
        }

        //get the innovation number of the connection
        //this will be a new number if no identical genome has mutated in the same way
        int connectionInnovationNumber = getInnovationNumber(innovationHistory, nodes.get(randomNode1), nodes.get(randomNode2));
        //add the connection with a random array

        genes.add(new ConnectionGene(nodes.get(randomNode1), nodes.get(randomNode2), Math.random()*2-1, connectionInnovationNumber));//changed this so if error here
        connectNodes();
    }

    int getInnovationNumber(ArrayList<ConnectionHistory> innovationHistory, Node from, Node to) {
        boolean isNew = true;
        int connectionInnovationNumber = nextConnectionNo;
        for (int i = 0; i < innovationHistory.size(); i++) {//for each previous mutation
            if (innovationHistory.get(i).matches(this, from, to)) {//if match found
                isNew = false;//its not a new mutation
                connectionInnovationNumber = innovationHistory.get(i).innovationNumber; //set the innovation number as the innovation number of the match
                break;
            }
        }

        if (isNew) {//if the mutation is new then create an arrayList of integers representing the current state of the genome
            //set the innovation numbers
            ArrayList<Integer> innoNumbers = genes.stream()
                    .map(gene -> gene.innovationNo)
                    .collect(Collectors.toCollection(ArrayList::new));

            //then add this mutation to the innovationHistory
            innovationHistory.add(new ConnectionHistory(from.number, to.number, connectionInnovationNumber, innoNumbers));
            nextConnectionNo++;
        }
        return connectionInnovationNumber;
    }

    boolean fullyConnected() {
        int maxConnections = 0;
        int[] nodesInLayers = new int[layers];//array which stored the amount of nodes in each layer

        //populate array
        nodes.forEach(node -> nodesInLayers[node.layer] += 1);

        //for each layer the maximum amount of connections is the number in this layer * the number of nodes infront of it
        //so lets add the max for each layer together and then we will get the maximum amount of connections in the network
        for (int i = 0; i < layers-1; i++) {
            int nodesInFront = 0;
            for (int j = i+1; j < layers; j++) {//for each layer infront of this layer
                nodesInFront += nodesInLayers[j];//add up nodes
            }

            maxConnections += nodesInLayers[i] * nodesInFront;
        }

        //if the number of connections is equal to the max number of connections possible then it is full
        return maxConnections == genes.size();
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    boolean randomConnectionNodesAreShit(int r1, int r2) {
        if (nodes.get(r1).layer == nodes.get(r2).layer) return true; // if the nodes are in the same layer
        if (nodes.get(r1).isConnectedTo(nodes.get(r2))) return true; //if the nodes are already connected



        return false;
    }


    //---------------------------------------------------------------------------------------------------------------------------------
    //called when this Genome is better that the other parent
    public Genome crossover(Genome parent2) {
        Genome child = new Genome(true);
        child.genes.clear();
        child.nodes.clear();
        child.layers = layers;
        child.biasNode = biasNode;
        ArrayList<ConnectionGene> childGenes = new ArrayList<>();//list of genes to be inherrited form the parents
        ArrayList<Boolean> isEnabled = new ArrayList<Boolean>();
        //all inherrited genes
        for (int i = 0; i< genes.size(); i++) {
            boolean setEnabled = true;//is this node in the chlid going to be enabled

            final ConnectionGene gene = genes.get(i);
            int parent2gene = matchingGene(parent2, gene.innovationNo);
            if (parent2gene != -1) {//if the genes match
                if (!gene.enabled || !parent2.genes.get(parent2gene).enabled) {//if either of the matching genes are disabled

                    if (Math.random() < 0.75) {//75% of the time disabel the childs gene
                        setEnabled = false;
                    }
                }
                double rand = Math.random();
                if (rand<0.5) {
                    childGenes.add(gene);

                    //get gene from this fucker
                } else {
                    //get gene from parent2
                    childGenes.add(parent2.genes.get(parent2gene));
                }
            } else {//disjoint or excess gene
                childGenes.add(gene);
                setEnabled = gene.enabled;
            }
            isEnabled.add(setEnabled);
        }


        //since all excess and disjoint genes are inherrited from the more fit parent (this Genome) the childs structure is no different from this parent | with exception of dormant connections being enabled but this wont effect nodes
        //so all the nodes can be inherrited from this parent
        for (int i = 0; i < nodes.size(); i++) {
            child.nodes.add(nodes.get(i).clone());
        }

        //clone all the connections so that they connect the childs new nodes
        for ( int i =0; i<childGenes.size(); i++) {
            final ConnectionGene childGene = childGenes.get(i);
            final Node fromNode = child.getNode(childGene.fromNode.number);
            final Node toNode = child.getNode(childGene.toNode.number);
            child.genes.add(childGene.clone(fromNode, toNode));
            child.genes.get(i).enabled = isEnabled.get(i);
        }

        child.connectNodes();
        return child;
    }

    private Node getNode(final int number) {
        return getNodes().get(number);
    }

    //----------------------------------------------------------------------------------------------------------------------------------------
    //create an empty genome
    Genome(boolean crossover) {
        nodes = new ArrayList<>();
    }
    //----------------------------------------------------------------------------------------------------------------------------------------
    //returns whether or not there is a gene matching the input innovation number  in the input genome
    int matchingGene(Genome parent2, int innovationNumber) {
        return IntStream.range(0, parent2.genes.size())
                .filter(i -> parent2.genes.get(i).innovationNo == innovationNumber)
                .findFirst()
                .orElse(-1);
        //no matching gene found
    }
    //----------------------------------------------------------------------------------------------------------------------------------------
    //prints out info about the genome to the console
    void printGenome() {
        System.out.printf("Print genome  layers: %d%n", layers);
        System.out.println("bias node: "  + biasNode);
        System.out.println("nodes");
        nodes.stream().map(node -> node.number + ",").forEach(System.out::println);
        System.out.println("Genes");
        //for each ConnectionGene
        genes.forEach(gene -> System.out.printf("gene %d From node %d To node %dis enabled %s from layer %d to layer %d weight: %s%n", gene.innovationNo, gene.fromNode.number, gene.toNode.number, gene.enabled, gene.fromNode.layer, gene.toNode.layer, gene.weight));

        System.out.println();
    }

    //----------------------------------------------------------------------------------------------------------------------------------------
    //returns a copy of this genome
    public Genome clone() {
        Genome clone = new Genome(true);

        for (int i = 0; i < nodes.size(); i++) {//copy nodes
            clone.nodes.add(nodes.get(i).clone());
        }

        //copy all the connections so that they connect the clone new nodes

        for ( int i =0; i<genes.size(); i++) {//copy genes
            clone.genes.add(genes.get(i).clone(clone.getNode(genes.get(i).fromNode.number), clone.getNode(genes.get(i).toNode.number)));
        }

        clone.layers = layers;
        clone.biasNode = biasNode;
        clone.connectNodes();

        return clone;
    }
}
