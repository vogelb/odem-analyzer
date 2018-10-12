package com.github.vogelb.tools.odem;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Components {
    
    private static class ComponentMapping implements Comparable<ComponentMapping>{
        public final String name;
        public final String packagePrefix;
        public final int numPathElements;

        /**
         * Create a new ComponentMapping
         * @param aComponentName the name of the component
         * @param aPathPrefix the path prefix to map to the component
         * @param pathElements The number of path elements to use for mapping
         */
        public ComponentMapping(String aComponentName, String aPathPrefix, int pathElements) {
            name = aComponentName;
            packagePrefix = aPathPrefix;
            numPathElements = pathElements;
        }
        
        public String toString() {
            return name + " [" + packagePrefix + '/' + numPathElements +']';
        }

        @Override
        public int compareTo(ComponentMapping other) {
            return Integer.compare(other.packagePrefix.length(), packagePrefix.length());
        }
    }
    
    private final Map<String, ComponentMapping> components;
    
    public Components() {
        components = new HashMap<>();
    }
    
    public Components(Components base) {
        components = new HashMap<>(base.components);
    }
    
    public Components(String aComponentName, String aPathPrefix, int pathElements) {
        this();
        add(aComponentName, aPathPrefix, pathElements);
    }
    
    public Components add(String aComponentName, String aPathPrefix, int pathElements) {
        components.put(aComponentName, new ComponentMapping(aComponentName, aPathPrefix, pathElements));
        return this;
    }
    
    public void clear() {
        components.clear();
    }
    
    /**
     * Get the mapped component for a package name. 
     * @param aPackageName the package name
     * @param components the component Mappings
     * @return
     */
    public String getComponent(String aPackageName) {
        Optional<ComponentMapping> component = components.values().stream().filter(c -> aPackageName.startsWith(c.packagePrefix)).sorted().findFirst();
        if (component.isPresent()) {
            return getComponentPath(aPackageName, component.get());
        }
        return aPackageName;
    }

    private String getComponentPath(String aPackageName, ComponentMapping componentMapping) {
        StringBuilder result = new StringBuilder(componentMapping.name);
        if (componentMapping.numPathElements > 0) {
            String remainder = aPackageName.substring(componentMapping.packagePrefix.length());
            if (remainder.startsWith(".")) {
                remainder = remainder.substring(1);
            }
            String[] parts = remainder.split("\\.");
            for (int i = 0; i < parts.length && i < componentMapping.numPathElements; ++i) {
                result.append('.').append(parts[i]);
            }
        }
        return result.toString();
    }

    public Components remove(String aComponentName) {
        components.remove(aComponentName);
        return this;
    }
    
    public Collection<String> getAll() {
        return components.values().stream().map(c -> c.name).collect(Collectors.toList());
    }

}
