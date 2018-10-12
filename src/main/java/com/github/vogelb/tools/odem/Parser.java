package com.github.vogelb.tools.odem;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.vogelb.tools.odem.model.Container;
import com.github.vogelb.tools.odem.model.Dependency;
import com.github.vogelb.tools.odem.model.Type;
import com.github.vogelb.tools.odem.model.TypeMap;

/**
 * ODEM file parser.
 */
public class Parser {
    
    private static final Container EXTERNAL_CONTAINER = new Container("External");
    private final Map<String, Type> typeMap;
    private final Components externalComponents;
    private final boolean createExternals;
    
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);
    
    public Parser() {
        this(null, false);
    }
    
    public Parser(Components externalComponents, boolean includeExternalComponents) {
        typeMap = new HashMap<>();
        this.externalComponents = externalComponents;
        createExternals = includeExternalComponents;
    }
    
    /**
     * Parse the given ODEM file
     * 
     * @param odemFile
     *            the file path
     * @return the parsed type map.
     */
    public TypeMap parse(String input, InputStream in) {
        TypeMap containers = new TypeMap();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            documentBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicID, String systemID)
                        throws SAXException {
                        return new InputSource(new StringReader(""));
                    }
            });
            Document doc = documentBuilder.parse(in);

            NodeList containerNodes = doc.getDocumentElement().getElementsByTagName("container");
            for (int i = 0; i < containerNodes.getLength(); ++i) {
                Node containerNode = containerNodes.item(i);
                containers.add(importContainer(containerNode));
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException("Error parsing odem file " + input, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        resolveTypes(containers);

        return containers;
    }
    
    private void resolveTypes(TypeMap containers) {
        Collection<Container> containers2 = new ArrayList<>(containers.getContainers());
        containers2.forEach(c -> c.getDependencies().forEach(d -> {
            Type type = typeMap.get(d.getName());
            if (type == null) {
                String external = externalComponents == null ? null : externalComponents.getComponent(d.getName()); 
                if (external != null && !external.equals(d.getName())) {
                    Container container = containers.getContainerByName(external);
                    if (container == null) {
                        container = new Container(external);
                        containers.add(container);
                    }
                    logger.info("Mapped to known External: " + d.getName() + " --> " + external);
                    type = new Type(container, d.getName(), true);
                    container.addType(type);
                } else if (createExternals) {
                    logger.info("Mapped to unknown External: " + d.getName());
                    Container container = new Container(d.getName());
                    type = new Type(container , d.getName(), true);
                    typeMap.put(d.getName(), type);
                } else {
                    logger.info("Mapped to anonymous External: " + d.getName());
                    type = new Type(EXTERNAL_CONTAINER, d.getName(), true);
                    typeMap.put(d.getName(), type);
                }
            }
            d.setType(type);
        }));
    }

    private Container importContainer(Node containerNode) {
        Container container = new Container(containerNode.getAttributes().getNamedItem("name").getNodeValue());

        NodeList typeNodes = ((Element) containerNode).getElementsByTagName("type");
        for (int i = 0; i < typeNodes.getLength(); ++i) {
            Node typeNode = typeNodes.item(i);
            container.addType(importType(container, typeNode));
        }

        return container;
    }

    private Type importType(Container container, Node typeNode) {
        String typeName = typeNode.getAttributes().getNamedItem("name").getNodeValue();
        if (typeName.startsWith("WEB-INF.classes.")) {
            typeName = typeName.substring("WEB-INF.classes.".length());
        }
        Type type = new Type(container, typeName);
        typeMap.put(typeName, type);
        NodeList depNodes = ((Element) typeNode).getElementsByTagName("depends-on");
        for (int i = 0; i < depNodes.getLength(); ++i) {
            Node depNode = depNodes.item(i);
            Dependency dependency = importDependency(depNode);
            type.addDependency(dependency);
        }

        return type;
    }

    private static Dependency importDependency(Node depNode) {
        NamedNodeMap attributes = depNode.getAttributes();
        Dependency result = new Dependency(attributes.getNamedItem("name").getNodeValue(),
                attributes.getNamedItem("classification").getNodeValue());
        return result;
    }

}
