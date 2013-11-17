package info.vividcode.text.hatena;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.validator.htmlparser.dom.Dom2Sax;
import nu.validator.htmlparser.sax.HtmlSerializer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

public class StructuredTextToHtmlConverter {
    public void convert(StructuredText srcDoc, Writer out) {
        Dom2Sax d2s = new Dom2Sax(new ConverterSaxHandler(out), null);
        try {
            d2s.parse(srcDoc.getDocument());
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }
}

class ConverterSaxHandler extends DefaultHandler2 {

    private static final String HTML_NS = "http://www.w3.org/1999/xhtml";

    private static Writer wrap(OutputStream out) {
        return new OutputStreamWriter(out, Charset.forName("UTF-8"));
    }

    private interface StartElementListener {
        void onStartElement(String uri, String localName, String qName,
                Attributes atts);
    }

    private interface ElementHandler {
        public String targetNamespaceUri();
        public String targetLocalName();
        public void startElement(Attributes atts);
        public void endElement();
    }

    private static class HtmlAttributes implements Attributes {

        public static final HtmlAttributes EMPTY_ATTRS =
                new HtmlAttributes(Collections.<String, String>emptyMap());

        Map<String,String> attrs;
        List<String> attrNames;

        public HtmlAttributes(Map<String,String> attrs) {
            this.attrs = attrs;
            this.attrNames = new ArrayList<>(attrs.keySet());
        }

        @Override
        public int getIndex(String qName) {
            if (qName.contains(":")) return -1;
            return attrNames.indexOf(qName);
        }

        @Override
        public int getIndex(String uri, String localName) {
            if (!"".equals(uri)) return -1;
            return attrNames.indexOf(localName);
        }

        @Override
        public int getLength() {
            return attrNames.size();
        }

        @Override
        public String getLocalName(int index) {
            if (index < 0 || attrNames.size() <= index) return null;
            return attrNames.get(index);
        }

        @Override
        public String getQName(int index) {
            if (index < 0 || attrNames.size() <= index) return null;
            return attrNames.get(index);
        }

        @Override
        public String getType(int index) {
            if (index < 0 || attrNames.size() <= index) return null;
            return "CDATA";
        }

        @Override
        public String getType(String qName) {
            return this.getType(this.getIndex(qName));
        }

        @Override
        public String getType(String uri, String localName) {
            return this.getType(this.getIndex(uri, localName));
        }

        @Override
        public String getURI(int index) {
            if (index < 0 || attrNames.size() <= index) return null;
            return "";
        }

        @Override
        public String getValue(int index) {
            if (index < 0 || attrNames.size() <= index) return null;
            return attrs.get(this.getQName(index));
        }

        @Override
        public String getValue(String qName) {
            return attrs.get(qName);
        }

        @Override
        public String getValue(String uri, String localName) {
            if (!"".equals(uri)) return null;
            return attrs.get(localName);
        }

    }

