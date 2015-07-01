package logic.neural;

/**
 * Used by neurons to cache inputs for faster NN evaluation performance.
 *
 * @author fazo
 */
public class NeuronCache {

    private float[] cache;
    private boolean[] validity;

    /**
     * Create a new empty input cache with given size.
     *
     * @param size how many inputs the requiring neuron has.
     */
    public NeuronCache(int size) {
        cache = new float[size];
        validity = new boolean[size];
        clear();
    }

    /**
     * Put a value in the cache.
     *
     * @param index the index of the value
     * @param value the value itself
     */
    public void put(int index, float value) {
        validity[index] = true;
        cache[index] = value;
    }

    /**
     * Read a value from the cache.
     *
     * @param index the index of the value
     * @return the value required
     * @throws Exception if value not stored or declared invalid
     */
    public float get(int index) throws Exception {
        if (validity[index]) {
            return cache[index];
        } else {
            throw new Exception("Value not present");
        }
    }

    /**
     * Returns true if required value is present and valid in the cache.
     *
     * @param index which value to check
     * @return true if has given value
     */
    public boolean has(int index) {
        return validity[index];
    }

    /**
     * Clears cache.
     */
    public void clear() {
        for (int i = 0; i < cache.length; i++) {
            validity[i] = false;
        }
    }
}
