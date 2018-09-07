package com.github.vogelb.tools.odem;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import com.github.vogelb.tools.odem.model.DependencyGraph;
import com.github.vogelb.tools.odem.model.DependencyGraph.GraphElement;

/**
 * Simple exporter for .dot files.<br/>
 * Use with https://www.graphviz.org/ DOT to create dependency graphs.
 */
public class DotExporter {
    public static void exportDependencyGraph(DependencyGraph graph, GraphElement[] graphProps, String fileName)
            throws IOException {
        File outFile = new File(fileName);

        String nodeFormat = "\"%s\" [fillcolor=\"#%s\"]";
        String dependencyFormat = "\"%s\" -> \"%s\" [penwidth=%d, color=\"#%s\", label=\"%d\"]";

        try (PrintStream out = new PrintStream(new FileOutputStream(outFile))) {
            out.println("digraph G {");
            out.println("node [shape=box, style=\"rounded,filled\"]");
            out.println("node [fontname=Courier,fontsize=10,fontcolor=white]");

            out.println();

            List<GraphElement> predefined = Arrays.asList(graphProps);
            predefined.forEach(e -> out.println(String.format(nodeFormat, e.name, getColor(e.color))));

            graph.getElements().forEach(e -> {
                if (!predefined.contains(e))
                    out.println(String.format(nodeFormat, e.name, getColor(e.color)));
            });

            out.println();
            graph.getElements().forEach(e -> e.dependencies.forEach(d -> out.println(String.format(dependencyFormat,
                    d.a.name, d.b.name, getPenWidth(d.weight), getColor(d.a.color), d.weight))));

            out.println("}");
        }
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
