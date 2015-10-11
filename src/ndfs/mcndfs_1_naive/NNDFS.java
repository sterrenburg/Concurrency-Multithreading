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

public class NNDFS implements NDFS {
    
    private int nrWorkers;
    private File promelaFile;
    private Graph graph;
    
    public volatile Map<State, AtomicCounter> counter;
    public volatile boolean cycleFound = false;
    
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
        System.out.printf("mcnndfs: %d\n", nrWorkers); //TODO remove
        this.nrWorkers = nrWorkers;
        this.promelaFile = promelaFile;
        this.graph = GraphFactory.createGraph(promelaFile);
        counter = new ConcurrentHashMap<State, AtomicCounter>();
        terminated = 0;
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
            
            return atomicCounter.increment();
        }
    }
    
    public AtomicCounter decrementCount(State s) {
        synchronized(this){
            AtomicCounter atomicCounter = counter.get(s);
            return atomicCounter.decrement();
        }
    }
    
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
        
        synchronized(this) {
            while(terminated != nrWorkers) {
                try {
                    wait();
                } catch(InterruptedException e) {
                    System.out.println(e);
                }
            }
        }
        
        System.out.printf("done waiting\n"); //TODO remove
        
        if(cycleFound) {
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
