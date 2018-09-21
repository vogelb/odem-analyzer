package com.github.vogelb.tools.odem;

import java.util.ArrayList;
import java.util.List;

public abstract class Components {
    
    private static class ComponentMapping {
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
    }
    
    private static final List<ComponentMapping> components = new ArrayList<>();
    
    public static void addComponent(String aComponentName, String aPathPrefix, int pathElements) {
        components.add(new ComponentMapping(aComponentName, aPathPrefix, pathElements));
    }
    
    public static void clear() {
        components.clear();
    }
    
    /**
     * Get the mapped component for a package name. 
     * @param aPackageName the package name
     * @param components the component Mappings
     * @return
     */
    public static String getComponent(String aPackageName) {
        for (ComponentMapping component : components) {
            if (aPackageName.startsWith(component.packagePrefix)) {
                String result = component.name; 
                if (component.numPathElements > 0) {
                    String remainder = aPackageName.substring(component.packagePrefix.length());
                    if (remainder.startsWith(".")) remainder = remainder.substring(1);
                    result += ".";
                    String[] parts = remainder.split("\\.");
                    for (int i = 0; i < parts.length && i < component.numPathElements; ++i) {
                        result += parts[i];
                    }
                }
                return result;
            }
        }
        return aPackageName;
    }

}
