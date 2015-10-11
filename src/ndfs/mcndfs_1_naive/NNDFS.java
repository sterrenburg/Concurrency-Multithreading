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
    public volatile boolean done = false;
    
    public int terminated;

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
        terminated = 0;
    }
    
    /**
     * Returns the counter object for a state.
     */
    public AtomicCounter getCount(State s) {
        return counter.get(s);
    }
    
    /**
     * Increments the counter object for a state.
     * A synchronized block is used to avoid race conditions,
     *      since two atomic steps are performed.
     */
    public AtomicCounter incrementCount(State s) {
        synchronized(this){
            AtomicCounter atomicCounter = counter.get(s);
            if(atomicCounter == null) {
                atomicCounter = new AtomicCounter();
                counter.put(s, atomicCounter);
            }

            return atomicCounter.increment();
        }
    }
    
    /**
     * Decrements the counter object for a state.
     */
    public AtomicCounter decrementCount(State s) {
        synchronized(this){
            AtomicCounter atomicCounter = counter.get(s);
            return atomicCounter.decrement();
        }
    }
    
    /**
     * Starts the worker threads and reports whether a cycle has been found.
     */
    private void nndfs(State s) throws ResultException {
        Red red = new Red();
        
        try {
            for(int i = 0; i < nrWorkers; i ++) {
                Worker worker = new Worker(i, promelaFile, s, red, this);
                worker.start();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        
        // Allows for checking the flag functionality
//        System.out.printf("start sleep\n");
//        
//        try {
//            Thread.sleep(3000);                 //1000 milliseconds is one second.
//        } catch(InterruptedException ex) {
//            Thread.currentThread().interrupt();
//        }
//        
//        System.out.printf("done sleep\n");
//        done = true;
        
        synchronized(this) {
            while(terminated != nrWorkers) {
                try {
                    wait();
                } catch(InterruptedException e) {
                    System.out.printf("NNDFS: Exception caught at wait\n");
                }
            }
            
            System.out.printf("NNDFS: done waiting\n");
        }
        
        if(done) {
            throw new CycleFoundException();
        } else {
            throw new NoCycleFoundException();
        }
    }

    @Override
    public void ndfs() throws ResultException {
        nndfs(graph.getInitialState());
    }
}
