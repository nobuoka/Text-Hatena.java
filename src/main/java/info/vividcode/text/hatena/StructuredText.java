package info.vividcode.text.hatena;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class StructuredText {

    private final Document doc;

    public StructuredText(Document doc) {
        this.doc = doc;
    }

    Document getDocument() {
        return doc;
    }

    public String serializeToXml() throws TransformerException {
        StringWriter sw = new StringWriter();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        // インデントを行う
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.METHOD, "xml");
        // インデントの文字数
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource    src    = new DOMSource(doc);
        StreamResult target = new StreamResult(sw);

        t.transform(src,target);
        return sw.toString();
    }

    public String getRawText() {
        return doc.getDocumentElement().getTextContent();
    }

}