    private static class HeadlineHandler implements ElementHandler {
        private final HtmlSerializer htmlSerializer;
        private String lastStartedTagName;
        public HeadlineHandler(HtmlSerializer htmlSerializer) {
            this.htmlSerializer = htmlSerializer;
        }
        @Override
        public String targetNamespaceUri() {
            return StructuredTextBuilder.VCST_NS;
        }
        @Override
        public String targetLocalName() {
            return "headline";
        }
        @Override
        public void startElement(Attributes atts) {
            try {
                int level = Integer.parseInt(atts.getValue("level"));
                String n = 'h' + Integer.toString(level);
                lastStartedTagName = n;
                htmlSerializer.startElement(HTML_NS, n, n, HtmlAttributes.EMPTY_ATTRS);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void endElement() {
            try {
                htmlSerializer.endElement(HTML_NS, lastStartedTagName, lastStartedTagName);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ListItemHandler implements ElementHandler, StartElementListener {
        private final HtmlSerializer htmlSerializer;
        private final LinkedList<String> listBlockStack = new LinkedList<>();
        /** onStartElement におけるタグ閉じ試行を行うかどうか */
        private boolean tryingClosing = false;
        public ListItemHandler(HtmlSerializer htmlSerializer) {
            this.htmlSerializer = htmlSerializer;
        }
        @Override
        public String targetNamespaceUri() {
            return StructuredTextBuilder.VCST_NS;
        }
        @Override
        public String targetLocalName() {
            return "list-item";
        }
        @Override
        public void onStartElement(String uri, String localName, String qName,
                Attributes atts) {
            if (!tryingClosing) return;
            // list-item かどうか
            if (StructuredTextBuilder.VCST_NS.equals(uri) && "list-item".equals(localName)) {
                int level = Integer.parseInt(atts.getValue("level"));
                // level が低いならそのレベルまで全て閉じる
                while (level < listBlockStack.size()) {
                    String elemName = listBlockStack.pollLast();
                    try {
                        htmlSerializer.endElement(HTML_NS, "li", "li");
                        htmlSerializer.endElement(HTML_NS, elemName, elemName);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    }
                }
                // level が同じなら li だけ閉じる
                // TODO ul と ol の違い
                if (level == listBlockStack.size()) {
                    try {
                        htmlSerializer.endElement(HTML_NS, "li", "li");
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    }
                }
                // level が高いなら閉じない
            } else {
                // 全部閉じ
                while (listBlockStack.size() > 0) {
                    String elemName = listBlockStack.pollLast();
                    try {
                        htmlSerializer.endElement(HTML_NS, "li", "li");
                        htmlSerializer.endElement(HTML_NS, elemName, elemName);
                    } catch (SAXException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        @Override
        public void startElement(Attributes atts) {
            try {
                int level = Integer.parseInt(atts.getValue("level"));
                if (listBlockStack.size() < level) {
                    htmlSerializer.startElement(HTML_NS, "ul", "ul", HtmlAttributes.EMPTY_ATTRS);
                    listBlockStack.add("ul");
                }
                htmlSerializer.startElement(HTML_NS, "li", "li", HtmlAttributes.EMPTY_ATTRS);
                tryingClosing = false;
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void endElement() {
            //try {
                //htmlSerializer.endElement(HTML_NS, "li", "li");
                tryingClosing = true;
            //} catch (SAXException e) {
             //   throw new RuntimeException(e);
            //}
        }
    }

    private final HtmlSerializer htmlSerializer;

    private boolean rawTextFlag = false;

    private final Map<String,ElementHandler> elementHandlers = new HashMap<>();
    private final Set<StartElementListener> startElementListeners = new HashSet<>();

    public ConverterSaxHandler(OutputStream out) {
        this(wrap(out));
    }

    public ConverterSaxHandler(Writer out) {
        //this.writer = out;
        this.htmlSerializer = new HtmlSerializer(out);
        // headline の処理
        {
            HeadlineHandler h = new HeadlineHandler(htmlSerializer);
            elementHandlers.put(h.targetLocalName(), h);
        }
        // list-item の処理
        {
            ListItemHandler h = new ListItemHandler(htmlSerializer);
            elementHandlers.put(h.targetLocalName(), h);
            startElementListeners.add(h);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        for (StartElementListener listener : startElementListeners) {
            listener.onStartElement(uri, localName, qName, atts);
        }
        if (HTML_NS.equals(uri)) {
            // HTML のタグの場合はそのまま HtmlSerializer に任せる
            htmlSerializer.startElement(uri, localName, qName, atts);
        } else if (StructuredTextBuilder.VCST_NS.equals(uri)) {
            if ("raw-text".equals(localName)) {
                rawTextFlag = true;
            } else {
                ElementHandler h = elementHandlers.get(localName);
                if (h != null) {
                    h.startElement(atts);
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (HTML_NS.equals(uri)) {
            // HTML のタグの場合はそのまま HtmlSerializer に任せる
            htmlSerializer.endElement(uri, localName, qName);
        } else if (StructuredTextBuilder.VCST_NS.equals(uri)) {
            if ("raw-text".equals(localName)) {
                rawTextFlag = false;
            } else {
                ElementHandler h = elementHandlers.get(localName);
                if (h != null) {
                    h.endElement();
                }
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (rawTextFlag) {
            htmlSerializer.characters(ch, start, length);
        }
    }

}
