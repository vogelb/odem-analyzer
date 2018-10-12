package com.github.vogelb.tools.odem.model.graph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyGraph {

    private static final Logger logger = LoggerFactory.getLogger(DependencyGraph.class);
    
    private Map<String, GraphElement> elements = new HashMap<>();
    
    public GraphElement addElement(String name, int weight, Color color) {
        GraphElement element = new GraphElement(name, color, weight);
        elements.put(element.name, element);
        return element;
    }
    
    public GraphElement addElement(GraphElement element) {
        elements.put(element.name, element);
        return element;
    }
    
    public void addDependency(GraphElement a, GraphElement b, long weight) {
    	GraphElement aa = elements.get(a.name);
    	if (aa == null) {
    		aa = addElement(a);
    	}
    	GraphElement  bb = elements.get(b.name);
    	if (bb == null) {
    		bb = addElement(b);
    	}
        Dependency d = new Dependency(aa, bb, weight);
        Dependency existing = aa.dependencies.get(d);
        if (existing != null) {
            existing.weight += d.weight;
        } else {
            aa.dependencies.put(d, d);
        }
        logger.info("Added dependency %s -> %s [%d]", a.name, b.name, weight);
    }
    
    public Iterable<GraphElement> getElements() {
        return Collections.unmodifiableList(new ArrayList<>(elements.values()));
    }

}
