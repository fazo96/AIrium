package com.mygdx.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hangles File I/O for AIrium components.
 *
 * @author fazo
 */
public class Serializer {

    public static void saveToFile(File f, String content) {
        PrintWriter fw = null;
        try {
            fw = new PrintWriter(f);
        } catch (IOException ex) {
            Log.log(Log.ERROR, ex.getMessage());
        }
        fw.print(content);
        fw.flush();
        fw.close();
    }

    public static String loadFromFile(File f) {
        String s, a = "";
        try {
            BufferedReader bf = new BufferedReader(new FileReader(f));
            while ((s = bf.readLine()) != null) {
                a += s+"\n";
            }
        } catch (Exception e) {
            Log.log(Log.ERROR, e.getMessage());
            System.out.println(e+"");
        }
        return a;
    }

    public static String serializeBrain(float brainMap[][][]) {
        String s = "# Neural Map for use with AIrium.\n"
                + "# More information at http://github.com/fazo96/AIrium\n"
                + "Layers: " + (brainMap.length+1);
        s += "\nInput Neurons: " + brainMap[0][0].length;
        s += "\n# Layer 1 Skipped because it's the input layer.";
        // layers (input layer not included in brain map)
        for (int i = 0; i < brainMap.length; i++) {
            s += "\nLayer " + (i + 2) + " has " + brainMap[i].length + " neurons";
            for (int j = 0; j < brainMap[i].length; j++) { // neurons
                s += "\nWeights for Layer " + (i + 2) + " Neuron " + (j + 1) + " = ";
                for (int z = 0; z < brainMap[i][j].length; z++) { // connections
                    s += brainMap[i][j][z] + " ";
                }
            }
        }
        return s;
    }

    public static float[][][] loadBrain(String s) {
        float brainMap[][][] = null;
        Log.log(Log.INFO,"Loading brain from String with "+s.split("\n").length+" lines:\n"+s);
        for (String l : s.split("\n")) {
            l = l.trim();
            if (l.isEmpty() || l.startsWith("#")) {
                // Skip comment
            } else if (l.startsWith("Layers: ")) {
                // Set Layer number
                int layers = Integer.parseInt(l.split(" ")[1]) - 1;
                brainMap = new float[layers][][];
                Log.log(Log.INFO, "Loaded NLayers: " + layers);
            } else if (l.startsWith("Input Neurons")) {
                int in = Integer.parseInt(l.split(" ")[2]);
                brainMap[0] = new float[in][0];
                Log.log(Log.INFO, "Loaded NInputNeurons: " + in);
            } else if (l.startsWith("Layer ")) {
                // Set neuron number for given layer
                String ll[] = l.split(" ");
                int layer = Integer.parseInt(ll[1]) - 2;
                int n = Integer.parseInt(ll[3]);
                brainMap[layer] = new float[n][];//[layer>0?brainMap[layer-1].length:0];
            } else if (l.startsWith("Weights ")) {
                // Set weights
                String ll[] = l.split(" ");
                int layer = Integer.parseInt(ll[3]) - 2;
                int neuron = Integer.parseInt(ll[5]) - 1;
                int nWeights = ll.length - 7;
                brainMap[layer][neuron] = new float[nWeights];
                if(layer == 0){
                    Log.log(Log.INFO, "This weightmap is for brains with "+(nWeights)+" input neurons.");
                } else if (nWeights != brainMap[layer - 1].length) {
                    Log.log(Log.ERROR, "WRONG WEIGHT NUMBER: prev layer has "
                            + brainMap[layer - 1].length + " neurons, but only "
                            + (nWeights)
                            + " weights are supplied to this neuron");
                }
                for (int i = 7; i < ll.length; i++) {
                    brainMap[layer][neuron][i - 7] = Float.parseFloat(ll[i]);
                    Log.log(Log.INFO,"Loading L"+layer+"N"+neuron+"W"+(i-7)+" = "+brainMap[layer][neuron][i-7]);
                }
                Log.log(Log.INFO, "Loaded " + (nWeights) + " Weights for Layer " + layer + " Neuron " + neuron);
            }
        }
        return brainMap;
    }
}
