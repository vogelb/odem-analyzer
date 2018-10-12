package com.github.vogelb.tools.odem;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.github.vogelb.tools.odem.model.graph.DependencyGraph;
import com.github.vogelb.tools.odem.model.graph.Node;

/**
 * Simple exporter for .dot files.<br/>
 * Use with https://www.graphviz.org/ DOT to create dependency graphs.
 */
public class DotExporter {
    private static final Random random = new Random();

    public static void exportDependencyGraph(DependencyGraph graph, Node[] graphProps, String fileName)
            throws IOException {
        File outFile = new File(fileName);

        String nodeFormat = "\"%s\" [fillcolor=\"#%s\", fontcolor=\"#%s\"]";
        String dependencyFormat = "\"%s\" -> \"%s\" [penwidth=%d, color=\"#%s\", label=\"%d\"]";

        try (PrintStream out = new PrintStream(new FileOutputStream(outFile))) {
            out.println("digraph G {");
            out.println("node [shape=box, style=\"rounded,filled\"]");
            out.println("node [fontname=Courier,fontsize=10,fontcolor=white]");

            out.println();

            List<Node> predefined = Arrays.asList(graphProps);
            predefined.forEach(e -> out.println(String.format(nodeFormat, e.name, getColor(e.color), getFontColor(e.color))));

            graph.getElements().forEach(e -> {
                if (!predefined.contains(e)) {
                    if (e.color == null) {
                        e.color = getRandomColor();
                    }
                    out.println(String.format(nodeFormat, e.name, getColor(e.color), getFontColor(e.color)));
                }
            });

            out.println();
            
            graph.getElements().forEach(e -> e.dependencies.values().forEach( d -> { 
                if (!d.a.name.equals(d.b.name)) {
                    out.println(String.format(dependencyFormat, d.a.name, d.b.name, getPenWidth(d.weight), getColor(d.a.color), d.weight));
                }
            }));

            out.println("}");
        }
    }
    
    private static String getFontColor(Color color) {
        return getColor(getMatchingFontColor(color));
    }

    private static Color getMatchingFontColor(Color backgroundColor) {
        if (backgroundColor.getRed() + backgroundColor.getGreen() + backgroundColor.getBlue() < 383) {
            return Color.white;
        }
        return Color.black;
    }

    private static Color getRandomColor() {
        // Java 'Color' class takes 3 floats, from 0 to 1.
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        return new Color(r, g, b);
    }

    private static String getColor(Color color) {
        return Integer.toHexString((color.getRGB() & 0xffffff) | 0x1000000).substring(1);
    }

    private static int getPenWidth(long weight) {
        int result = 1;
        if (weight >= 25)
            ++result;
        if (weight >= 50)
            ++result;
        if (weight >= 100)
            ++result;
        if (weight >= 200)
            ++result;
        if (weight >= 400)
            ++result;
        if (weight >= 500)
            ++result;

        return result;
    }
}
