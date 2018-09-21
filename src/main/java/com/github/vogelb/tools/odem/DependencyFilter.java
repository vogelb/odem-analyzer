package com.github.vogelb.tools.odem;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.github.vogelb.tools.odem.model.Container;
import com.github.vogelb.tools.odem.model.Dependency;
import com.github.vogelb.tools.odem.model.DependencyGraph;
import com.github.vogelb.tools.odem.model.DependencyGraph.GraphElement;
import com.github.vogelb.tools.odem.model.Type;
import com.github.vogelb.tools.odem.model.TypeMap;

/**
 * Filter given dependency containers according configurable criteria.
 */
public class DependencyFilter {
    private final TypeMap containers;
    private final Random random = new Random();
    private final String basePathFilter;
    private String includeContainerFilter = null;
    private String includePackageFilter = null;
    private String ignorePackageFilter = null;
    private String ignoreContainerFilter = null;
    private boolean includeInternalDependencies = true;
    private Map<String, GraphElement> graphProperties = null;

    /**
     * Create a new Dependency Filter for given containers
     * 
     * @param containers
     *            The containers to consider
     * @param basePathFilter
     *            Base path filter expression (regex)
     */
    DependencyFilter(TypeMap containers, String basePathFilter) {
        this.containers = containers;
        this.basePathFilter = basePathFilter;
    }

    /**
     * Filter expression for included containers (incoming dependencies only)
     * 
     * @param includeContainerFilter
     *            The filter expression
     * @return a filter that will only accept dependencies to the given
     *         containers
     */
    public DependencyFilter includeContainerFilter(String includeContainerFilter) {
        this.includeContainerFilter = includeContainerFilter;
        return this;
    }

    /**
     * Filter expression for ignored containers (outgoing dependencies only)
     * 
     * @param ignoreContainerFilter
     *            The filter expression
     * @return a filter that will ignore dependencies from the given containers
     */
    public DependencyFilter ignoreContainerFilter(String ignoreContainerFilter) {
        this.ignoreContainerFilter = ignoreContainerFilter;
        return this;
    }

    /**
     * Filter expression for included packages.
     * 
     * @param includePackageFilter
     *            The filter expression
     * @return a filter that will include dependencies from the given packages
     */
    public DependencyFilter includePackageFilter(String includePackageFilter) {
        this.includePackageFilter = includePackageFilter;
        return this;
    }

    /**
     * Filter expression for ignored packages.
     * 
     * @param includePackageFilter
     *            The filter expression
     * @return a filter that will ignore dependencies from the given packages
     */
    public DependencyFilter ignorePackageFilter(String ignorePackageFilter) {
        this.ignorePackageFilter = ignorePackageFilter;
        return this;
    }

    /**
     * Whether or not to include dependencies from / to the same package
     * 
     * @param includePackageDependencies
     *            The parameter
     * @return A filter that will include dependencies from / to the same
     *         package when set to true and ignore them otherwise. The default
     *         setting is true.
     */
    public DependencyFilter setIncludePackageDependencies(boolean includePackageDependencies) {
        includeInternalDependencies = includePackageDependencies;
        return this;
    }

    /**
     * Set properties for graphical representations.
     * 
     * @param graphProps
     *            The graphical properties
     * @return The filter
     */
    public DependencyFilter setGraphicProperties(GraphElement[] graphProps) {
        graphProperties = new HashMap<>();
        for (GraphElement e : graphProps) {
            graphProperties.put(e.name, e);
        }
        return this;
    }

    private GraphElement getGraphicProperties(String name) {
        if (graphProperties == null)
            return null;
        GraphElement result = graphProperties.get(name);
        if (result == null) {
            result = new GraphElement(name, getRandomColor(), 1);
        }
        return result;
    }

