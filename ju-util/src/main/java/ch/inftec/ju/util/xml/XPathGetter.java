package ch.inftec.ju.util.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.inftec.ju.util.JuRuntimeException;

/**
 * Class providing a couple of helper functions to evaluate XPath expressions. There
 * are a variety of return types like Strings, Longs, Arrays and other (relative)
 * XPathGetters.
 * 
 * @author tgdmemae
 *
 */
public class XPathGetter {
	/**
	 * XPath instance to work with XPaths.
	 */
	private static XPath xPath;
	
	/**
	 * Node this getter works with, i.e. that matches '.'
	 */
	private Node node;
	
	/**
	 * Creates a new XPathGetter with the specified node as a
	 * reference item.
	 * @param node Node that will match the XPath expression '.'
	 */
	public XPathGetter(Node node) {
		this.node = node;
	}
	
	/**
	 * Gets an XPath instance to compile XPath expressions. There is only
	 * one static XPath instance created for all XPathGetters.
	 * @return XPath instance
	 */
	private XPath getXPath() {
		if (XPathGetter.xPath == null) {
			// Load the xPath instance to parse the document
		    XPathFactory xPathFactory = XPathFactory.newInstance();
		    XPathGetter.xPath = xPathFactory.newXPath();
		}
		
		return XPathGetter.xPath;
	}
	
	/**
	 * Evaluates the specified XPath expression.
	 * @param query XPath expression
	 * @return XPath result
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	private String evaluate(String query) throws JuRuntimeException {
		try {
			XPathExpression xPathExpression = this.getXPath().compile(query);
			return xPathExpression.evaluate(this.node);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't evaluate xPath expression: " + query, ex);			
		}
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a NodeList.
	 * @param query XPath expression
	 * @return XPath result as a NodeList
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	private NodeList evaluateNodeSet(String query) throws JuRuntimeException {
		try {
			XPathExpression xPathExpression = this.getXPath().compile(query);
			return (NodeList)xPathExpression.evaluate(this.node, XPathConstants.NODESET);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't evaluate xPath expression: " + query, ex);			
		}
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a single String.
	 * @param query XPath expression
	 * @return XPath result as String
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public String getSingle(String query) throws JuRuntimeException {
		return this.evaluate(query);
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a single Long.
	 * @param query XPath expression
	 * @return XPath result as Long
	 * @throws JuRuntimeException If the XPath cannot be evaluated or if the 
	 * result cannot be converted to a Long
	 */
	public Long getSingleLong(String query) throws JuRuntimeException {
		String value = this.getSingle(query);
		if (value == null || value.length() == 0) return null;
		
		try { 
			return Long.parseLong(value);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't convert " + value + " to Long", ex);
		}
	}
	
