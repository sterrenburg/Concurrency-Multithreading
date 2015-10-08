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
    private State testState; // TODO remove
    private int testCount = 0; // TODO remove
    private Red red;

    public Worker(int i, File promelaFile, State s, Red red, NNDFS nndfs) throws FileNotFoundException {
        //System.out.printf("[%d] Creating\n", threadNumber);
        threadNumber = i;
        this.s = s;
        this.nndfs = nndfs;
        this.graph = GraphFactory.createGraph(promelaFile);
        this.colors = new Colors();
        this.red = red;
    }
    
    //TODO remove
    private void printList(List<State> list) {
        int j = 0;
        for (State i: list) {
            System.out.printf("[%d] %s\n", j, i);
            j ++;
        }   
    }

    private void dfsBlue(State s) throws ResultException {
        if(nndfs.done) {
//            System.out.printf("[%d] done because of flag\n", threadNumber);
            return;
        }
        
        nndfs.counter.putIfAbsent(s, new AtomicCounter());
        
    	//System.out.printf("[%d] dfsBlue\n", threadNumber);
        //System.out.printf("[%d]     no of pink: %d\n         no of red: %d (%d),(%s)\n", threadNumber, colors.pink.size(), red.size(), red.count(), red);

        colors.color(s, Color.CYAN);
        
        for (State t : permute(graph.post(s))) {
            if (colors.hasColor(t, Color.WHITE) && !red.get(t)) {
                dfsBlue(t);
            }
        }
        
        if (s.isAccepting()) {
            
            //TODO check synchronized
            synchronized(this.nndfs){
//                testCount ++;
                nndfs.incrementCount(s);
//                if(testCount == 100) {
//                    testState = s;
//                }
//                
//                if(testCount >= 100 && s == testState) {
                    //System.out.printf("[%d] incr count %d (%s)\n", threadNumber, nndfs.getCount(s).value(), s);
//                }
            }
            
            dfsRed(s);
            
        }
        
        // removed else statement here to conform to alg 2
        colors.color(s, Color.BLUE);
    }
    
    private void dfsRed(State s) throws ResultException {
        
        if(nndfs.done) {
//            System.out.printf("[%d] done because of flag\n", threadNumber);
            return;
        }
        
        //System.out.printf("[%d] dfsRed\n", threadNumber);
        colors.setPink(s, true);
        
        for (State t : permute(graph.post(s))) {
            if (colors.hasColor(t, Color.CYAN)) {
                nndfs.done = true;
//                System.out.printf("[%d] set flag to true\n", threadNumber);
                throw new CycleFoundException();
            } else if (!colors.getPink(t) && !red.get(s)) {
                dfsRed(t);
            }
        }
        
        if(s.isAccepting()) {
            synchronized(this.nndfs){
        	nndfs.decrementCount(s);
//                if(testCount >= 100 && (testState != null)) {
//                    if(s == testState) {
                        //System.out.printf("[%d] decr count %d (%s)\n", threadNumber, nndfs.getCount(s).value(), s);
//                    }
//                }
                
                
            }

        	AtomicCounter c = nndfs.getCount(s);
        	
            int i = 0;
//            int j = c.value();
            
        	while(c.value() > 0) {
        		// spin
                //i ++;
                //if(i % 200 == 0) {
                    //System.out.printf("[%d] spinning on %s\n", threadNumber, s);
                //}
                
                //j = c.value();
        	}
            
            //System.out.printf("[%d] continue (value:)\n", threadNumber);
        }
        //} // from synchronized
        
        red.set(s);
        colors.setPink(s, false);
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
//        System.out.printf("[%d] Running\n", threadNumber);
        
        // TODO catch to Main
        try {
            worker(s);
        } catch (ResultException e) {
            System.out.println(e.getMessage());
            // listener.notify to circumvent the fact that run() can't throw
            // maybe use callable instead of runnable
        }
        
//        System.out.printf("[%d] Exiting\n", threadNumber);
//        System.out.printf("[%d] (red size: %d, red count: %d)\n", threadNumber, red.size(), red.count());
    }
    
    public void start () {
//      System.out.printf("[%d] Starting\n",  threadNumber );

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