package com.github.vogelb.tools.odem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.github.vogelb.tools.odem.model.TypeMap;

/**
 * Main entry point. Load and parse ODEM file.
 */
public class DependencyAnalyser {

    private final TypeMap containers;

    
    public DependencyAnalyser(final String odemFile) throws FileNotFoundException {
        this(odemFile, (Components) null, false);
    }
    
    public DependencyAnalyser(final String odemFile, Components externalComponents, boolean includeExternalComponents) throws FileNotFoundException {
        this(odemFile, new FileInputStream(odemFile), externalComponents, includeExternalComponents);
    }

    public DependencyAnalyser(final String inputDescriptor, final InputStream odemFile, Components externalComponents, boolean includeExternalComponents) {
        Parser parser = new Parser(externalComponents, includeExternalComponents);
        containers = parser.parse(inputDescriptor, odemFile);
    }

    public DependencyAnalyser(String inputDescriptor, InputStream in) {
        this(inputDescriptor, in, null, false);
    }

    /**
     * Create a dependency filter.
     * 
     * @param basePathFilter
     *            Select dependencies based on their package path.
     * @return a new filter
     */
    public DependencyFilter filter(String basePathFilter) {
        return new DependencyFilter(containers, basePathFilter);
    }
    
    /**
     * Create a dependency filter.
     * 
     * @return a new filter
     */
    public DependencyFilter filter() {
        return new DependencyFilter(containers, ".*");
    }

}