    /**
     * Build the dependency graph.
     * 
     * @param tlps
     * @return
     */
    public DependencyGraph buildGraph() {

        DependencyGraph result = new DependencyGraph();
        if (graphProperties != null) {
            graphProperties.values().forEach(g -> result.addElement(g));
        }

        Collection<Container> sourceContainers;
        if (!(includeContainerFilter == null || includeContainerFilter.isEmpty())) {
            sourceContainers = containers.getContainers().stream().filter(new Predicate<Container>() {
                @Override
                public boolean test(Container t) {
                    return t.getShortName().matches(includeContainerFilter);
                }
            }).collect(Collectors.toList());
        } else {
            sourceContainers = containers.getContainers();
        }

        for (Container c : sourceContainers) {
            System.out.println("Searching container " + c.getName());
            Map<String, List<Dependency>> grouped = c.getDependencies().stream().filter(new Predicate<Dependency>() {
                @Override
                public boolean test(Dependency d) {
                    boolean result =  d.getParent().getName().matches(basePathFilter)
                            && d.getPackage().matches(includePackageFilter)
                            && !d.getPackage().matches(ignorePackageFilter)
                            && !d.getParent().getPackage().matches(ignorePackageFilter)
                            && (includeInternalDependencies || d.getName().matches(basePathFilter));
                    return result;
                }
            }).collect(Collectors.groupingBy(d -> d.getParent().getTopLevelPackage()));

            for (String fromPackage : grouped.keySet()) {
                List<Dependency> deps = grouped.get(fromPackage);
                System.out.println("\nProcessing dependencies for package " + fromPackage);
                deps.stream().collect(Collectors.groupingBy(d -> d.getTopLevelPackage(), Collectors.counting()))
                        .forEach((toPackage, numberOfDependencies) -> result.addDependency(
                                getGraphicProperties(fromPackage),
                                getGraphicProperties(toPackage), numberOfDependencies));
            }
        }

        return result;
    }

    public List<Dependency> getDependenciesFrom() {
        List<Dependency> result = new ArrayList<Dependency>();
        // Get all source containers
        Collection<Container> sourceContainers;
        if (!(includeContainerFilter == null || includeContainerFilter.isEmpty())) {
            sourceContainers = containers.getContainers().stream().filter(new Predicate<Container>() {
                @Override
                public boolean test(Container t) {
                    return t.getShortName().matches(includeContainerFilter);
                }
            }).collect(Collectors.toList());
        } else {
            sourceContainers = containers.getContainers();
        }

        for (Container c : sourceContainers) {
            System.out.println("Searching container " + c.getName());
            for (Type t : c.getTypes()) {
                Type type = t;
                result.addAll(type.getDependencies().stream().filter(new Predicate<Dependency>() {
                    @Override
                    public boolean test(Dependency d) {
                        boolean result = d.getParent().getName().matches(basePathFilter)
                                && d.getPackage().matches(includePackageFilter)
                                && !d.getPackage().matches(ignorePackageFilter)
                                && (includeInternalDependencies || !d.getName().matches(basePathFilter));
                        return result;
                    }
                }).collect(Collectors.toList()));
            }
        }
        return result;
    }

    /**
     * Get dependencies to the given set of classes.
     * 
     * @return The list of dependencies.
     */
    public List<Dependency> getDependenciesTo() {
        List<Dependency> result = new ArrayList<Dependency>();

        // Get all source containers
        Collection<Container> sourceContainers;
        if (!(ignoreContainerFilter == null || ignoreContainerFilter.isEmpty())) {
            sourceContainers = containers.getContainers().stream().filter(new Predicate<Container>() {
                @Override
                public boolean test(Container t) {
                    return !t.getName().matches(ignoreContainerFilter);
                }
            }).collect(Collectors.toList());
        } else {
            sourceContainers = containers.getContainers();
        }

        for (Container c : sourceContainers) {
            System.out.println("Searching container " + c.getName() + " for dependents on " + basePathFilter + " / "
                    + includePackageFilter);
            for (Type t : c.getTypes()) {
                result.addAll(t.getDependencies().stream().filter(new Predicate<Dependency>() {
                    @Override
                    public boolean test(Dependency d) {
                        boolean result = d.getName().matches(basePathFilter)
                                && d.getPackage().matches(includePackageFilter)
                                && !d.getParent().getName().matches(ignorePackageFilter)
                                && (includeInternalDependencies || !d.getParent().getName().matches(basePathFilter));
                        return result;
                    }
                }).collect(Collectors.toList()));
            }
        }

        return result;
    }

    private Color getRandomColor() {
        // Java 'Color' class takes 3 floats, from 0 to 1.
        float r = random.nextFloat();
        float g = random.nextFloat();
        float b = random.nextFloat();
        return new Color(r, g, b);
    }

}