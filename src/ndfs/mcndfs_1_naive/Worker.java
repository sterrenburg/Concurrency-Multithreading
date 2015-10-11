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
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Worker implements Runnable, NDFS{
    private Thread t;
    private int threadNumber;
    private NNDFS nndfs;
    private State s;
    private Graph graph;
    private final Colors colors;
    private Red red;
    
    public Worker(int i, File promelaFile, State s, Red red, NNDFS nndfs) throws FileNotFoundException {
        threadNumber = i;
        this.s = s;
        this.nndfs = nndfs;
        this.graph = GraphFactory.createGraph(promelaFile);
        this.colors = new Colors();
        this.red = red;
    }
    
    private void dfsBlue(State s) throws ResultException {
        nndfs.counter.putIfAbsent(s, new AtomicCounter());
       
        if(nndfs.cycleFound) {
            return;
        }
        
        colors.color(s, Color.CYAN);
        
        for (State t : permute(graph.post(s))) {
            if (colors.hasColor(t, Color.WHITE) && !red.get(t)) {
                if(!nndfs.cycleFound) {
                    dfsBlue(t);
                }
            }
        }
        
        if (s.isAccepting()) {
            synchronized(this.nndfs){
                nndfs.incrementCount(s);
            }
            
            if(!nndfs.cycleFound) {
                dfsRed(s);
            }
            
        }
        
        colors.color(s, Color.BLUE);
    }
    
    private void dfsRed(State s) throws ResultException {
        colors.setPink(s);
        for (State t : permute(graph.post(s))) {
            if (colors.hasColor(t, Color.CYAN)) {
                synchronized(nndfs) {
                    if(!nndfs.cycleFound) {
                        nndfs.cycleFound = true;
                        nndfs.notifyAll();
                        throw new CycleFoundException();
                    }
                }
            } else if (!colors.getPink(t) && !red.get(s) && !nndfs.cycleFound) {
                dfsRed(t);
            }
        }
        
        if(s.isAccepting()) {
            synchronized(this.nndfs){
                nndfs.decrementCount(s);
            
            AtomicCounter c = nndfs.getCount(s);
            if(c.value()==0){
                this.nndfs.notifyAll();
            }
                
            while(c.value() > 0 && !nndfs.cycleFound) {
                try{
                this.nndfs.wait();
                    } catch(InterruptedException e){
                }
            
                }
            }
        }
        
        red.set(s);
        colors.setPink(s);
    }
    
    private List<State> permute(List<State> list) {
        List<State> result = new ArrayList<State>();
        Random randomGenerator = new Random();
        
        while(list.size() > 0) {
            int randomInt = randomGenerator.nextInt(list.size());
            result.add(list.remove(randomInt));
        }
        
        return result;
    }
    
    public void run() {
        try {
            worker(s);
        } catch (ResultException e) {
            
        }
        
        synchronized(nndfs) {
            nndfs.terminated ++;
            nndfs.notify();
        }
    }
    
    public void start () {
        if (t == null)
        {
            t = new Thread (this, Integer.toString(threadNumber));
            t.start ();
        }
    }
    
    private void worker(State s) throws ResultException {
        dfsBlue(s);
        if(!nndfs.cycleFound) {
            throw new NoCycleFoundException();
        }
    }
    
    @Override
    public void ndfs() throws ResultException {
        worker(graph.getInitialState());
    }
}