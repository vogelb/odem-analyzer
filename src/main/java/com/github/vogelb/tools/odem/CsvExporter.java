package com.github.vogelb.tools.odem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.github.vogelb.tools.odem.model.Dependency;
import com.github.vogelb.tools.odem.model.ToplevelPackage;

/**
 * Export dependencies to CSV format.
 */
public class CsvExporter {
    public static void exportDependencies(List<Dependency> dependencies, final String outputFile,
            final ToplevelPackage... tlps) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            out.println("Container;TopLevelPackage;Package;Class;DependencyType;DependencyContainer;DependencyTopLevelPackage;DependencyPackage;Dependency");
            for (Dependency dependency : dependencies) {
                String containerName = dependency.getParent().getParent().getShortName();
                String packageName = dependency.getParent().getPackage();
                String topLevelPackage = PackageUtil.getTopLevelPackage(packageName, tlps);
                String dependencyContainer = dependency.getParent().getParent().getShortName();
                String dependencyPackageName = dependency.getPackage();
                String dependencyTopLevelPackage = PackageUtil.getTopLevelPackage(dependencyPackageName, tlps);
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
