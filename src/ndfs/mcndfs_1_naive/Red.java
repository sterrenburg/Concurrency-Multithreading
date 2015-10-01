package ndfs.mcndfs_1_naive;
import java.io.PrintStream;
import graph.State;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Red {
    
    private volatile ConcurrentLinkedQueue<State> red = new ConcurrentLinkedQueue<State>();
    private volatile int count = 0; // for testing
    
    public Red() {
        //System.out.printf("Red: instance created\n");
    }
    
    public boolean set(State state) {
        //System.out.printf("Red: adding\n");
        count ++;
    	return red.add(state);
    }
    
    public boolean get(State state) {
        //System.out.printf("Red: getting\n");
    	return red.contains(state);
    }
    
    public int size() {
        return red.size();
    }
    
    public int count() {
        return count;
    }
}