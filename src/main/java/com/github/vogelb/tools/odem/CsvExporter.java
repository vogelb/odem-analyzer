package com.github.vogelb.tools.odem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.github.vogelb.tools.odem.model.Dependency;

/**
 * Export dependencies to CSV format.
 */
public class CsvExporter {
    public static void exportDependencies(final List<Dependency> dependencies, final Components components, final String outputFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            out.println("Container;Component;Package;Class;DependencyType;DependencyContainer;DependencyComponent;DependencyPackage;Dependency");
            for (Dependency dependency : dependencies) {
                String containerName = dependency.getParent().getParent().getShortName();
                String packageName = dependency.getParent().getPackage();
                String topLevelPackage = components.getComponent(packageName);
                String dependencyContainer = dependency.getParent().getParent().getShortName();
                String dependencyPackageName = dependency.getPackage();
                String dependencyTopLevelPackage = components.getComponent(dependencyPackageName);
                System.out.println(containerName + " :: " + dependency.getParent().getName() + " --> "
                        + dependencyPackageName + " :: " + dependency.getName());
                out.println(containerName + ';' + topLevelPackage + ';' + packageName + ';'
                        + dependency.getParent().getName() + ';' + dependency.getClassification() + ';'
                        + dependencyContainer + ';' + dependencyTopLevelPackage + ';' + dependencyPackageName + ';'
                        + dependency.getName());
            }
            System.out.println("Found " + dependencies.size() + " dependencies.");
        } catch (IOException e) {
            throw new RuntimeException("Error analysing dependencies.", e);
        }
    }
}
