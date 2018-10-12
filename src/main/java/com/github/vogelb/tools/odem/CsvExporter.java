package com.github.vogelb.tools.odem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.vogelb.tools.odem.model.Dependency;

/**
 * Export dependencies to CSV format.
 */
public class CsvExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvExporter.class);
    
    public static void exportDependencies(final List<Dependency> dependencies, final Components components, final String outputFile) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            out.println("Container;Component;Package;Class;DependencyType;DependencyContainer;DependencyComponent;DependencyPackage;Dependency");
            for (Dependency dependency : dependencies) {
                String containerName = dependency.getDependent().getParent().getShortName();
                String packageName = dependency.getDependent().getPackage();
                String topLevelPackage = components.getComponent(packageName);
                String dependencyContainer = dependency.getType().getParent().getShortName();
                String dependencyPackageName = dependency.getPackage();
                String dependencyTopLevelPackage = components.getComponent(dependencyPackageName);
                logger.info(containerName + " :: " + dependency.getDependent().getName() + " --> "
                        + dependencyPackageName + " :: " + dependency.getName());
                out.println(containerName + ';' + topLevelPackage + ';' + packageName + ';'
                        + dependency.getDependent().getName() + ';' + dependency.getClassification() + ';'
                        + dependencyContainer + ';' + dependencyTopLevelPackage + ';' + dependencyPackageName + ';'
                        + dependency.getName());
            }
            logger.info("Found {} dependencies.", dependencies.size());
        } catch (IOException e) {
            throw new RuntimeException("Error analysing dependencies.", e);
        }
    }
}
