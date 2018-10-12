package com.github.vogelb.tools.odem.model;

public class Dependency {
    private Type type;
    private Type dependent;
    private String name;
    private final String classification;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getDependent() {
        return dependent;
    }

    public void setDependent(Type parent) {
        this.dependent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackage() {
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return name;
    }

    public Dependency(String dependencyName, String classification) {
        name = dependencyName;
        this.classification = classification;
    }

    public String getClassification() {
        return classification;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Dependency ").append(name).append('[').append(classification).append(']');
        return result.toString();
    }

}
