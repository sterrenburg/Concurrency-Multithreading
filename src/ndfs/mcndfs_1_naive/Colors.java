package ndfs.mcndfs_1_naive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import graph.State;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class provides a color map for graph states.
 */
public class Colors {

    private final Map<State, Color> map    = new HashMap<State, Color>();
    //public final Map<State, Boolean> pink = new HashMap<State, Boolean>();
    public final Set<State> pink = new HashSet<State>();
    /**
     * Returns <code>true</code> if the specified state has the specified color,
     * <code>false</code> otherwise.
     *
     * @param state
     *            the state to examine.
     * @param color
     *            the color
     * @return whether the specified state has the specified color.
     */
    public boolean hasColor(State state, Color color) {

        // The initial color is white, and is not explicitly represented.
        if (color == Color.WHITE) {
            return map.get(state) == null;
        } else {
            return map.get(state) == color;
        }
    }

    /**
     * Gives the specified state the specified color.
     *
     * @param state
     *            the state to color.
     * @param color
     *            color to give to the state.
     */
    public void color(State state, Color color) {
        if (color == Color.WHITE) {
            map.remove(state);
        } else {
            map.put(state, color);
        }
    }
//    public boolean setPink(State state, Boolean bool) {
//        return pink.put(state, bool) != null;
//    }
//    
//    public boolean getPink(State state) {
//        Boolean contains = pink.get(state);
//        
//        if(contains == null || contains == false) {
//            return false;
//        }
//        
//        return true;
//    }
        public boolean setPink(State state){
            return pink.add(state);   
        }
    
        public boolean getPink(State state){
            return pink.contains(state);
        }
        public int sizePink(){
            return pink.size();
        }
}
