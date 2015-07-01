package logic.neural;

import com.mygdx.game.Log;

/**
 * Represents a virtual brain
 *
 * @author fazo
 */
public class Brain {

    public static final float bias = 0.5f;
    private Neuron[][] neurons;

    /**
     * Create a new brain with a random map (mind) with given number of neurons
     *
     * @param nInputs the number of input neurons (at least 1)
     * @param nOutputs the number of output neurons (at least 1)
     * @param hiddenLayers how many hidden layers of neurons (at least 1)
     * @param neuronsPerHiddenLayer how many neurons per hidden layer (at least
     * 1)
     */
    public Brain(int nInputs, int nOutputs, int hiddenLayers, int neuronsPerHiddenLayer) {
        // Prepare brain map
        neurons = new Neuron[hiddenLayers + 2][];
        neurons[0] = new Neuron[nInputs];
        neurons[hiddenLayers + 1] = new Neuron[nOutputs];
        for (int i = 0; i < hiddenLayers; i++) {
            neurons[i + 1] = new Neuron[neuronsPerHiddenLayer];
        }
        // Randomize brain
        initialize();
    }

    /**
     * Create a new brain using given brain map (mind)
     *
     * @param brainMap the brain map (mind) to use
     */
    public Brain(float[][][] brainMap) {
        neurons = new Neuron[brainMap.length][];
        for (int i = 0; i < brainMap.length; i++) { // for each layer
            neurons[i] = new Neuron[brainMap[i].length];
            for (int j = 0; j < brainMap[i].length; j++) { // for each neuron
                neurons[i][j] = new Neuron(i, bias, this, brainMap[i][j]);
            }
        }
    }

    /**
     * Apply a new brain map (mind) to this brain
     *
     * @param brainMap the new brain map to apply
     */
    public void remap(float[][][] brainMap) {
        for (int i = 0; i < brainMap.length; i++) { // for each layer
            for (int j = 0; j < brainMap[i].length; j++) { // for each neuron
                if (neurons[i][j] == null) {
                    neurons[i][j] = new Neuron(j, bias, this, brainMap[i][j]);
                } else {
                    neurons[i][j].setWeights(brainMap[i][j]);
                }
            }
        }
    }

    /**
     * Populate the brain with brand new random neurons
     */
    private void initialize() {
        // init hidden layers
        for (int i = 0; i < neurons.length; i++) {
            for (int j = 0; j < neurons[i].length; j++) {
                // create neuron
                Neuron n = new Neuron(i, bias, this);
                neurons[i][j] = n;
                Log.log(Log.DEBUG, "Adding Layer " + (i + 1) + " Neuron " + (j + 1));
            }
        }
    }

    /**
     * Give some input to the brain
     *
     * @param values the array of input. Its length must match the number of
     * input neurons of this brain
     */
    public void input(float[] values) {
        for (int i = 0; i < values.length; i++) {
            neurons[0][i].setOutput(values[i]);
        }
        clearCache();
    }

    /**
     * Compute output of the brain starting from given input
     *
     * @return an array as long as the number of output neurons, containing the
     * result
     */
    public float[] compute() {
        //clearCache(); // unnecessary if already called when changing inputs
        float[] res = new float[neurons[neurons.length - 1].length];
        for (int i = 0; i < neurons[neurons.length - 1].length; i++) {
            res[i] = neurons[neurons.length - 1][i].compute();
        }
        return res;
    }

    /**
     * Input some values (see input function) and then compute the results.
     *
     * @param values
     * @return the results of the neural network
     */
    public float[] compute(float[] values) {
        input(values);
        return compute();
    }

    /**
     * Get a brainMap that represents this brain's mind
     *
     * @return a tridimensional floating point number array representing a full
     * mind
     */
    public float[][][] getMap() {
        float[][][] res = new float[neurons.length][neurons[1].length][neurons[1].length];
        for (int i = 0; i < neurons.length; i++) // layers
        {
            for (int j = 0; i < neurons[i].length; j++) // neurons per layer
            {
                if (neurons[i][j] == null) {
                    continue;
                }
                res[i][j] = neurons[i][j].getWeights();
            }
        }
        return res;
    }

    /**
     * Get a map of this brain's mind.. with a mutation
     *
     * @param mutationFactor the highest this number, the bigger the mutation
     * @return a mutated brain map of this brain's mind
     */
    public float[][][] getMutatedMap(float mutationFactor) {
        float[][][] res = new float[neurons.length][][];
        for (int i = 0; i < neurons.length; i++) // layers
        {
            res[i] = new float[neurons[i].length][];
            for (int j = 0; j < neurons[i].length; j++) // neurons per layer
            {
                res[i][j] = neurons[i][j].mutate(mutationFactor);
            }
        }
        return res;
    }

    /**
     * Empties the neurons' cache. Needs to be called after changing brain
     * inputs or before computing the result.
     */
    private void clearCache() {
        for (int i = 1; i < neurons.length; i++) {
            for (int j = 0; j < neurons[i].length; j++) {
                neurons[i][j].clearCache();
            }
        }
    }

    /**
     * Returns an array with pointers to all this brain's neurons.
     *
     * @return bidimensional array with first index representing the layer.
     */
    public Neuron[][] getNeurons() {
        return neurons;
    }
}
