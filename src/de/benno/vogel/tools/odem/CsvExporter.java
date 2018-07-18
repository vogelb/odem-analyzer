package de.benno.vogel.tools.odem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import de.benno.vogel.tools.odem.model.Dependency;

public class CsvExporter {
    public static void exportDependencies(List<Dependency> dependencies, final String outputFile, final String packagePrefix) {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            out.println(
                    "Container;TopLevelPackage;Package;Class;DependencyType;DependencyContainer;DependencyTopLevelPackage;DependencyPackage;Dependency");
            for (Dependency dependency : dependencies) {
                String containerName = dependency.getParent().getParent().getShortName();
                String packageName = dependency.getParent().getPackage();
                String topLevelPackage = packageName.startsWith(packagePrefix)
                        ? packageName.substring(packagePrefix.length())
                        : packageName;
                int tlpIndex = topLevelPackage.indexOf('.');
                topLevelPackage = tlpIndex > 0 ? topLevelPackage.substring(0, tlpIndex) : topLevelPackage;
                String dependencyContainer = dependency.getParent().getParent().getShortName();
                String dependencyPackageName = dependency.getPackage();
                String dependencyTopLevelPackage = dependencyPackageName.startsWith(packagePrefix)
                        ? dependencyPackageName.substring(packagePrefix.length())
                        : dependencyPackageName;
                int dTlpIndex = dependencyTopLevelPackage.indexOf('.');
                dependencyTopLevelPackage = dTlpIndex > 0 ? dependencyTopLevelPackage.substring(0, dTlpIndex)
                        : dependencyTopLevelPackage;
                System.out.println(containerName + " :: " + dependency.getParent().getName() + " --> "
                        + dependencyPackageName + " :: " + dependency.getName());
                out.println(containerName + ';' + topLevelPackage + ';' + packageName + ';'
                        + dependency.getParent().getName() + ';' + dependency.getClassification() + ';'
                        + dependencyContainer + ';' + dependencyTopLevelPackage + ';' + dependencyPackageName + ';'
                        + dependency.getName());
            }
            System.out.println("Found " + dependencies.size() + " dependencies for package " + packagePrefix + ".");
        } catch (IOException e) {
            throw new RuntimeException("Error analysing dependencies.", e);
        }
    }
}
