package com.github.vogelb.tools.odem.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class Type {
	private Container parent;
	private final String name;
	private final List<Dependency> dependencies = new ArrayList<Dependency>();

	public Container getParent() {
		return parent;
	}

	public void setParent(Container parent) {
		this.parent = parent;
	}
	
	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public String getName() {
		return name;
	}

	public Type(String containerName) {
		name = containerName;
	}

	public void addDependency(Dependency dependency) {
		dependency.setParent(this);
		dependencies.add(dependency);
	}
	
	public String toString() {
		StringBuilder result = new StringBuilder();
		
		result.append("Type ").append(name).append("{");
		for (Iterator<Dependency> i =  dependencies.iterator(); i.hasNext(); ) {
			Dependency dep = i.next();
			result.append(dep);
			if (i.hasNext()) result.append(',');
		}
		result.append("}");
		return result.toString();
	}

	public boolean hasDependency(String filter) {
		boolean result = dependencies.stream().anyMatch(new Predicate<Dependency>() {

			@Override
			public boolean test(Dependency t) {
				return t.getName().matches(filter);
			}
		});
		
		if (name.equals("com.lsyas.acs.basis.mad.mbq.ejb.MadMbqBc")) {
			System.out.println("DADA");
		}

		System.out.println("Type " + getName() + " has dependency " + filter + ": " + result);
		return result;
	}

	public String getPackage() {
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return name;
    }
	
}
