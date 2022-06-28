
package com.pge.dataload.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class contains general purpose utility methods working with XML.
 */
public final class XmlUtilities {

	/** This member holds the factory to read in XML files. */
	public static DocumentBuilderFactory m_documentBuilderFactory = DocumentBuilderFactory.newInstance();

	/** This member holds a map of bad CDATA XML characters. */
	private static Map<Character, Character> m_mapCdataReplacements;

	static {
		m_mapCdataReplacements = new HashMap<Character, Character>();

		m_mapCdataReplacements.put(new Character((char) 0x13), null);
		m_mapCdataReplacements.put(new Character((char) 0x14), null);
		m_mapCdataReplacements.put(new Character((char) 0x15), null);
		m_mapCdataReplacements.put(new Character((char) 0x16), null);
		m_mapCdataReplacements.put(new Character((char) 0x17), null);
		m_mapCdataReplacements.put(new Character((char) 0x18), null);
		m_mapCdataReplacements.put(new Character((char) 0x19), null);
	}

	/**
	 * This method returns a clean XML string, suitable for CDATA.
	 */
	public static String cleanCdata(final String strValue) {
		String strReturn = strValue;

		if (strValue != null && strValue.length() > 0) {
			final StringBuilder strBuffer = new StringBuilder(strValue);

			for (int nIndex = strBuffer.length() - 1; nIndex >= 0; --nIndex) {
				final char c = strBuffer.charAt(nIndex);
				if (m_mapCdataReplacements.containsKey(new Character(c))) {
					final Character cReplacement = m_mapCdataReplacements.get(new Character(c));
					strBuffer.deleteCharAt(nIndex);
					if (cReplacement != null) {
						strBuffer.insert(nIndex, cReplacement);
					}
				}
			}

			strReturn = strBuffer.toString();
		}

		return strReturn;
	}

	/**
	 * This method creates a node with the given tag and value.
	 */
	public static Node createNode(final Document document, final String strTagName) {
		return createNode(document, strTagName, null, false);
	}

	/**
	 * This method creates a node with the given tag and value.
	 */
	public static Node createNode(final Document document, final String strTagName, final String strValue) {
		return createNode(document, strTagName, strValue, false);
	}

	/**
	 * This method creates a node with the given tag and value.
	 */
	public static Node createNode(final Document document, final String strTagName, final String strValue,
			final boolean bUseCdata) {
		final Element element = document.createElement(strTagName);

		if (bUseCdata) {
			final Node nodeData = document.createCDATASection(strValue);
			element.appendChild(nodeData);
		} else if (strValue != null) {
			final Node nodeText = document.createTextNode(strValue);
			element.appendChild(nodeText);
		}

		return element;
	}

	/**
	 * This method creates a node with the given tag and value as a child of the
	 * parent node.
	 */
	public static Node createNode(final Node nodeParent, final String strTagName) {
		return createNode(nodeParent, strTagName, null, false);
	}

	/**
	 * This method creates a node with the given tag and value as a child of the
	 * parent node.
	 */
	public static Node createNode(final Node nodeParent, final String strTagName, final String strValue) {
		return createNode(nodeParent, strTagName, strValue, false);
	}

	/**
	 * This method creates a node with the given tag and value as a child of the
	 * parent node.
	 */
	public static Node createNode(final Node nodeParent, final String strTagName, final String strValue,
			final boolean bUseCdata) {
		final Node nodeChild = createNode(nodeParent.getOwnerDocument(), strTagName, strValue, bUseCdata);
		nodeParent.appendChild(nodeChild);

		return nodeChild;
	}

	/**
	 * This method returns a named child node from the parent's child list.
	 * 
	 * @param nodeParent
	 *            Node containing the child nodes.
	 * @param strName
	 *            String containing the name of the child to find.
	 * @return
	 */
	public static Node find(final Node nodeParent, final String strName) {
		Node nodeChild = null;

		if (nodeParent != null && nodeParent.hasChildNodes()) {
			Node node = nodeParent.getFirstChild();

			while (node != null && nodeChild == null) {
				if (strName.equals(node.getNodeName())) {
					nodeChild = node;
				} else {
					node = node.getNextSibling();
				}
			}
		}

		return nodeChild;
	}

	/**
	 * This method gets the text of a named child node. The text is summation of
	 * all the values of the found node and its children.
	 * 
	 * @param nodeParent
	 *            Node containing the child nodes.
	 * @param strName
	 *            String containing the name of the child to find.
	 */
	public static String getChildText(final Node nodeParent, final String strName) {
		String strValue = "";

		final Node nodeChild = find(nodeParent, strName);
		if (nodeChild != null) {
			strValue = getText(nodeChild);
		}

		return strValue;
	}

	/**
	 * This method returns an empty document.
	 * 
	 * @throws ParserConfigurationException
	 */
	public static Document getDocument() throws ParserConfigurationException {
		final DocumentBuilder documentBuilder = m_documentBuilderFactory.newDocumentBuilder();

		return documentBuilder.newDocument();
	}

	/**
	 * This method returns a Document representing the XML found in the reader.
	 * 
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Document getDocument(final Reader reader)
			throws SAXException, IOException, ParserConfigurationException {
		final DocumentBuilder documentBuilder = m_documentBuilderFactory.newDocumentBuilder();

		return documentBuilder.parse(new InputSource(reader));
	}

	/**
	 * This method gets the text of a node. The text is summation of all the
	 * values of the current node and its children.
	 */
	public static String getText(final Node node) {
		final TextAction action = new TextAction();

		traverse(node, action);

		return action.toString();
	}

	/**
	 * This method returns a string representation of the document.
	 * 
	 * @throws IOException
	 */
	public static String printNode(final Document document) throws IOException {
		return printNode(document.getDocumentElement());
	}

