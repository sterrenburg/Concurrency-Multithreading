package ndfs.mcndfs_2_improv;
import java.io.PrintStream;
import graph.State;
import java.util.*;

public class Red {
    private Set<State> red = Collections.synchronizedSet(new HashSet<State>());
    
    public boolean set(State state) {
    	return red.add(state);
    }
    
    public boolean get(State state) {
    	return red.contains(state);
    }
    
    public int size() {
        return red.size();
    }
}