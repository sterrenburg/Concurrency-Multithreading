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
    
    private int nrWorkers;
    private File promelaFile;
    private Graph graph;
    
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
        this.promelaFile = promelaFile;
        this.graph = GraphFactory.createGraph(promelaFile);
        counter = new ConcurrentHashMap<State, AtomicCounter>();
    }
    
    public AtomicCounter getCount(State s) {
        return counter.get(s);
    }
    
    public AtomicCounter incrementCount(State s) {
        synchronized(this){
        AtomicCounter atomicCounter = counter.get(s);
        
        if(atomicCounter == null) {
            atomicCounter = new AtomicCounter();
            counter.put(s, atomicCounter);
        }
        
        
//        if(atomicCounter == null) {
//            // TODO exception
//            System.out.printf("Error: AtomicCounter null\n");
//        }
        
        return atomicCounter.increment();
        }
    }
    
    public AtomicCounter decrementCount(State s) {
        synchronized(this){
        AtomicCounter atomicCounter = counter.get(s);
        
//        if(atomicCounter == null) {
//            // TODO exception
//            System.out.printf("Error: AtomicCounter null\n");
//        }
//        
        return atomicCounter.decrement();
        }
    }
    
    // testing
//    public void access(State s) {
//        System.out.printf("nndfs access function\n");
//        AtomicCounter c = new AtomicCounter();
//        counter.put(s, c);
//        c.increment();
//        System.out.println("Incr count:"+c.value());
//        this.incrementCount(s);
//        System.out.println("is the mth incrementcount working?"+this.getCount(s).value());
//
//    }

    private void nndfs(State s) throws ResultException {
        //dfsBlue(s);
        //this.access(s);
        // run nrWorkers workers on dfsBlue
//        for(int i = 0; i < nrWorkers; i ++) {
//            //System.out.printf("Creating worker [%d]\n", i);
//            Worker worker = new Worker(i, promelaFile, s, this);
//            worker.start();
//        }
        
        try {
            for(int i = 0; i < nrWorkers; i ++) {
                //System.out.printf("Creating worker [%d]\n", i);
                Worker worker = new Worker(i, promelaFile, s, this);
                worker.start();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        
        throw new NoCycleFoundException();
    }

    @Override
    public void ndfs() throws ResultException {
        nndfs(graph.getInitialState());
    }
}
