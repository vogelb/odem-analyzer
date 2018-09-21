package com.github.vogelb.tools.odem;

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
        String packageFilter = "de.gebit.*";
        // Ignore language dependencies
        String ignorePackageFilter = "(java\\..*)|(de.gebit.trend\\..*)|(de.gebit.pos\\..*)";

        // Set graphic properties for package nodes
        GraphElement[] graphProps = {
        };

        Components.addComponent("poslive.server", "de.gebit.poslive.server", 0);
        Components.addComponent("poslive.client", "de.gebit.poslive.client", 0);
        Components.addComponent("poslive.core", "de.gebit.poslive.core", 0);
        Components.addComponent("poslive.client", "de.gebit.poslive.client", 0);
        Components.addComponent("poslive.kafka", "de.gebit.poslive.kafka", 0);
        Components.addComponent("poslive", "de.gebit.poslive", 1);
        Components.addComponent("gebit", "de.gebit", 1);
        Components.addComponent("compas", "de.gebit.compas", 0);

        try {
            // Use odem file in resources
            InputStream in = Main.class.getClassLoader().getResourceAsStream("odem-analyzer.odem");

            // Load and parse odem file
            DependencyAnalyser analyser = new DependencyAnalyser("POSLive.odem", in);

            // Create dependency graph
            DependencyGraph graph = analyser.filter(packageFilter)
                    .setGraphicProperties(graphProps)
                    .includePackageFilter(packageFilter)
                    .setIncludePackageDependencies(false)
                    .ignorePackageFilter(ignorePackageFilter)
                    .buildGraph();
            // Export graph as graphviz DOT file (issue "dot -Tpng -O
            // odem-analyzer.dot" to create a png image)
            DotExporter.exportDependencyGraph(graph, graphProps, "C:/dev/eclipse/poslive-master/POSLive.dot");

            // Export Outgoing dependencies
            List<Dependency> guiDependencies = analyser.filter("de.gebit.poslive.*")
                    .includePackageFilter(packageFilter)
                    .ignorePackageFilter(ignorePackageFilter)
                    .getDependenciesFrom();
            CsvExporter.exportDependencies(guiDependencies, "C:/dev/eclipse/poslive-master/poslive-dependencies.csv");

            // Export Incoming dependencies for the model namespace
            /*
             * List<Dependency> dependencies = analyser.filter("com.github.vogelb.tools.odem.model.*")
             * .includePackageFilter(packageFilter) .ignorePackageFilter(ignorePackageFilter) .getDependenciesTo();
             * CsvExporter.exportDependencies(dependencies, "odem-analyzer-model-dependents.csv",
             * "com.github.vogelb.tools.odem.*");
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
