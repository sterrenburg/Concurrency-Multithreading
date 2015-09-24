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
    private State s;
    private Graph graph;
    
    private final Colors colors;
    private Red red;

    public Worker(int i, File promelaFile, State s, Red red, NNDFS nndfs) throws FileNotFoundException {
        System.out.printf("[%d] Creating\n", threadNumber);
        threadNumber = i;
        this.s = s;
        this.nndfs = nndfs;
        this.graph = GraphFactory.createGraph(promelaFile);
        this.colors = new Colors();
        
        //red = new Red();
        this.red = red;
    }
    
    private void dfsBlue(State s) throws ResultException {
    	System.out.printf("[%d] dfsBlue\n", threadNumber);
        System.out.printf("[%d]     no of pink: %d\n         no of red: %d (%d),(%s)\n", threadNumber, colors.pink.size(), red.size(), red.count(), red);
    	// check whether initialization took place already
    	if(!nndfs.counter.containsKey(s)) {
    		nndfs.counter.put(s, new AtomicCounter());
    	}

        colors.color(s, Color.CYAN);
        
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.WHITE) && !red.get(t)) {
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
        	//nndfs.incrementCount(s);
            dfsRed(s);
//            red.set(s);
            // need counter here
        }
        
        // removed else statement here to conform to alg 2
        colors.color(s, Color.BLUE);
    }
    
    private void dfsRed(State s) throws ResultException {
        System.out.printf("[%d] dfsRed\n", threadNumber);
        colors.setPink(s, true);
        
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
                throw new CycleFoundException();
            //} else if (colors.hasColor(t, Color.BLUE)) {
            } else if (!colors.getPink(t) && !red.get(s)) {
//                red.set(t);
                dfsRed(t);
            }
        }
        
        if(s.isAccepting()) {
        	nndfs.decrementCount(s);
        	AtomicCounter c = nndfs.getCount(s);
        	
//        	while(c.value() > 0) {
//        		// spin
//        	}
        }
        
        red.set(s);
        colors.setPink(s, false);
    }

    public void run() {
        System.out.printf("[%d] Running\n", threadNumber);
        
        // TODO catch to Main
        try {
            worker(s);
        } catch (ResultException e) {
            System.out.println(e.getMessage());
            // listener.notify to circumvent the fact that run() can't throw
            // maybe use callable instead of runnable
        }
        
        System.out.printf("[%d] Exiting\n", threadNumber);
        System.out.printf("[%d] (red size: %d, red count: %d)\n", threadNumber, red.size(), red.count());
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