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
        this(odemFile, new FileInputStream(odemFile));
    }

    public DependencyAnalyser(final String input, final InputStream odemFile) {
        Parser parser = new Parser();
        containers = parser.parse(input, odemFile);
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

}
