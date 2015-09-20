package ndfs.mcndfs_1_naive;

import java.io.File;
import java.io.FileNotFoundException;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import ndfs.CycleFoundException;
import ndfs.NDFS;
import ndfs.NoCycleFoundException;
import ndfs.ResultException;

public class Worker implements Runnable {
    private Thread t;
    private int threadNumber;
    private NNDFS nndfs;
    private State s; //for count test

    public Worker(int i, Graph graph, State s, Colors colors, NNDFS nndfs) {
        threadNumber = i;
        this.s = s; // for count test
        this.nndfs = nndfs;
        System.out.printf("[%d] Creating\n", threadNumber);
        //System.out.printf("pointer to graph: %s\n", Integer.toHexString(System.identityHashCode(graph)));
    }

    public void run() {
        System.out.printf("[%d] Running\n", threadNumber);
        //nndfs.access();
        AtomicCounter c = nndfs.getCount(s);
        if(c == null) {
            System.out.printf("c = null\n");
        } else {
            System.out.printf("c something\n");
        }
        
        System.out.printf("[%d] reading counter: %d\n", threadNumber, nndfs.getCount(s).value());
        nndfs.incrementCount(s);
        //dfsBlue();

        System.out.printf("[%d] Exiting\n", threadNumber);
    }
    
    public void start () {
      System.out.printf("[%d] Starting\n",  threadNumber );

      if (t == null)
      {
         t = new Thread (this, Integer.toString(threadNumber));
         t.start ();
      }
    }
}