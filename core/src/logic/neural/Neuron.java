/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.neural;

import com.mygdx.game.Log;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fazo
 */
public class Neuron {

    private float[] weights;
    private NeuronCache cache;
    private float bias, output;
    private boolean isInputNeuron;
    private int layer;
    private Brain brain;

    public Neuron(int layer, float bias, Brain brain) {
        this(layer, bias, brain, null);
    }

    public Neuron(int layer, float bias, Brain brain, float[] weights) {
        this.brain = brain;
        this.layer = layer;
        if (weights == null) {
            scramble();
        } else {
            this.weights = weights;
        }
        cache = new NeuronCache(this.weights.length);
    }

    private void scramble() {
        // init weights
        if (layer > 1) {
            weights = new float[brain.getNeurons()[layer - 1].length];
        } else if (layer == 1) {
            weights = new float[brain.howManyInputNeurons()];
        } else { // layer 0
            isInputNeuron = true;
            weights = new float[0];
        }
        // Put random weights
        for (int i = 0; i < weights.length; i++) {
            weights[i] = (float) (Math.random() * 5 - 2.5f);
        }
    }

    public float compute() {
        if (isInputNeuron) {
            return output;
        }
        float a = bias * -1; // activation
        for (int i = 0; i < weights.length; i++) {
            //if(brain == null) System.out.println("BRAINS NULL"); else if(brain.getNeurons() == null) System.out.println("NEURONS NULL");
            //System.out.println(Arrays.toString(brain.getNeurons()));
            if (cache.has(i)) {
                try {
                    return cache.get(i);
                } catch (Exception ex) {
                    // This should never happen
                    Logger.getLogger(Neuron.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Neuron n = brain.getNeurons()[layer - 1][i];
            a += n.compute() * weights[i];
        }
        // sigmoid function
        float res = (float) (1 / (1 + Math.pow(Math.E, a * -1)));
        Log.log(Log.DEBUG, "Computed Value " + res + " for neuron");
        return res;
    }

    public float[] mutate(float mutationFactor) {
        float[] mutatedWeights = new float[weights.length];
        for (int i = 0; i < weights.length; i++) {
            mutatedWeights[i] = weights[i] + mutationFactor - mutationFactor / 2;
        }
        return mutatedWeights;
    }

    public void setOutput(float output) {
        isInputNeuron = true;
        this.output = output;
    }

    public float getBias() {
        return bias;
    }

    public void setBias(float bias) {
        this.bias = bias;
    }

    public boolean isIsInputNeuron() {
        return isInputNeuron;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public float[] getWeights() {
        return weights;
    }

    public void setWeights(float[] weights) {
        this.weights = weights;
    }

    public void clearCache() {
        cache.clear();
    }

}
