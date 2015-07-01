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
public class NeuralConnection {

    private float weight = 1;

    private final Neuron source;
    private float cachedValue;
    private boolean cachedValueValid = false;

    public NeuralConnection(float weight, Neuron source) {
        this.source = source;
        this.weight = weight;
    }

    public float compute() {
        if (cachedValueValid) {
            return cachedValue;
        }
        // get value from Neuron
        cachedValueValid = true;
        return cachedValue = source.compute() * getWeight();
    }

    public void mutate(float mutationFactor) {
        float mutation = (float) (Math.random() * mutationFactor - mutationFactor/2);
        weight += mutation;
    }
    
    public void clearCachedValue() {
        cachedValueValid = false;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
