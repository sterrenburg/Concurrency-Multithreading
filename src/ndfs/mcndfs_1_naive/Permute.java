package ndfs.mcndfs_1_naive;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Permute {
    private List<Integer> list;
    
    public Permute() {
        System.out.printf("Permute created\n");
        
        list = new ArrayList<Integer>();
        
        for(int i = 0; i < 10; i ++) {
            list.add(i * 10);
        }
        
        List<Integer> permute1 = new ArrayList<Integer>(list);
        
        permute1 = permute(permute1);
        
        System.out.printf("original:\n");
        printList(list);
        System.out.printf("\nnew:\n");
        printList(permute1);
    }
    
    private List<Integer> permute(List<Integer> list) {
        List<Integer> result = new ArrayList<Integer>();
        Random randomGenerator = new Random();
        
        while(list.size() > 0) {
            int randomInt = randomGenerator.nextInt(list.size());
            result.add(list.remove(randomInt));
            System.out.printf("added\n");
        }
        
        return result;
    }
    
    private void printList(List<Integer> list) {
        int j = 0;
        for (int i:list) {
            System.out.printf("[%d] %d\n", j, i);
            j ++;
        }   
    }
}