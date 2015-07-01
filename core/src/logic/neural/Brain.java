package logic.neural;

import com.mygdx.game.Log;
import java.util.ArrayList;

/**
 *
 * @author fazo
 */
public class Brain {

    public static final float bias = 0.5f;
    private Neuron[][] neurons;

    public Brain(int nInputs, int nOutputs, int hiddenLayers, int neuronsPerHiddenLayer) {
        neurons = new Neuron[hiddenLayers + 2][];
        populate(nInputs, nOutputs, hiddenLayers, neuronsPerHiddenLayer);
    }

    private void populate(int nInputs, int nOutputs, int hiddenLayers, int neuronsPerHiddenLayer) {
        // Create input neurons
        neurons[0] = new Neuron[nInputs];
        for (int i = 0; i < nInputs; i++) {
            neurons[0][i] = new Neuron(0, bias, this);
            Log.log(Log.DEBUG, "Adding Input Layer Neuron " + (i + 1));
        }
        // popiulate hidden layers
        for (int i = 0; i < hiddenLayers; i++) {
            neurons[i + 1] = new Neuron[neuronsPerHiddenLayer];
            for (int j = 0; j < neuronsPerHiddenLayer; j++) {
                // create neuron
                Neuron n = new Neuron(i + 1, bias, this);
                neurons[i + 1][j] = n;
                Log.log(Log.DEBUG, "Adding Hidden Layer " + (i + 1) + " Neuron " + (j + 1));
            }
        }
        // populate output layer
        neurons[hiddenLayers + 1] = new Neuron[nOutputs];
        for (int i = 0; i < nOutputs; i++) {
            // add neuron
            Neuron n = new Neuron(hiddenLayers + 1, bias, this);
            neurons[hiddenLayers + 1][i] = n;
            Log.log(Log.DEBUG, "Adding Output Layer Neuron " + (i + 1));
        }
    }

    private float randWeight() {
        return (float) Math.random() * 5 - 2.5f;
    }

    public void input(float[] values) {
        for (int i = 0; i < values.length; i++) {
            neurons[0][i].setOutput(values[i]);
        }
    }

    public float[] compute() {
        clearCache();
        float[] res = new float[neurons[neurons.length - 1].length];
        for (int i = 0; i < neurons[neurons.length - 1].length; i++) {
            Neuron n = neurons[neurons.length - 1][i];
            if (n != null) {
                res[i] = n.compute();
            }
        }
        return res;
    }

    public void map(float[][][] map) {
        // Populate with new neurons
        for (int j = 0; j < map.length; j++) {
            for (int i = 0; i < map[j].length; i++) {
                if (map[j] == null || map[i] == null) {
                    continue;
                }
                neurons[j][i] = new Neuron(j, bias, this);
                neurons[j][i].setWeights(map[j][i]);
            }
        }
    }

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

    public float[][][] mutate(float mutationFactor) {
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

    private void clearCache() {
        for (int i = 1; i < neurons.length; i++) {
            for (int j = 0; j < neurons[i].length; j++) {
                neurons[i][j].clearCache();
            }
        }
    }

    public Neuron[][] getNeurons() {
        return neurons;
    }
}
