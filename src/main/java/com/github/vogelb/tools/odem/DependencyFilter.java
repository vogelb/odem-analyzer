package com.github.vogelb.tools.odem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.vogelb.tools.odem.model.Container;
import com.github.vogelb.tools.odem.model.Dependency;
import com.github.vogelb.tools.odem.model.Type;
import com.github.vogelb.tools.odem.model.TypeMap;
import com.github.vogelb.tools.odem.model.graph.DependencyGraph;
import com.github.vogelb.tools.odem.model.graph.Node;

/**
 * Filter given dependency containers according configurable criteria.
 */
public class DependencyFilter {
    private static final Container UNKNOWN_CONTAINER = new Container("EXTERNAL");

    private static final Logger logger = LoggerFactory.getLogger(DependencyFilter.class);

    private final TypeMap containers;
    private final String basePathFilter;
    private String includeContainerFilter = null;
    private String includePackageFilter = null;
    private String ignorePackageFilter = null;
    private String ignoreContainerFilter = null;
    private boolean includeInternalDependencies = true;
    private Map<String, Node> graphProperties = null;
    private Components components = new Components();
    private Map<String, Container> classByContainers;
    private Function<String, String> nameMapper = a -> a;
    private boolean ignoreExternals = true;

    public DependencyFilter setNameMapper(Function<String, String> mapper) {
        nameMapper = mapper;
        return this;
    }

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

    public DependencyFilter configureComponents(Components components) {
        this.components = components;
        return this;
    }

    /**
     * Filter expression for included containers (incoming dependencies only)
     * 
     * @param includeContainerFilter
     *            The filter expression
     * @return a filter that will only accept dependencies to the given containers
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
     * @return A filter that will include dependencies from / to the same package when set to true and ignore them
     *         otherwise. The default setting is true.
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
    public DependencyFilter setGraphicProperties(Node[] graphProps) {
        graphProperties = new HashMap<>();
        for (Node e : graphProps) {
            graphProperties.put(e.name, e);
        }
        return this;
    }

    private Node getGraphicProperties(String name) {
        Node result = null;
        if (graphProperties != null) {
            result = graphProperties.get(name);
        }
        if (result == null) {
            result = new Node(name, null, 1);
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
            graphProperties.values().forEach(g -> result.addNode(g));
        }

        Collection<Container> sourceContainers = getFilteredContainers();
        for (Container c : sourceContainers) {
            logger.info("Searching container {}", c.getName());
            List<Dependency> filteredDependencies = filterDependencies(c).collect(Collectors.toList());
            Map<String, List<Dependency>> groupedByDependency = filteredDependencies.stream()
                    .collect(Collectors.groupingBy(d -> components.getComponent(d.getName())));

            for (String component : groupedByDependency.keySet()) {
                logger.info("Processing incoming dependencies for component {}", component);
                List<Dependency> deps = groupedByDependency.get(component);
                deps.stream().collect(Collectors.groupingBy(d -> components.getComponent(d.getDependent().getName()),
                        Collectors.counting()))
                        .forEach((fromPackage, numberOfDependencies) -> result.addDependency(
                                getGraphicProperties(fromPackage), getGraphicProperties(component),
                                numberOfDependencies));
            }
        }
        return result;
    }

    private Stream<Dependency> filterDependencies(Container c) {
        return c.getDependencies().stream().filter(new Predicate<Dependency>() {
            @Override
            public boolean test(Dependency d) {
                boolean result = (d.getDependent().getName().matches(basePathFilter)
                        || d.getName().matches(basePathFilter))
                        && (includePackageFilter == null || d.getName().matches(includePackageFilter))
                        && (includePackageFilter == null || d.getDependent().getName().matches(includePackageFilter))
                        && (ignorePackageFilter == null || !(d.getName().matches(ignorePackageFilter)
                                || d.getDependent().getName().matches(ignorePackageFilter)));
                return result;
            }
        });
    }

    /**
     * Build a graph of dependencies between containers.
     * 
     * @return The graph
     */
    public DependencyGraph buildContainerGraph() {

        DependencyGraph result = new DependencyGraph();
        if (graphProperties != null) {
            graphProperties.values().forEach(g -> result.addNode(g));
        }

        Collection<Container> sourceContainers = getFilteredContainers();

        buildTypeMap(sourceContainers);

        for (Container c : sourceContainers) {
            logger.info("Searching container {}", c.getName());
            filterDependencies(c)
                    .collect(Collectors.groupingBy(d -> getContainerForClass(d.getName()), Collectors.counting()))
                    .forEach(
                            (container, weight) -> {
                                if (!(ignoreExternals && container.equals(UNKNOWN_CONTAINER))) {
                                    result.addDependency(getGraphicProperties(nameMapper.apply(c.getName())),
                                            getGraphicProperties(nameMapper.apply(container.getName())), weight);
                                }
                            });
        }

        return result;
    }

