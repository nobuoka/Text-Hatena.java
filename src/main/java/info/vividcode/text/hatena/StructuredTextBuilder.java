package info.vividcode.text.hatena;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class StructuredTextBuilder {

    private final Document doc;
    private Node currentNode;

    public static final String VCST_NS =
            "http://xml.vividcode.info/vc-structured-text/v1";

    private Element createNode(Document doc, String nodeName) {
        return doc.createElementNS(VCST_NS, nodeName);
    }

    public StructuredTextBuilder() {
        // Step 1: create a DocumentBuilderFactory and setNamespaceAware
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        // Step 2: create a DocumentBuilder
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        doc = db.newDocument();
        currentNode = doc.appendChild(createNode(doc, "root"));
    }

    public void appendText(String text) {
        if (text.length() == 0) return;
        Node lastChild = currentNode.getLastChild();
        if (lastChild == null || !lastChild.getNodeName().equals("raw-text")) {
            lastChild = currentNode.appendChild(createNode(doc, "raw-text"));
        }
        text = Pattern.compile("\\s+").matcher(text).replaceAll(" ");
        lastChild.appendChild(doc.createTextNode(text));
    }

    public void openNode(String nodeName) {
        openNode(nodeName, Collections.<String,String>emptyMap());
    }

    public void openNode(String nodeName, Map<String,String> attributes) {
        Element e = createNode(doc, nodeName);
        for (Entry<String,String> attr : attributes.entrySet()) {
            e.setAttribute(attr.getKey(), attr.getValue());
        }
        currentNode = currentNode.appendChild(e);
    }

    public void closeNode(String nodeName) {
        if (!currentNode.getNodeName().equals(nodeName)) {
            // 名前が違う!!
            Logger.getGlobal().info("名前が違うので Node を閉じません : " + nodeName);
        } else {
            currentNode = currentNode.getParentNode();
        }
    }

    public StructuredText build() {
        return new StructuredText(doc);
    }

}
