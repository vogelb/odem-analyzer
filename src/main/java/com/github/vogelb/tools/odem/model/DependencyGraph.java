package com.github.vogelb.tools.odem.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DependencyGraph {
    
    public static class Dependency {
        public Dependency(GraphElement a, GraphElement b, long weight) {
            this.a = a;
            this.b = b;
            this.weight = weight;
        }
        public final GraphElement a;
        public final GraphElement b;
        public final long weight;
    }
    
    public static class GraphElement {
        public String name;
        public Color color;
        public int weight;
        public final List<Dependency> dependencies = new ArrayList<>();
        
        public GraphElement(String name, Color color, int weight) {
            this.name = name;
            this.color = color;
            this.weight = weight;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GraphElement other = (GraphElement) obj;
            return name.equals(other.name);
        }
    }
    
    private List<GraphElement> elements = new ArrayList<>();
    
    public GraphElement addElement(String name, int weight, Color color) {
        GraphElement element = new GraphElement(name, color, weight);
        elements.add(element);
        return element;
    }
    
    public GraphElement addElement(GraphElement element) {
        elements.add(element);
        return element;
    }
    
    public void addDependency(GraphElement a, GraphElement b, long weight) {
        if (!elements.contains(a)) elements.add(a);
        if (!elements.contains(b)) elements.add(b);
        Dependency d = new Dependency(a, b, weight);
        a.dependencies.add(d);
        System.out.println(String.format("Added dependency %s -> %s [%d]", a.name, b.name, weight));
    }
    
    public Iterable<GraphElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

}