	/**
	 * This method returns a string representation of the node.
	 * 
	 * @throws IOException
	 */
	public static String printNode(final Node node) throws IOException {
		final StringWriter writer = new StringWriter();

		printNode(node, writer);

		return writer.toString();
	}

	/**
	 * This method replicates the given node. The new node is a deep copy of the
	 * original node.
	 */
	public static Node replicate(final Node nodeOriginal) {
		return replicate(nodeOriginal, nodeOriginal.getNodeName());
	}

	/**
	 * This method replicates the given node. The new node is a deep copy of the
	 * original node. The new node will have the passed in name.
	 */
	public static Node replicate(final Node nodeOriginal, final String strTagName) {
		final Node nodeNew = createNode(nodeOriginal.getOwnerDocument(), strTagName, nodeOriginal.getNodeValue());

		// do the deep copy part of it
		Node nodeChild = nodeOriginal.getFirstChild();
		while (nodeChild != null) {
			final Node nodeNewChild = nodeChild.cloneNode(true);
			nodeNew.appendChild(nodeNewChild);

			nodeChild = nodeChild.getNextSibling();
		}

		return nodeNew;
	}

	/**
	 * This method attempts to set the value of a node. If the node has a child
	 * CDATA node, it sets that; otherwise, it sets the passed in node value.
	 */
	public static void setNodeValue(Node node, final String strValue) {
		if (node != null) {
			if (node.hasChildNodes()) {
				Node nodeChild = node.getFirstChild();
				while (nodeChild != null) {
					if (Node.CDATA_SECTION_NODE == nodeChild.getNodeType()
							|| Node.TEXT_NODE == nodeChild.getNodeType()) {
						node = nodeChild;
						break;
					}
					nodeChild = nodeChild.getNextSibling();
				}
			}

			if (strValue != null) {
				if (Node.TEXT_NODE == node.getNodeType() || Node.CDATA_SECTION_NODE == node.getNodeType()) {
					node.setNodeValue(strValue);
				} else {
					final Node nodeText = node.getOwnerDocument().createTextNode(strValue);
					node.appendChild(nodeText);
				}
			} else if (Node.CDATA_SECTION_NODE == node.getNodeType()) {
				node.getParentNode().removeChild(node);
			}
		}
	}

	/**
	 * This method prints the value of the current node (technically a child of
	 * the current node but not explicitly called out) and the other children of
	 * this node.
	 * 
	 * @throws IOException
	 */
	private static void printChildren(final Node node, final Writer writer) throws IOException {
		String strValue = node.getNodeValue();
		if (strValue != null) {
			strValue = StringUtils.trimToNull(strValue);
			if (strValue != null) {
				writer.write(strValue);
			}
		}

		Node nodeNext = node.getFirstChild();
		while (nodeNext != null) {
			printNode(nodeNext, writer);

			nodeNext = nodeNext.getNextSibling();
		}
	}

	/**
	 * This method writes the node information to the writer.
	 * 
	 * @throws IOException
	 */
	private static void printNode(final Node node, final Writer writer) throws IOException {
		final int nType = node.getNodeType();

		switch (nType) {
		case Node.ELEMENT_NODE:
			writer.write("<");
			writer.write(node.getNodeName());

			final NamedNodeMap mapAttributes = node.getAttributes();
			if (mapAttributes != null && mapAttributes.getLength() > 0) {
				for (int nIndex = 0; nIndex < mapAttributes.getLength(); ++nIndex) {
					final Node nodeAttribute = mapAttributes.item(nIndex);
					writer.write(" ");
					writer.write(nodeAttribute.getNodeName());
					writer.write("=\"");

					final String strValue = nodeAttribute.getNodeValue();
					if (strValue != null) {
						writer.write(strValue);
					}

					writer.write("\"");
				}
			}

			writer.write(">");

			printChildren(node, writer);

			writer.write("</");
			writer.write(node.getNodeName());
			writer.write(">");
			break;

		case Node.DOCUMENT_NODE:
		case Node.TEXT_NODE:
			printChildren(node, writer);
			break;

		case Node.CDATA_SECTION_NODE:
			writer.write("<![CDATA[");

			printChildren(node, writer);

			writer.write("]]>");
			break;

		// default:
		// m_log.debug("Node Type: " + node.getNodeType());
		// break;
		}
	}

	/**
	 * This method recursively processes the node. The action is performed on
	 * the node prior to processing the children. This process is an in-depth
	 * traversal.
	 * 
	 * @param node
	 *            Node to process.
	 * @param action
	 *            Action instance defining the action to take on each node.
	 */
	private static void traverse(final Node node, final Action action) {
		action.process(node);

		Node nodeNext = node.getFirstChild();
		while (nodeNext != null) {
			traverse(nodeNext, action);

			nodeNext = nodeNext.getNextSibling();
		}
	}

	/**
	 * This class is a base class for traversal action.
	 */
	private abstract static class Action {
		/**
		 * This method performs the action on the node.
		 * 
		 * @param node
		 *            Node to be processed.
		 */
		public abstract void process(Node node);
	}

	/**
	 * This class is used to collect text from nodes.
	 */
	private static class TextAction extends Action {
		/** This member collection the string being built. */
		private final StringBuilder m_strBuffer = new StringBuilder();

		/**
		 * This method performs the action on the node.
		 * 
		 * @param node
		 *            Node to be processed.
		 */
		@Override
		public void process(final Node node) {
			final String strValue = node.getNodeValue();
			if (strValue != null) {
				m_strBuffer.append(strValue);
			}
		}

		/**
		 * This method returns a string representation of the node.
		 */
		@Override
		public String toString() {
			return m_strBuffer.toString();
		}
	}
}
