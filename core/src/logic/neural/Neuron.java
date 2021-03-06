package logic.neural;

import com.mygdx.game.Log;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A neuron in some brain. See Brain class.
 *
 * @author fazo
 */
public class Neuron {

    private double[] weights;
    private NeuronCache cache;
    private double bias, output;
    private boolean isInputNeuron;
    private int layer, receivesFromLayer;
    private Brain brain;

    /**
     * Create a neuron with given brain, bias, and at the given layer with 0
     * being the input layer, with random weights
     *
     * @param layer the layer in which this neuron is positioned
     * @param bias the bias of this neuron
     * @param receivesFromLayer the layer to read data from (negative for input
     * neurons)
     * @param brain the brain which contains this neuron
     */
    public Neuron(int layer, int receivesFromLayer, double bias, Brain brain) {
        this(layer, receivesFromLayer, bias, brain, null);
    }

    /**
     * Create a neuron with given brain, bias, and at the given layer with 0
     * being the input layer, with given weights
     *
     * @param layer the layer in which this neuron is positioned
     * @param receivesFromLayer the layer to read data from (negative for input
     * neurons)
     * @param bias the bias of this neuron
     * @param brain the brain which contains this neuron
     * @param weights the weights to use to configure this neuron
     */
    public Neuron(int layer, int receivesFromLayer, double bias, Brain brain, double[] weights) {
        this.brain = brain;
        this.layer = layer;
        this.receivesFromLayer = receivesFromLayer;
        if (receivesFromLayer < 0 || layer == 0) {
            isInputNeuron = true;
        } else if (weights == null) {
            scramble();
        } else {
            this.weights = weights;
        }
        if (!isInputNeuron) {
            cache = new NeuronCache(this.weights.length);
        }
    }

    /**
     * Randomize the weights of this neuron
     */
    private void scramble() {
        // init weights
        if (layer > 0) {
            weights = new double[brain.getNeurons()[receivesFromLayer].length];
        } else { // layer 0 or negative
            isInputNeuron = true;
            weights = new double[0];
        }
        // Put random weights
        for (int i = 0; i < weights.length; i++) {
            weights[i] = (double) (Math.random() * 5 - 2.5f);
        }
    }

    /**
     * Compute the output of this neuron using the previous layer. Does nothing
     * with input neurons. Uses a cache to store the output until it is
     * invalidated by using the clearCache function. This function is recursive,
     * meaning it will calculate all necessary neuron outputs to get this one.
     *
     * @return the output of this neuron.
     */
    public double compute() {
        if (weights == null || weights.length == 0) {
            isInputNeuron = true;
        }
        if (isInputNeuron) {
            return output;
        }
        if (cache.hasCachedOutput()) {
            return cache.getCachedOutput();
        }
        double a = bias * -1; // activation
        for (int i = 0; i < weights.length; i++) {
            if (cache.has(i)) {
                try {
                    a += cache.get(i);
                } catch (Exception ex) {
                    // This should never happen
                    Logger.getLogger(Neuron.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                Neuron n = brain.getNeurons()[receivesFromLayer][i];
                double v = n.compute() * weights[i];
                a += v;
                cache.put(i, v);
            }
        }
        // sigmoid function
        double res = (double) (1 / (1 + Math.pow(Math.E, a * -1)));
        cache.setCachedOutput(res);
        Log.log(Log.DEBUG, "Computed Value " + res + " for neuron");
        return res;
    }

    /**
     * Get a copy of the weights, with a mutation
     *
     * @param mutationProbability controls how many weights actually mutates
     * @param mutationFactor controls how much weights mutate
     * @return the new weights
     */
    public double[] getMutatedWeights(double mutationProbability, double mutationFactor) {
        double[] mutatedWeights = new double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            if (Math.random() <= mutationProbability) {
                mutatedWeights[i] = weights[i] + (double) (Math.random() * mutationFactor) - mutationFactor / 2;
            } else {
                mutatedWeights[i] = weights[i];
            }
        }
        return mutatedWeights;
    }

    /**
     * Use this to manually set the output of input neurons
     *
     * @param output the output you want to set
     */
    public void setOutput(double output) {
        isInputNeuron = true;
        this.output = output;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public boolean isInputNeuron() {
        return isInputNeuron;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public double[] getWeights() {
        return weights;
    }

    /**
     * Change the neuron weights
     *
     * @param weights the new weights to put
     */
    public void setWeights(double[] weights) {
        this.weights = weights;
        // Changing the neuron makes the cache invalid
        clearCache();
    }

    public void clearCache() {
        cache.clear();
    }

}
