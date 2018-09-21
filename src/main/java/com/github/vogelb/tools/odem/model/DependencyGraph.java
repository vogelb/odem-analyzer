package com.github.vogelb.tools.odem.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DependencyGraph {
    
    public static class Dependency {
        public Dependency(GraphElement a, GraphElement b, long weight) {
            this.a = a;
            this.b = b;
            this.weight = weight;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((a == null) ? 0 : a.hashCode());
            result = prime * result + ((b == null) ? 0 : b.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Dependency other = (Dependency) obj;
            if (a == null) {
                if (other.a != null)
                    return false;
            } else if (!a.equals(other.a))
                return false;
            if (b == null) {
                if (other.b != null)
                    return false;
            } else if (!b.equals(other.b))
                return false;
            return true;
        }
        public final GraphElement a;
        public final GraphElement b;
        public long weight;
    }
    
    public static class GraphElement {
        public String name;
        public Color color;
        public int weight;
        public final Map<Dependency, Dependency> dependencies = new HashMap<>();
        
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
        System.out.println(String.format("Added dependency %s -> %s [%d]", a.name, b.name, weight));
    }
    
    public Iterable<GraphElement> getElements() {
        return Collections.unmodifiableList(new ArrayList<>(elements.values()));
    }

}
