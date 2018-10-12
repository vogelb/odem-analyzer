package com.github.vogelb.tools.odem.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Maps types to containers.
 *  
 * @author BVogel
 */
public class TypeMap {
	private Map<String, Container> containers = new HashMap<>();
	private Map<String, String> types = new HashMap<String, String>();
	
	/**
	 * Add a new container and all contained types.
	 * 
	 * @param container the container to add
	 */
	public void add(Container container) {
		containers.put(container.getName(), container);
		for (Type type : container.getTypes()) {
			types.put(type.getName(), container.getName());
		}
	}
	
	/**
	 * Get all containers.
	 * 
	 * @return The list of containers.
	 */
	public Collection<Container> getContainers() {
		return containers.values();
	}

	/**
	 * Get the container containing the given type.
	 * 
	 * @param typeName the type name
	 * @return The container containing the type, null if none found
	 */
	public String getContainerByType(String typeName) {
		return types.get(typeName);
	}

    /**
     * Get the container with the given name.
     * 
     * @param containerName the container name
     * @return the container, null if none found
     */
    public Container getContainerByName(String containerName) {
        return containers.get(containerName);
    }
}
