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

public class Worker implements Runnable, NDFS {
    private Thread t;
    private int threadNumber;
    private NNDFS nndfs;
    private State s; //for count test
    private Graph graph;
    
    private final Colors colors;

    public Worker(int i, File promelaFile, State s, Colors colors, NNDFS nndfs) throws FileNotFoundException {
        threadNumber = i;
        this.s = s; // for count test
        this.nndfs = nndfs;
        this.graph = GraphFactory.createGraph(promelaFile);
        this.colors = new Colors();
        System.out.printf("[%d] Creating\n", threadNumber);
        //System.out.printf("pointer to graph: %s\n", Integer.toHexString(System.identityHashCode(graph)));
    }
    
    private void dfsBlue(State s) throws ResultException {
    	System.out.printf("[%d] dfsBlue\n", threadNumber);
    	// check whether initialization took place already
    	if(!nndfs.counter.containsKey(s)) {
    		nndfs.counter.put(s, new AtomicCounter());
    	}

        colors.color(s, Color.CYAN);
        
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.WHITE)) {
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
        	nndfs.incrementCount(s);
            dfsRed(s);
            colors.setRed(s); // global
        } else {
            colors.color(s, Color.BLUE);
        }
    }
    
    private void dfsRed(State s) throws ResultException {
        System.out.printf("[%d] dfsRed\n", threadNumber);
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
                throw new CycleFoundException();
            } else if (colors.hasColor(t, Color.BLUE)) {
                colors.setRed(t);
                dfsRed(t);
            }
        }
        
        if(s.isAccepting()) {
        	nndfs.decrementCount(s);
        	AtomicCounter c = nndfs.getCount(s);
        	
        	while(c.value() > 0) {
        		// spin
        	}
        }
    }

    public void run() {
        System.out.printf("[%d] Running\n", threadNumber);
        //nndfs.access();
//        dfsBlue(s);
//        AtomicCounter c = nndfs.getCount(s);
//        if(c == null) {
//            System.out.printf("c = null\n");
//        } else {
//            System.out.printf("c something\n");
//        }
//        
//        System.out.printf("[%d] reading counter: %d\n", threadNumber, nndfs.getCount(s).value());
//        nndfs.incrementCount(s);
//        //dfsBlue();
        try {
            worker(s);
        } catch (ResultException e) {
            System.out.println(e.getMessage());
        }
        
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
    
    private void worker(State s) throws ResultException {
        dfsBlue(s);
        throw new NoCycleFoundException();
    }
    
    @Override
    public void ndfs() throws ResultException {
        worker(graph.getInitialState());
    }
}