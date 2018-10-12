package com.github.vogelb.tools.odem.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Type {
    private static final Logger logger = LoggerFactory.getLogger(Type.class);
    
	private final Container parent;
	private final String name;
	private final boolean isExternal;
	public boolean isExternal() {
        return isExternal;
    }

    private final List<Dependency> dependencies = new ArrayList<Dependency>();
	

	public Container getParent() {
		return parent;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public String getName() {
		return name;
	}

	
	public Type(Container parent, String containerName) {
	    this(parent, containerName, false);
	}
	
	public Type(Container parent, String containerName, boolean external) {
	    this.parent = parent;
		name = containerName;
		isExternal = external;
	}

	public void addDependency(Dependency dependency) {
		dependency.setDependent(this);
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
		
		logger.debug("Type {} has dependency {}: {}", getName(), filter, result);
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
