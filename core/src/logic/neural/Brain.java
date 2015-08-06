package logic.neural;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Log;
import com.mygdx.game.Serializer;

/**
 * Represents a virtual brain
 *
 * @author fazo
 */
public class Brain {

    public static final float bias = 0.5f;
    private Neuron[][] neurons;
    private String name;

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
     * Apply a new brain map (mind) to this brain
     *
     * @param brainMap the new brain map to apply
     */
    public void remap(float[][][] brainMap) {
        for (int i = 0; i < brainMap.length; i++) { // for each layer (skip input)
            for (int j = 0; j < brainMap[i].length; j++) { // for each neuron
                // skip input layer
                if (neurons[i + 1][j] == null) {
                    neurons[i + 1][j] = new Neuron(j, bias, this, brainMap[i][j]);
                } else {
                    neurons[i + 1][j].setWeights(brainMap[i][j]);
                }
            }
        }
        recomputeName();
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
        recomputeName();
    }

    /**
     * Draw this brain's status to the screen.
     *
     * @param s the ShapeRenderer to use for the drawing
     */
    public void render(ShapeRenderer s) {
        int sepX = 100, sepY = 50, offset = 100;
        s.set(ShapeRenderer.ShapeType.Filled);
        int neuronHeight = 0;
        for (Neuron[] ns : neurons) {
            if (ns.length > neuronHeight) {
                neuronHeight = ns.length;
            }
        }
        for (int i = 0; i < neurons.length; i++) {
            //s.set(ShapeRenderer.ShapeType.Line);
            for (int j = 0; j < neurons[i].length; j++) {
                // get neuron result first so cache system can kick in and save some calculations
                float nr = neurons[i][j].compute();
                // Draw neuron links
                float[] links = neurons[i][j].getWeights();
                if (links != null) {
                    for (int f = 0; f < links.length; f++) {
                        s.setColor(links[f] < 0 ? links[f] / 2 * -1 : 0, links[f] > 0 ? links[f] / 2 : 0, 0, 1);
                        s.line(i * sepX + offset, j * sepY + offset, (i - 1) * sepX + offset, f * sepY + offset);
                    }
                }
                // Draw neuron
                s.setColor(1 - nr, nr, 0, 1);
                s.circle(i * sepX + offset, j * sepY + offset, 15);
            }
        }
    }

    /**
     * Give some input to the brain
     *
     * @param values the array of input. Its length must match the number of
     * input neurons of this brain
     * @throws Exception if the number of inputs given differs from the number
     * of input neurons of this brain
     */
    public void input(float[] values) throws Exception {
        if (values.length != neurons[0].length) {
            throw new Exception("Not enough or too many inputs");
        }
        for (int i = 0; i < values.length; i++) {
            neurons[0][i].setOutput(values[i]);
        }
    }

    /**
     * Compute output of the brain starting from given input
     *
     * @return an array as long as the number of output neurons, containing the
     * result
     */
    public float[] compute() {
        clearCache(); // unnecessary if already called when changing inputs
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
     * @throws Exception if the number of inputs given differs from the number
     * of input neurons of this brain
     */
    public float[] compute(float[] values) throws Exception {
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
        float[][][] res = new float[neurons.length - 1][][];
        for (int i = 1; i < neurons.length; i++) // layers (skip input layer)
        {
            res[i - 1] = new float[neurons[i].length][];
            for (int j = 0; j < neurons[i].length; j++) // neurons per layer
            {
                res[i - 1][j] = neurons[i][j].getWeights();
            }
        }
        return res;
    }

    /**
     * Get a map of this brain's mind.. with a mutation
     *
     * @param mutationFactor the higher this number, the bigger the mutation
     * @param connectionMutationProbability the probability that determines how
     * many connections mutate in a neuron (from 0 to 1)
     * @param mutationProbability the higher this number the higher the amount
     * of mutated neurons (range: from 0 to 1)
     * @return a mutated brain map of this brain's mind
     */
    public float[][][] getMutatedMap(float mutationProbability, float connectionMutationProbability, float mutationFactor) {
        float[][][] res = new float[neurons.length - 1][][];
        for (int i = 1; i < neurons.length; i++) // layers (skip input layer)
        {
            res[i - 1] = new float[neurons[i].length][];
            for (int j = 0; j < neurons[i].length; j++) // neurons per layer
            {
                if (Math.random() <= mutationProbability) {
                    res[i - 1][j] = neurons[i][j].getMutatedWeights(connectionMutationProbability, mutationFactor);
                }
            }
        }
        return res;
    }

    /**
     * Apply a mutation to this brain
     *
     * @param connectionMutationProbability the probability that determines how
     * many connections mutate in a neuron (from 0 to 1)
     * @param mutationProbability the higher this number the higher the amount
     * of mutated neurons (range: from 0 to 1)
     * @param mutationFactor the higher this number, the bigger the mutation
     */
    public void mutate(float mutationProbability, float connectionMutationProbability, float mutationFactor) {
        for (int i = 1; i < neurons.length; i++) // layers (skip input layer)
        {
            for (int j = 0; j < neurons[i].length; j++) // neurons per layer
            {
                if (Math.random() <= mutationProbability) {
                    neurons[i][j].setWeights(neurons[i][j].getMutatedWeights(connectionMutationProbability, mutationFactor));
                }
            }
        }
    }

    /**
     * Combine this brain with another one's map to get an offspring. The brains
     * must have identical neuron configuration. There are huge amounts of
     * combinations, so you can call multiple times to get different children
     * from the same parents
     *
     * @param map the brain to "breed" with this one
     * @return a child brain from the two brains
     * @throws Exception if the brains don't have identical neuron and layer
     * numbers
     */
    public float[][][] breed(float[][][] map) throws Exception {
        float[][][] res = new float[neurons.length - 1][][];
        if (map.length != neurons.length - 1) {
            throw new Exception("incompatible brains");
        }
        for (int i = 1; i < neurons.length; i++) // layers (skip input layer)
        {
            res[i - 1] = new float[neurons[i].length][];
            if (map[i - 1].length != neurons[i].length) {
                throw new Exception("incompatible brains");
            }
            for (int j = 0; j < neurons[i].length; j++) // neurons per layer
            {
                res[i - 1][j] = new float[neurons[i][j].getWeights().length];
                if (map[i - 1][j].length != neurons[i][j].getWeights().length) {
                    throw new Exception("incompatible brains");
                }
                for (int z = 0; z < neurons[i][j].getWeights().length; z++) // each weight
                {
                    // Combine the two weights
                    if (Math.random() < 0.5) {
                        res[i - 1][j][z] = map[i - 1][j][z];
                    } else {
                        res[i - 1][j][z] = neurons[i][j].getWeights()[z];
                    }
                }
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

    private void recomputeName(){
        name = Serializer.nameBrain(getMap());
    }
    
    /**
     * Returns an array with pointers to all this brain's neurons.
     *
     * @return bidimensional array with first index representing the layer.
     */
    public Neuron[][] getNeurons() {
        return neurons;
    }

    public String getName() {
        return name;
    }
    
}
