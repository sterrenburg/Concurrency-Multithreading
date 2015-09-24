/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ndfs.mcndfs_1_naive;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Dalie
 */
class AtomicCounter {
    private volatile AtomicInteger c;
    
    public AtomicCounter() {
         c = new AtomicInteger(0);
    }

    public AtomicCounter increment() {
        c.incrementAndGet();
        return this;
    }

    public AtomicCounter decrement() {
        c.decrementAndGet();
        return this;
    }

    public int value() {
        return c.get();
    }

}
