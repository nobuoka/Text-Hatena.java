package info.vividcode.text.hatena;

import info.vividcode.text.hatena.processors.HeadlineProcessor;
import info.vividcode.text.hatena.processors.ListProcessor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

public class HatenaTextParser {

    /*
     * 行頭
     *
     * [*]* CHAR+
     * [+-]* CHAR+
     * :CHAR+:CHAR+
     * |CHAR+|CHAR+|
     * >|
     * |<
     * >|CHAR*|
     * ||<
     * >CHAR*>
     * <<
     */

    /*
     * インライン
     *
     * (( CHAR+ ))
     * [[ CHAR+ ]]
     * [ URL ]
     * [ URL :title= CHAR+ ]
     * `CHAR+`
     * &...;
     * <...>
     */

    public static void main(String[] args) {
        Logger.getGlobal().setLevel(Level.ALL); // バグ回避コード?
        ConsoleHandler h = new ConsoleHandler();
        h.setLevel(Level.ALL);
        Logger.getGlobal().addHandler(h);

        HatenaTextParser p = new HatenaTextParser();
        StructuredText t = p.parse("おっ、いいね\n*日本語です\n-リスト!!\n--さらにリスト!!\n-リスト1\nどうだろ? ちゃんとできてる? \n**次の見出し\n ごーごー");

        String out;
        try {
            out = t.serializeToXml();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            out = "";
        }
        System.out.println(out);
        System.out.println(t.getRawText());

        try (StringWriter sw = new StringWriter()) {
            StructuredTextToHtmlConverter conv =
                    new StructuredTextToHtmlConverter();
            conv.convert(t, sw);
            System.out.println("!!" + sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Set<Processor> processors = new HashSet<>();
    {
        processors.add(new HeadlineProcessor());
        processors.add(new ListProcessor());
    }

    public StructuredText parse(String in) {
        TextCursor textCursor = new TextCursor(in);
        ProcessorCursorList pcs = new ProcessorCursorList(textCursor, processors);
        StructuredTextBuilder stb = new StructuredTextBuilder();
        while (!pcs.isEmpty()) {
            ProcessorWithCursor pc = pcs.pollFirst();
            String plainText = textCursor.advanceTo(pc.pos());
            Logger.getGlobal().finer("plain: " + plainText + " (" + plainText.length() + ")");
            stb.appendText(plainText);
            pc.process(textCursor, stb, pcs);
            if (pc.next()) {
                pcs.add(pc);
            } else {
                Logger.getGlobal().finer("次のマッチ箇所が存在しない");
            }
            pcs.progressProcessorCursorsAccordingToTextCursor();
        }
        String plainText = textCursor.advanceTo(in.length());
        Logger.getGlobal().finer("plain: " + plainText + " (" + plainText.length() + ")");
        stb.appendText(plainText);
        return stb.build();
    }

}