	/**
	 * Gets the count of results for the specified queries.
	 * @param query XPath expression
	 * @return Number of results
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public int getCount(String query) throws JuRuntimeException {
		return this.getNodes(query).length;
	}
	
	/**
	 * Gets whether the specified query either returns nothing or an empty element
	 * like &lt;a/&gt;
	 * <p>
	 * Note that an element is not considered empty though if it contains only empty elements.
	 * @param query XPath expression
	 * @return If the expression returns nothing or only empty elements (i.e. an element that doesn't contain
	 * any attributes nor text. The method also returns true if we find multiple of such elements.
	 * @throws JuRuntimeException I the XPath cannot be evaluated
	 */
	public boolean isEmptyElement(String query) throws JuRuntimeException {
		for (Node node : this.getNodes(query)) {
			if (!StringUtils.isEmpty(node.getTextContent())
					|| node.getChildNodes().getLength() > 0
					|| node.getAttributes().getLength() > 0) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a String array.
	 * @param query XPath expression
	 * @return XPath result as String array
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public String[] getArray(String query) throws JuRuntimeException {
		try {			
			XPathExpression xPathExpression = this.getXPath().compile(query);
			NodeList list = (NodeList)xPathExpression.evaluate(this.node, XPathConstants.NODESET);
			
			String[] values = new String[list.getLength()];
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				values[i] = node.getTextContent();
			}
			
			return values;
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't evaluate xPath query: " + query);
		}
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a Long array.
	 * @param query XPath expression
	 * @return XPath result as Long array
	 * @throws JuRuntimeException If the XPath cannot be evaluated or if a result
	 * cannot be converted to Long
	 */
	public Long[] getArrayLong(String query) throws JuRuntimeException {
		String[] values = this.getArray(query);
		
		Long[] longValues = new Long[values.length];
		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			if (value == null || value.length() == 0) {
				longValues[i] = null;
			} else {
				try {
					longValues[i] = Long.parseLong(value);
				} catch (Exception ex) {
					throw new JuRuntimeException("Couldn't convert " + value + " to Long", ex);
				}
			}
		}
		
		return longValues;
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a NodeList.
	 * @param query XPath expression
	 * @return XPath result as NodeList
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public NodeList getNodeList(String query) throws JuRuntimeException {
		return this.evaluateNodeSet(query);
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a list of names of the
	 * nodes the query matched.
	 * @param query XPath expression
	 * @return List of node names
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public List<String> getNodeNames(String query) throws JuRuntimeException {
		List<String> nodeNames = new ArrayList<>();
		for (Node node : this.getNodes(query)) {
			nodeNames.add(node.getNodeName());
		}
		
		return nodeNames;
	}
	
	/**
	 * Evaluates the specific XPath expression and returns true if there
	 * is at least one matching node.
	 * @param query XPath expression
	 * @return True if there is at least one matching node
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public boolean exists(String query) throws JuRuntimeException {
		return this.evaluateNodeSet(query).getLength() > 0;
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a Node. If the expression
	 * actually returns more than one Node, only the first one is returned.
	 * @param query XPath expression
	 * @return XPath result as Node
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public Node getNode(String query) throws JuRuntimeException {
		NodeList list = this.getNodeList(query);
		
		if (list.getLength() == 0) return null;
		else return list.item(0);
	}
	
	/**
	 * Evaluates the specified XPath expression and returns an array of Nodes.
	 * @param query XPath expression
	 * @return XPath result as an array of Nodes
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public Node[] getNodes(String query) throws JuRuntimeException {
		NodeList list = this.getNodeList(query);
		
		Node nodes[] = new Node[list.getLength()];
		for (int i = 0; i < list.getLength(); i++) {
			nodes[i] = list.item(i);
		}
		
		return nodes;
	}
	
	/**
	 * Evaluates the specified XPath expression and returns an array of
	 * XPathGetters relative to the corresponding result nodes of the query.
	 * @param query XPath expression
	 * @return Array of XPathGetters
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public XPathGetter[] getGetters(String query) throws JuRuntimeException {
		NodeList list = this.getNodeList(query);
		
		XPathGetter getters[] = new XPathGetter[list.getLength()];
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			getters[i] = new XPathGetter(node);
		}
		
		return getters;
	}
	
	/**
	 * Evaluates the specified XPath expression and returns an
	 * XPathGetter relative to the corresponding result node of the query.
	 * @param query XPath expression
	 * @return XPathGetter
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public XPathGetter getGetter(String query) throws JuRuntimeException {
		XPathGetter getters[] = this.getGetters(query);
		
		if (getters.length == 0) return null;
		else return getters[0];
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a String array. The array
	 * contains only distinct values, duplicate values are discarded.
	 * @param query XPath expression
	 * @return XPath result as String array containing only distinct values
	 * @throws JuRuntimeException If the XPath cannot be evaluated
	 */
	public String[] getDistinctArray(String query) throws JuRuntimeException {
		String[] values = this.getArray(query);
		List<String> distinctValues = new ArrayList<String>();
		
		for (String value : values) {
			if (!distinctValues.contains(value)) distinctValues.add(value);
		}
		
		return (String[])distinctValues.toArray(new String[0]);
	}
	
	/**
	 * Evaluates the specified XPath expression and returns a Long array. The array
	 * contains only distinct values, duplicate values are discarded.
	 * @param query XPath expression
	 * @return XPath result as Long array containing only distinct values
	 * @throws JuRuntimeException If the XPath cannot be evaluated or if a value cannot
	 * be converted to Long
	 */
	public Long[] getDistinctArrayLong(String query) throws JuRuntimeException {
		String distinctValues[] = this.getDistinctArray(query);
		Long distinctValuesLong[] = new Long[distinctValues.length];
		
		for (int i = 0; i < distinctValues.length; i++) {
			try {
				distinctValuesLong[i] = Long.parseLong(distinctValues[i]);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't convert " + distinctValues[i] + " to Long", ex);
			}
		}
		
		return distinctValuesLong;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("node", this.node.getNodeName())
			.append("nodeValue", this.node.getNodeValue()).toString();
	}
}
