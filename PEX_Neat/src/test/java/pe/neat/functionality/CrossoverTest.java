package pe.neat.functionality;

import pe.Random;
import pe.neat.*;

public class CrossoverTest
{
    static GenomeDrawer drawer = new GenomeDrawer();
    
    public static void main(String[] args)
    {
        NeatTest.setup(CrossoverTest::setup);
        NeatTest.test(CrossoverTest::test);
        
        // drawer.nodeSpacing = 50;
        NeatTest.run("CrossoverTest", 3, drawer);
    }
    
    private static void setup(Random random, Genome[] genome, Counter[] nodeInnovation, Counter[] connInnovation)
    {
        random.setSeed(1337);
    
        for (int i = 0; i < 3; i++)
        {
            genome[0].addNode(new Node(nodeInnovation[0].inc(), Node.Type.INPUT, 0));
        }
        genome[0].addNode(new Node(nodeInnovation[0].inc(), Node.Type.OUTPUT, 2));
        genome[0].addNode(new Node(nodeInnovation[0].inc(), Node.Type.HIDDEN, 1));
    
        genome[0].addConnection(new Connection(connInnovation[0].inc(), 0, 3, 1.0, true));
        genome[0].addConnection(new Connection(connInnovation[0].inc(), 1, 3, 1.0, false));
        genome[0].addConnection(new Connection(connInnovation[0].inc(), 2, 3, 1.0, true));
        genome[0].addConnection(new Connection(connInnovation[0].inc(), 1, 4, 1.0, true));
        genome[0].addConnection(new Connection(connInnovation[0].inc(), 4, 3, 1.0, true));
        genome[0].addConnection(new Connection(connInnovation[0].inc(), 0, 4, 1.0, true));
    
        for (int i = 0; i < 3; i++)
        {
            genome[1].addNode(new Node(nodeInnovation[1].inc(), Node.Type.INPUT, 0));
        }
        genome[1].addNode(new Node(nodeInnovation[1].inc(), Node.Type.OUTPUT, 3));
        genome[1].addNode(new Node(nodeInnovation[1].inc(), Node.Type.HIDDEN, 1));
        genome[1].addNode(new Node(nodeInnovation[1].inc(), Node.Type.HIDDEN, 2));
    
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 0, 3, -0.5, true));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 1, 3, -0.5, false));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 2, 3, -0.5, true));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 1, 4, -0.5, true));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 4, 3, -0.5, false));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 4, 5, -0.5, true));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 5, 3, -0.5, true));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 2, 4, -0.5, true));
        genome[1].addConnection(new Connection(connInnovation[1].inc(), 0, 5, -0.5, true));
    }
    
    private static void test(Random random, Genome[] genome, Counter[] nodeInnovation, Counter[] connInnovation)
    {
        genome[2] = Genome.crossover(random, genome[0], genome[1], 0.10);
    }
}
