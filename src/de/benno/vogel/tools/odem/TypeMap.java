package de.benno.vogel.tools.odem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.benno.vogel.tools.odem.model.Container;
import de.benno.vogel.tools.odem.model.Type;

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
