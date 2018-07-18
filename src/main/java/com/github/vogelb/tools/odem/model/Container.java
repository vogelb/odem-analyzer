package com.github.vogelb.tools.odem.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Container {
	private final String name;
	private final List<Type> types = new ArrayList<Type>();
	
	public String getName() {
		return name;
	}

	public Container(String containerName) {
		name = containerName;
	}
	
	public List<Type> getTypes() {
		return types;
	}
	
	public void addType(Type type) {
		type.setParent(this);
		types.add(type);
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		result.append("Container ").append(name).append("{");
		for (Iterator<Type> i =  types.iterator(); i.hasNext(); ) {
			Type type = i.next();
			result.append(type);
			if (i.hasNext()) result.append(',');
		}
		result.append("}");
		return result.toString();
	}
	
	public List<Dependency> getDependencies() {
		List<Dependency> result = new ArrayList<Dependency>();
		for (Type type : types) {
			result.addAll(type.getDependencies());
		}
		return result;
	}

    public String getShortName() {
        return name.substring(name.lastIndexOf('/') + 1);
    }
}
