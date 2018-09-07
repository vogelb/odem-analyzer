package com.github.vogelb.tools.odem;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.vogelb.tools.odem.model.Container;
import com.github.vogelb.tools.odem.model.Dependency;
import com.github.vogelb.tools.odem.model.Type;
import com.github.vogelb.tools.odem.model.TypeMap;

/**
 * ODEM file parser.
 */
public class Parser {

	/**
	 * Parse the given ODEM file 
	 * @param odemFile the file path
	 * @return the parsed type map.
	 */
	public TypeMap parse(String input, InputStream in) {
		TypeMap containers = new TypeMap();
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(in);

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

		return containers;
	}

	private static Container importContainer(Node containerNode) {
		Container container = new Container(containerNode.getAttributes().getNamedItem("name").getNodeValue());

		NodeList typeNodes = ((Element) containerNode).getElementsByTagName("type");
		for (int i = 0; i < typeNodes.getLength(); ++i) {
			Node typeNode = typeNodes.item(i);
			container.addType(importType(typeNode));
		}

		return container;
	}

	private static Type importType(Node typeNode) {
		String typeName = typeNode.getAttributes().getNamedItem("name").getNodeValue();
		if (typeName.startsWith("WEB-INF.classes.")) {
			typeName = typeName.substring("WEB-INF.classes.".length());
		}
		Type type = new Type(typeName);

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
		Dependency result = new Dependency(attributes.getNamedItem("name")
				.getNodeValue(), attributes.getNamedItem("classification")
				.getNodeValue());
		return result;
	}

}
