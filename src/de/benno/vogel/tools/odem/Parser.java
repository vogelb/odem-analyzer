package de.benno.vogel.tools.odem;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.benno.vogel.tools.odem.model.Container;
import de.benno.vogel.tools.odem.model.Dependency;
import de.benno.vogel.tools.odem.model.Type;

public class Parser {

	public TypeMap parse(String odemFile) {
		TypeMap containers = new TypeMap();
		FileInputStream in = null;
		try {
			in = new FileInputStream(odemFile);

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
			throw new RuntimeException("Error parsing odem file " + odemFile, e);
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

	private Container importContainer(Node containerNode) {
		Container container = new Container(containerNode.getAttributes().getNamedItem("name").getNodeValue());

		NodeList typeNodes = ((Element) containerNode).getElementsByTagName("type");
		for (int i = 0; i < typeNodes.getLength(); ++i) {
			Node typeNode = typeNodes.item(i);
			container.addType(importType(typeNode));
		}

		return container;
	}

	private Type importType(Node typeNode) {
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
