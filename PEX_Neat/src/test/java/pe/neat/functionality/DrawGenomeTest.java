package pe.neat.functionality;

import pe.Random;
import pe.neat.Connection;
import pe.neat.Counter;
import pe.neat.Genome;
import pe.neat.Node;

public class DrawGenomeTest
{
    public static void main(String[] args)
    {
        NeatTest.setup(DrawGenomeTest::setup);
        
        NeatTest.run("DrawGenomeTest");
    }
    
    private static void setup(Random random, Genome genome, Counter nodeInnovation, Counter connInnovation)
    {
        random.setSeed(100);
    
        for (int i = 0; i < 3; i++)
        {
            genome.addNode(new Node(nodeInnovation.inc(), Node.Type.INPUT, 0));
        }
        genome.addNode(new Node(nodeInnovation.inc(), Node.Type.OUTPUT, 2));
        genome.addNode(new Node(nodeInnovation.inc(), Node.Type.HIDDEN, 1));
    
        genome.addConnection(new Connection(connInnovation.inc(), 0, 3, 1.0, true));
        genome.addConnection(new Connection(connInnovation.inc(), 1, 3, 1.0, false));
        genome.addConnection(new Connection(connInnovation.inc(), 2, 3, 1.0, true));
        genome.addConnection(new Connection(connInnovation.inc(), 1, 4, 1.0, true));
        genome.addConnection(new Connection(connInnovation.inc(), 4, 3, 1.0, true));
        genome.addConnection(new Connection(connInnovation.inc(), 0, 4, 1.0, true));
        // genome.addConnection(new Connection(connInnovation.inc(), genome.getNode(2), genome.getNode(4), 1.0, true));
    }
}
