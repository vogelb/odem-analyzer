package com.github.vogelb.tools.odem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.github.vogelb.tools.odem.model.Container;
import com.github.vogelb.tools.odem.model.Type;

import java.util.ArrayList;
import java.util.List;

public class TypeMap {
	private List<Container> containers = new ArrayList<Container>();
	private Map<String, String> types = new HashMap<String, String>();
	
	
	public void add(Container container) {
		containers.add(container);
		for (Type type : container.getTypes()) {
			types.put(type.getName(), container.getName());
		}
	}
	
	public Collection<Container> getContainers() {
		return containers;
	}

	public String getContainer(String typeName) {
		return types.get(typeName);
	}
}