    private Collection<Container> getFilteredContainers() {
        Collection<Container> sourceContainers;
        if (!(includeContainerFilter == null || includeContainerFilter.isEmpty())
                || !(ignoreContainerFilter == null || ignoreContainerFilter.isEmpty())) {
            sourceContainers = containers.getContainers().stream().filter(new Predicate<Container>() {
                @Override
                public boolean test(Container t) {
                    boolean result = true;
                    if (!(includeContainerFilter == null || includeContainerFilter.isEmpty())) {
                        result = t.getShortName().matches(includeContainerFilter);
                    }
                    if (!(ignoreContainerFilter == null || ignoreContainerFilter.isEmpty())) {
                        result &= !t.getShortName().matches(ignoreContainerFilter);
                    }
                    return result;
                }
            }).collect(Collectors.toList());
        } else {
            sourceContainers = containers.getContainers();
        }
        return sourceContainers;
    }

    private void buildTypeMap(Collection<Container> sourceContainers) {
        logger.info("Build type map...");
        classByContainers = new HashMap<>();
        components.getAll().forEach(c -> classByContainers.put(c, new Container(c)));
        for (Container c : sourceContainers) {
            c.getTypes().forEach(t -> {
                logger.info("Adding container {} for class {}", c.getName(), t.getName());
                classByContainers.put(t.getName(), c);
            });
        }
    }

    private Container getContainerForClass(String className) {
        String component = components.getComponent(className);
        if (component != null && classByContainers.containsKey(component)) {
            return classByContainers.get(component);
        }

        if (classByContainers.containsKey(className)) {
            return classByContainers.get(className);
        }
        logger.debug("Mapping {} to EXTERNAL", className);
        return UNKNOWN_CONTAINER;
    }

    public List<Dependency> getDependenciesFrom() {
        List<Dependency> result = new ArrayList<Dependency>();
        // Get all source containers
        Collection<Container> sourceContainers = getFilteredContainers();
        for (Container c : sourceContainers) {
            result.addAll(filterDependencies(c).peek(d -> {
                d.setName(nameMapper.apply(d.getName()));
                d.getType().getParent().setName(nameMapper.apply(d.getType().getParent().getName()));
                d.getDependent().getParent().setName(nameMapper.apply(d.getDependent().getParent().getName()));
            }).collect(Collectors.toList()));
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
            logger.info("Searching container {} for dependents on {} / {}", c.getName(), basePathFilter,
                    includePackageFilter);
            for (Type t : c.getTypes()) {
                result.addAll(t.getDependencies().stream().filter(new Predicate<Dependency>() {
                    @Override
                    public boolean test(Dependency d) {
                        boolean result = d.getName().matches(basePathFilter)
                                && d.getPackage().matches(includePackageFilter)
                                && !d.getDependent().getName().matches(ignorePackageFilter)
                                && (includeInternalDependencies || !d.getDependent().getName().matches(basePathFilter));
                        return result;
                    }
                }).collect(Collectors.toList()));
            }
        }

        return result;
    }

    public DependencyFilter ignoreExternals(boolean b) {
        ignoreExternals = b;
        return this;
    }

}
