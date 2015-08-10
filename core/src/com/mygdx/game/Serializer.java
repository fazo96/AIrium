package com.mygdx.game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Hangles File I/O for AIrium components.
 *
 * @author fazo
 */
public class Serializer {

    private static final String[] sillabe = {"ba", "de", "ka", "mo", "shi", "du", "ro", "te", "mi", "lo", "pa"};
    private static Map<String, Float> defaults;

    public static String nameBrain(float[][][] brainMap) {
        // Compute a unique representation of the brainmap
        long a = 0;
        for (int i = 0; i < brainMap.length; i++) {
            for (int j = 0; j < brainMap[i].length; j++) {
                for (int z = 0; z < brainMap[i][j].length; z++) {
                    a += brainMap[i][j][z] * i * j * z;
                }
            }
        }
        Random gen = new Random(a);
        String name = "";
        int length = Math.abs(gen.nextInt()) % 5 + 2;
        for (int i = 0; i < length; i++) {
            name += sillabe[Math.abs(gen.nextInt()) % sillabe.length];
        }
        return name;
    }

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
                a += s + "\n";
            }
        } catch (Exception e) {
            Log.log(Log.ERROR, e.getMessage());
            System.out.println(e + "");
        }
        return a;
    }

    public static String serializeSettings(Map<String, Float> options) {
        String a = "# Settings file for use with AIrium.\n"
                + "# More information at http://github.com/fazo96/AIrium\n";
        for (Object o : options.entrySet().toArray()) {
            Map.Entry<String, Float> e = (Map.Entry<String, Float>) o;
            a += e.getKey() + " = " + e.getValue() + "\n";
        }
        return a;
    }

    public static Map<String, Float> readSettings(String fileContent) {
        int line = 0;
        Map<String, Float> m = new HashMap<String, Float>();
        for (String s : fileContent.split("\n")) {
            line++;
            if (s.startsWith("#")) {
                // Skip comment
                continue;
            }
            String[] ss = s.trim().split(" = ");
            try {
                if (ss.length != 2) {
                    throw new Exception("Invalid string at line " + line);
                }
                Log.log(Log.DEBUG, "Loading setting \"" + ss[0].trim() + "\" with value \"" + Float.parseFloat(ss[1].trim()) + "\"");
                m.put(ss[0].trim(), Float.parseFloat(ss[1].trim()));
            } catch (Exception e) {
                Log.log(Log.ERROR, e.getMessage());
            }
        }
        return m;
    }

    public static String serializeBrain(float brainMap[][][]) {
        String s = "# Neural Map for use with AIrium.\n"
                + "# More information at http://github.com/fazo96/AIrium\n"
                + "Layers: " + (brainMap.length + 1);
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
        Log.log(Log.INFO, "Loading brain from String with " + s.split("\n").length + " lines");
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
                if (layer == 0) {
                    Log.log(Log.DEBUG, "This weightmap is for brains with " + (nWeights) + " input neurons.");
                } else if (nWeights != brainMap[layer - 1].length) {
                    Log.log(Log.ERROR, "WRONG WEIGHT NUMBER: prev layer has "
                            + brainMap[layer - 1].length + " neurons, but only "
                            + (nWeights)
                            + " weights are supplied to this neuron");
                }
                for (int i = 7; i < ll.length; i++) {
                    brainMap[layer][neuron][i - 7] = Float.parseFloat(ll[i]);
                    Log.log(Log.DEBUG, "Loading L" + layer + "N" + neuron + "W" + (i - 7) + " = " + brainMap[layer][neuron][i - 7]);
                }
                Log.log(Log.DEBUG, "Loaded " + (nWeights) + " Weights for Layer " + layer + " Neuron " + neuron);
            }
        }
        Log.log(Log.INFO, "Loading complete.");
        return brainMap;
    }

    public static Map<String, Float> getDefaultSettings() {
        if (defaults == null) {
            String s = "corpse_decay_rate = 0.0\n"
                    + "mutationFactor = 1.0\n"
                    + "fps_limit = 60.0\n"
                    + "creature_hp_decay = 0.5\n"
                    + "enable_multithreading = 1.0\n"
                    + "max_ticks = 0.0\n"
                    + "parents_count = 0.0\n"
                    + "draw_view_cones = 0.0\n"
                    + "world_width = 2000.0\n"
                    + "world_height = 2000.0\n"
                    + "number_of_plants = 200.0\n"
                    + "nMutatedNeurons = 0.2\n"
                    + "enable_corpses = 0.0\n"
                    + "nMutatedBrains = 0.5\n"
                    + "nMutatedConnections = 0.5\n"
                    + "number_of_creatures = 15.0\n"
                    + "draw_sight_lines = 0.0\n"
                    + "vegetable_size = 20\n"
                    + "creature_max_hp = 100\n"
                    + "creature_fov = 1.5\n"
                    + "creature_hp_decay = 0.5\n"
                    + "creature_max_speed = 3.0\n"
                    + "creature_hp_for_attacking = 1.0\n"
                    + "creature_hp_for_eating_plants = 1.0\n"
                    + "creature_points_for_eating_plants = 1.0\n"
                    + "creature_points_for_attacking = 2.0\n"
                    + "creature_sight_range = 100.0\n"
                    + "creature_radius = 20.0\n"
                    + "brain_hidden_neurons = 20.0\n"
                    + "brain_hidden_layers = 3.0\n"
                    + "brain_bias = 0.5\n";
            defaults = Serializer.readSettings(s);
        }
        return defaults;
    }
}
