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
    
    private Map<String, Node> elements = new HashMap<>();
    
    public Node addNode(String name, int weight, Color color) {
        Node element = new Node(name, color, weight);
        elements.put(element.name, element);
        return element;
    }
    
    public Node addNode(Node element) {
        elements.put(element.name, element);
        return element;
    }
    
    public void addDependency(Node a, Node b, long weight) {
    	Node aa = elements.get(a.name);
    	if (aa == null) {
    		aa = addNode(a);
    	}
    	Node  bb = elements.get(b.name);
    	if (bb == null) {
    		bb = addNode(b);
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
    
    public Iterable<Node> getElements() {
        return Collections.unmodifiableList(new ArrayList<>(elements.values()));
    }

}
