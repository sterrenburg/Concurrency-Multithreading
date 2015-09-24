package ndfs.mcndfs_1_naive;
import java.io.PrintStream;
import graph.State;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Red {
    
    private volatile ConcurrentLinkedQueue<State> red = new ConcurrentLinkedQueue<State>();
    
    public Red() {
        System.out.printf("Red instance created\n");
    }
    
    public boolean set(State state) {
    	return red.add(state);
    }
    
    public boolean get(State state) {
    	return red.contains(state);
    }
}