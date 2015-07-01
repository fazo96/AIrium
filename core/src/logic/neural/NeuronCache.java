/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.neural;

/**
 *
 * @author fazo
 */
public class NeuronCache {

    private float[] cache;
    private boolean[] validity;

    public NeuronCache(int size) {
        cache = new float[size];
        validity = new boolean[size];
        clear();
    }

    public void put(int index, float value) {
        validity[index] = true;
        cache[index] = value;
    }

    public float get(int index) throws Exception {
        if (validity[index]) {
            return cache[index];
        } else {
            throw new Exception("Value not present");
        }
    }

    public boolean has(int index) {
        return validity[index];
    }

    public void clear() {
        for (int i = 0; i < cache.length; i++) {
            validity[i] = false;
        }
    }
}
