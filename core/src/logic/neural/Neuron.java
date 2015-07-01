/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic.neural;

import java.util.ArrayList;

/**
 *
 * @author fazo
 */
public class Neuron {

    private ArrayList<NeuralConnection> inputs;
    private float bias, output;
    private boolean isInputNeuron;
    private int layer;
    private float cachedValue;
    private boolean cachedValueValid = false;

    public Neuron(int layer, float bias) {
        this.layer = layer;
        inputs = new ArrayList<NeuralConnection>();
    }

    public float compute() {
        if (isInputNeuron) {
            return output;
        }
        if (cachedValueValid) {
            return cachedValue;
        }
        float a = bias * -1; // activation
        for (NeuralConnection i : inputs) {
            a += i.compute();
        }
        System.out.println("Computed Value "+a+" for neuron");
        cachedValueValid = true;
        // sigmoid function
        cachedValue = (float) (1 / (1 + Math.pow(Math.E, a * -1)));
        System.out.println("Computed Value "+cachedValue+" for neuron");
        return cachedValue;
    }

    public void mutate(float mutationFactor){
        for(NeuralConnection n : inputs) n.mutate(mutationFactor);
    }
    
    public void setOutput(float output) {
        isInputNeuron = true;
        this.output = output;
    }

    public ArrayList<NeuralConnection> getInputs() {
        return inputs;
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

    public void clearCachedValue() {
        cachedValueValid = false;
        for(NeuralConnection n : inputs) n.clearCachedValue();
    }

}
