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

/**
 * This is a straightforward implementation of Figure 1 of
 * <a href="http://www.cs.vu.nl/~tcs/cm/ndfs/laarman.pdf">
 * "the Laarman paper"</a>.
 *
 * This class should be modified/extended to implement Figure 2 of this paper.
 */
public class NNDFS implements NDFS {

    private final Graph graph;
    private final Colors colors = new Colors();
    private int nrWorkers;
    
    public volatile Map<State, AtomicCounter> counter;

    /**
     * Constructs an NDFS object using the specified Promela file.
     *
     * @param promelaFile
     *            the Promela file.
     * @param nrWorkers
     *            the number of worker threads to use.
     * @throws FileNotFoundException
     *             is thrown in case the file could not be read.
     */
    public NNDFS(File promelaFile, int nrWorkers) throws FileNotFoundException {
        System.out.printf("mcnndfs: %d\n", nrWorkers);
        this.nrWorkers = nrWorkers;
        this.graph = GraphFactory.createGraph(promelaFile);
        counter = new ConcurrentHashMap<State, AtomicCounter>();
    }
    
    public AtomicCounter getCount(State s) {
        return counter.get(s);
    }
    
    public AtomicCounter incrementCount(State s) {
        AtomicCounter atomicCounter = counter.get(s);
        
        if(atomicCounter == null) {
            atomicCounter = new AtomicCounter();
            counter.put(s, atomicCounter);
        }
        
        if(atomicCounter == null) {
            System.out.printf("Error: AtomicCounter null\n");
        }
        
        return atomicCounter.increment();
    }
    
    public AtomicCounter decrementCount(State s) {
        AtomicCounter atomicCounter = counter.get(s);
        
        if(atomicCounter == null) {
            System.out.printf("Error: AtomicCounter null\n");
        }
        
        return atomicCounter.decrement();
    }

    private void dfsRed(State s) throws ResultException {

        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
                throw new CycleFoundException();
            } else if (colors.hasColor(t, Color.BLUE)) {
                colors.setRed(t);
                dfsRed(t);
            }
        }
        
        if(s.isAccepting()) {
        	counter.put(s, counter.get(s).decrement());
        	AtomicCounter c = counter.get(s);
        	
        	while(c.value() > 0) {
        		// spin
        	}
        }
    }

    private void dfsBlue(State s) throws ResultException {
    	System.out.printf("mcndfs doing dfsBlue\n");
    	// check whether initialization took place already
    	if(!counter.containsKey(s)) {
    		counter.put(s, new AtomicCounter());
    	}

        colors.color(s, Color.CYAN);
        
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.WHITE)) {
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
        	counter.put(s, counter.get(s).increment());
            dfsRed(s);
            colors.setRed(s);
        } else {
            colors.color(s, Color.BLUE);
        }
    }
    
    public void access(State s) {
        System.out.printf("nndfs access function\n");
        AtomicCounter c = new AtomicCounter();
        counter.put(s, c);
    }

    private void nndfs(State s) throws ResultException {
        //dfsBlue(s);
        this.access(s);
        // run nrWorkers workers on dfsBlue
        for(int i = 0; i < nrWorkers; i ++) {
            //System.out.printf("Creating worker [%d]\n", i);
            Worker worker = new Worker(i, graph, s, colors, this);
            worker.start();
        }
        
        
        
        throw new NoCycleFoundException();
    }

    @Override
    public void ndfs() throws ResultException {
        nndfs(graph.getInitialState());
    }
}
