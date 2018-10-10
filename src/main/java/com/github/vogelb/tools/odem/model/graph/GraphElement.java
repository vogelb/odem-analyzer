package com.github.vogelb.tools.odem.model.graph;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class GraphElement {
    public String name;
    public Color color;
    public int weight;
    public final Map<Dependency, Dependency> dependencies = new HashMap<>();
    
    public GraphElement(String name, Color color, int weight) {
        this.name = name;
        this.color = color;
        this.weight = weight;
    }
    
    public String toString() {
        return name + " [" + weight + "]";
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