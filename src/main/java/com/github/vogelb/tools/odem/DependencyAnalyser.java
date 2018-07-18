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
import com.github.vogelb.tools.odem.model.Type;
import com.github.vogelb.tools.odem.model.TypeMap;
import com.github.vogelb.tools.odem.model.DependencyGraph.GraphElement;

public class DependencyAnalyser {
    
    public class DependencyFilter {
        Random random = new Random();
        private String basePathFilter = null;
        private String includeContainerFilter = null;
        private String includePackageFilter = null;
        private String ignorePackageFilter = null;
        private String ignoreContainerFilter = null;
        private Map<String, GraphElement> graphProperties = null;

        public DependencyFilter(String basePathFilter) {
            this.basePathFilter = basePathFilter;
        }

        public DependencyFilter IncludeContainerFilter(String includeContainerFilter) {
            this.includeContainerFilter = includeContainerFilter;
            return this;
        }
        
        public DependencyFilter IgnoreContainerFilter(String ignoreContainerFilter) {
            this.ignoreContainerFilter  = ignoreContainerFilter;
            return this;
        }

        public DependencyFilter IncludePackageFilter(String includePackageFilter) {
            this.includePackageFilter = includePackageFilter;
            return this;
        }

        public DependencyFilter IgnorePackageFilter(String ignorePackageFilter) {
            this.ignorePackageFilter = ignorePackageFilter;
            return this;
        }
        
        public DependencyFilter SetGraphicProperties(GraphElement[] graphProps) {
            graphProperties = new HashMap<>();
            for (GraphElement e : graphProps) {
                graphProperties.put(e.name, e);
            }
            return this;
        }
        
        private GraphElement GetGraphicProperties(String name) {
            GraphElement result = graphProperties.get(name);
            if (result == null) {
                result = new GraphElement(name, getRandomColor(), 1);
            }
            return result;
        }
        
        public DependencyGraph buildGraph(String packagePrefix) {
            
            DependencyGraph result = new DependencyGraph();
            graphProperties.values().forEach(g -> result.addElement(g));
            
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
                        return d.getParent().getName().matches(basePathFilter)
                                && d.getPackage().matches(includePackageFilter)
                                && !d.getPackage().matches(ignorePackageFilter)
                                && !getTopLevelPackage(d.getPackage(), packagePrefix).equals(getTopLevelPackage(d.getParent().getPackage(), packagePrefix));
                    }
                }).collect(Collectors.groupingBy(d -> getTopLevelPackage(d.getParent().getPackage(), packagePrefix)));

                for (String fromPackage : grouped.keySet()) {
                    List<Dependency> deps = grouped.get(fromPackage);
                    System.out.println("\nProcessing dependencies for package " + fromPackage);
                    deps.stream()
                        .collect(Collectors.groupingBy(d -> getTopLevelPackage(d.getPackage(), packagePrefix), Collectors.counting()))
                        .forEach((packageName, numberOfDependencies) -> result.addDependency(GetGraphicProperties(fromPackage), GetGraphicProperties(packageName), numberOfDependencies));
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
                            return d.getParent().getName().matches(basePathFilter)
                                    && d.getPackage().matches(includePackageFilter)
                                    && !d.getName().matches(basePathFilter);
                        }
                    }).collect(Collectors.toList()));
                }
            }
            return result;
        }

        List<Dependency> getDependenciesTo() {
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
                System.out.println("Searching container " + c.getName());
                for (Type t : c.getTypes()) {
                    result.addAll(t.getDependencies().stream().filter(new Predicate<Dependency>() {
                        @Override
                        public boolean test(Dependency d) {
                            return d.getName().matches(basePathFilter)
                                    && d.getPackage().matches(includePackageFilter)
                                    && !d.getParent().getName().matches(basePathFilter);
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

    private final TypeMap containers;
    
    private static String getTopLevelPackage(String packageName, String packagePrefix) {
        String topLevelPackage = packageName.startsWith(packagePrefix)
                ? packageName.substring(packagePrefix.length())
                : packageName;
        int tlpIndex = topLevelPackage.indexOf('.');
        topLevelPackage = tlpIndex > 0 ? topLevelPackage.substring(0, tlpIndex) : topLevelPackage;
        return topLevelPackage;
    }
    
    public DependencyAnalyser(final String odemFile) {
        Parser parser = new Parser();
        containers = parser.parse(odemFile);
    }

    public DependencyFilter filter(String basePathFilter) {
        return new DependencyFilter(basePathFilter);
    }

}
