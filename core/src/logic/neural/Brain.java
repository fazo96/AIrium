package logic.neural;

import java.util.ArrayList;

/**
 *
 * @author fazo
 */
public class Brain {

    private ArrayList<Neuron> inputs, outputs, hidden;

    public Brain(int nInputs, int nOutputs, int hiddenLayers, int neuronsPerHiddenLayer) {
        inputs = new ArrayList<Neuron>(nInputs);
        outputs = new ArrayList<Neuron>(nOutputs);
        hidden = new ArrayList<Neuron>(hiddenLayers * neuronsPerHiddenLayer);
        // Create input neurons
        for (int i = 0; i < nInputs; i++) {
            inputs.add(new Neuron(0));
        }
        // popiulate hidden layers
        for (int i = 0; i < hiddenLayers; i++) {
            for (int j = 0; j < neuronsPerHiddenLayer; j++) {
                // create neuron
                Neuron n = new Neuron(i + 1);
                // add connections
                for (Neuron s : inputs) {
                    n.getInputs().add(new NeuralConnection(randWeight(), s));
                }
                hidden.add(n);
                System.out.println("Adding Hidden Layer "+(i+1)+" Neuron "+j+" with "+inputs.size()+" inputs");
            }
        }
        // populate output layer
        for (int i = 0; i < nOutputs; i++) {
            // add neuron
            Neuron n = new Neuron(hiddenLayers + 1);
            int conn = 0;
            for (Neuron s : hidden) {
                // add connections where applicable
                if (s.getLayer() == hiddenLayers) {
                    conn++;
                    n.getInputs().add(new NeuralConnection(randWeight(), s));
                }
            }
            System.out.println("Adding Output Layer Neuron "+i+" with "+conn+" inputs");
            outputs.add(n);
        }
    }
    
    private float randWeight(){
        return (float) Math.random()*2-1f;
    }

    public void input(float[] values) {
        for (int i = 0; i < values.length; i++) {
            inputs.get(i).setOutput(values[i]);
        }
    }

    public float[] compute() {
        for (Neuron n : hidden) {
            n.clearCachedValue();
        }
        float[] res = new float[outputs.size()];
        for (int i=0;i<outputs.size();i++) {
            Neuron n = outputs.get(i);
            n.clearCachedValue();
            res[i] = n.compute();
        }
        return res;
    }

}
