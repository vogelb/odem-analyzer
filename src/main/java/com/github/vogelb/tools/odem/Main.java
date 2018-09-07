package com.github.vogelb.tools.odem;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.github.vogelb.tools.odem.model.Dependency;
import com.github.vogelb.tools.odem.model.DependencyGraph;
import com.github.vogelb.tools.odem.model.DependencyGraph.GraphElement;

/**
 * This is a sample main class demonstrating the use of the odem analyzer
 */
public class Main {
    public static void main(String[] args) {

        // Set base package filter.
        String packageFilter = "com.github.vogelb.tools.*";
        // Ignore language dependencies
        String ignorePackageFilter = "java.*";

        // Set graphic properties for package nodes
        GraphElement[] graphProps = {
                new GraphElement("odem", new Color(255, 153, 0), 1),
                new GraphElement("odem.model", new Color(0, 153, 204), 1)
        };

        // Use odem file in resources
        InputStream in = Main.class.getClassLoader().getResourceAsStream("odem-analyzer.odem");

        // Load and parse odem file
        DependencyAnalyser analyser = new DependencyAnalyser("odem-analyzer.odem", in);

        // Create dependency graph
        DependencyGraph graph = analyser.filter(packageFilter)
                .setGraphicProperties(graphProps)
                .includePackageFilter(packageFilter)
                .setIncludePackageDependencies(false)
                .ignorePackageFilter(ignorePackageFilter)
                .buildGraph("com.github.vogelb.tools");
        // Export graph as graphviz DOT file (issue "dot -Tpng -O
        // odem-analyzer.dot" to create a png image)
        try {
            DotExporter.exportDependencyGraph(graph, graphProps, "odem-analyzer.dot");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Export Outgoing dependencies
        List<Dependency> guiDependencies = analyser.filter("com.github.vogelb.tools.*")
                .includePackageFilter(packageFilter)
                .ignorePackageFilter(ignorePackageFilter)
                .getDependenciesFrom();
        CsvExporter.exportDependencies(guiDependencies, "odem-analyzer-dependencies.csv",
                "com.github.vogelb.tools.odem.*");

        // Export Incoming dependencies for the model namespace
        List<Dependency> dependencies = analyser.filter("com.github.vogelb.tools.odem.model.*")
                .includePackageFilter(packageFilter)
                .ignorePackageFilter(ignorePackageFilter)
                .getDependenciesTo();
        CsvExporter.exportDependencies(dependencies, "odem-analyzer-model-dependents.csv",
                "com.github.vogelb.tools.odem.*");
    }
}
