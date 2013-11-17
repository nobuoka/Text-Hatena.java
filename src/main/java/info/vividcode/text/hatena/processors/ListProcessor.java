package info.vividcode.text.hatena.processors;

import info.vividcode.text.hatena.Processor;
import info.vividcode.text.hatena.ProcessorCursorList;
import info.vividcode.text.hatena.StructuredTextBuilder;
import info.vividcode.text.hatena.TextCursor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ListProcessor implements Processor {

    public static final String LIST_ITEM_NODE_NAME = "list-item";

    private static final Pattern TRIGGER_PATTERN =
            Pattern.compile("^-+", Pattern.MULTILINE);
    private static final Pattern END_PATTERN =
            Pattern.compile("(?!-)");

    @Override
    public Pattern triggerPattern() {
        return TRIGGER_PATTERN;
    }

    @Override
    public void process(
            TextCursor tc,
            StructuredTextBuilder stb,
            ProcessorCursorList pr) {
        String substr = tc.advanceTo(END_PATTERN);
        int level = substr.length();
        Logger.getGlobal().finer("Start list (level " + Integer.toString(level) + ")");
        Map<String,String> attrs = new HashMap<>();
        attrs.put("level", Integer.toString(level));
        stb.openNode(LIST_ITEM_NODE_NAME, attrs);
        pr.registerOnetimeProcessor(new ListEndProcessor(level), level);
    }
}

class ListEndProcessor implements Processor {
    @Override
    public Pattern triggerPattern() {
        return triggerPattern;
    }

    private final Pattern triggerPattern;
    private final int level;

    public ListEndProcessor(int level) {
        this.level = level;
        triggerPattern = Pattern.compile("$", Pattern.MULTILINE);
    }

    @Override
    public void process(
            TextCursor tc,
            StructuredTextBuilder stb,
            ProcessorCursorList pr) {
        Logger.getGlobal().finer("End list (level " + Integer.toString(level) + ")");
        stb.closeNode(ListProcessor.LIST_ITEM_NODE_NAME);
        tc.advanceWith(1); // 改行を除去
    }
}
