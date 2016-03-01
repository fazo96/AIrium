package logic.neural;

/**
 * Used by neurons to cache inputs for faster NN evaluation performance.
 *
 * @author fazo
 */
public class NeuronCache {

    private double[] cache;
    private double cachedOutput;
    private boolean cachedOutputValid;
    private boolean[] validity;

    /**
     * Create a new empty input cache with given size.
     *
     * @param size how many inputs the requiring neuron has.
     */
    public NeuronCache(int size) {
        cache = new double[size];
        validity = new boolean[size];
        clear();
    }

    /**
     * Put a value in the cache.
     *
     * @param index the index of the value
     * @param value the value itself
     */
    public void put(int index, double value) {
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
    public double get(int index) throws Exception {
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

    public double getCachedOutput() {
        return cachedOutput;
    }

    public boolean hasCachedOutput() {
        return cachedOutputValid;
    }

    public void setCachedOutput(double cachedOutput) {
        this.cachedOutput = cachedOutput;
    }

    /**
     * Clears cache.
     */
    public void clear() {
        for (int i = 0; i < cache.length; i++) {
            validity[i] = false;
        }
        cachedOutputValid = false;
        cachedOutput = 0;
    }
}
