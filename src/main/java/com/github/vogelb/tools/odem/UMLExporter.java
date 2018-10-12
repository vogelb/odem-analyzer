package com.github.vogelb.tools.odem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.vogelb.tools.odem.model.Dependency;

public class UMLExporter {
    
    private String nodeViewModel;
    private String linkViewModel;
    private static final Logger logger = LoggerFactory.getLogger(UMLExporter.class);
    
    public UMLExporter(String nodeType, String linkType) {
        nodeViewModel = nodeType;
        linkViewModel = linkType;
    }

    public void exportComponentDiagram(List<Dependency> dependencies, String outputFile, boolean ignoreExternals) {
        Set<String> generatedComponents = new HashSet<>();        
        Set<String> generatedDependencies = new HashSet<>();
        try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<component-diagram version=\"1\" metaclass=\"uml.component_graph\">");
            out.println("<component>");
            out.println("<diagram type=\"ref\" ref=\"./\"/>");
            out.println("  <id type=\"java.lang.String\" value=\"CMPN-" + UUID.randomUUID() + "\"/>");
            out.println("</component>");
            
            dependencies.forEach(d -> {
                String componentName = d.getDependent().getParent().getShortName();
                String componentId="CMPN-" +d.getDependent().getParent().getShortName();
                createComponent(generatedComponents, out, componentName, componentId);
                if (!(ignoreExternals && d.getType().isExternal())) createComponent(generatedComponents, out, d.getType().getParent().getShortName(), "CMPN-" + d.getType().getParent().getShortName());
            });
            
            dependencies.forEach(d -> {
                String componentName=d.getDependent().getParent().getShortName();
                String dependentName=d.getType().getParent().getShortName();
                if (!componentName.equals(dependentName)) {
                    createDependency(generatedDependencies, out, componentName, dependentName);
                }
            });
            out.println("</component-diagram>");
                
        } catch (IOException e) {
            throw new RuntimeException("Error analysing dependencies.", e);
        }
    }

    private void createDependency(Set<String> generatedDepenencies, PrintWriter out, String componentName,
            String dependentName) {
        String key=componentName + "-" + dependentName;
        if (!generatedDepenencies.contains(key)) {
            generatedDepenencies.add(key);
            out.println("<base.link restore-hint=\"dependency\">");
            if (linkViewModel != null) {
                out.println("  <view type=\"" + linkViewModel + "\">");
                out.println("    <bendpoints type=\"java.util.List\"></bendpoints>");
                out.println("    <labelBendpoints type=\"java.util.List\"></labelBendpoints>");
                out.println("    <visibleState type=\"boolean\" value=\"true\"/>");
                out.println("  </view>");
            }
            out.println("  <stereotype type=\"java.lang.String\" value=\"dependency\"/>");
            out.println("  <id type=\"java.lang.String\" value=\"LNK-" + key + "\"/>");
            out.println("  <source type=\"ref\" ref=\"./uml.component=" + componentName + "#CMPN-" + componentName + "\"/>");
            out.println("  <target type=\"ref\" ref=\"./uml.component=" + dependentName + "#CMPN-" + dependentName + "\"/>");
            out.println("</base.link>");
        }
    }

    private void createComponent(Set<String> generatedComponents, PrintWriter out, String componentName,
            String componentId) {
        if (!generatedComponents.contains(componentId)) {
            generatedComponents.add(componentId);
            logger.info("UMLExport: Crate Component {}", componentName);
            out.println("<uml.component>");
            if (nodeViewModel != null) {
                out.println("  <view type=\"" + nodeViewModel + "\">");
                out.println("    <ZOrder type=\"int\" value=\"0\"/>");
                out.println("      <defaultSize type=\"boolean\" value=\"true\"/>)");
                out.println("      <hiddenFlags type=\"int\" value=\"0\"/>");
                out.println("      <location type=\"org.eclipse.draw2d.geometry.Point\" x=\"276\" y=\"48\"/>");
                out.println("      <visibleState type=\"boolean\" value=\"true\"/>");
                out.println("      <layouted type=\"boolean\" value=\"false\"/>");
                out.println("  </view>");
            }
            out.println("  <name type=\"java.lang.String\" value=\"" + componentName + "\"/>");
            out.println("  <id type=\"java.lang.String\" value=\"" + componentId + "\"/>");
            out.println("</uml.component>");
        }
    }

}
