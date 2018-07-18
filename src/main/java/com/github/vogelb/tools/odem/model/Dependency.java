package com.github.vogelb.tools.odem.model;

public class Dependency {
	private Type parent;
	private final String name;
	private final String classification;

	public Type getParent() {
		return parent;
	}

	public void setParent(Type parent) {
		this.parent = parent;
	}
	
	public String getName() {
		return name;
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
