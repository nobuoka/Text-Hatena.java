package info.vividcode.text.hatena.processors;

import info.vividcode.text.hatena.Processor;
import info.vividcode.text.hatena.ProcessorCursorList;
import info.vividcode.text.hatena.StructuredTextBuilder;
import info.vividcode.text.hatena.TextCursor;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class HeadlineProcessor implements Processor {

    private static final Pattern TRIGGER_PATTERN =
            Pattern.compile("^[\\*]+", Pattern.MULTILINE);
    private static final Pattern END_PATTERN =
            Pattern.compile("(?!\\*)");

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
        String headlineLevel = Integer.toString(substr.length());
        Logger.getGlobal().finer("Start headline (level: " + headlineLevel + ")");
        Map<String,String> attrs = new HashMap<>();
        attrs.put("level", headlineLevel);
        stb.openNode("headline", attrs);
        pr.registerOnetimeProcessor(new HeadlineEndProcessor());
    }
}

class HeadlineEndProcessor implements Processor {
    private static final Pattern TRIGGER_PATTERN =
            Pattern.compile("$", Pattern.MULTILINE);
    @Override
    public Pattern triggerPattern() {
        return TRIGGER_PATTERN;
    }

    @Override
    public void process(
            TextCursor tc,
            StructuredTextBuilder stb,
            ProcessorCursorList pr) {
        Logger.getGlobal().finer("End headline");
        stb.closeNode("headline");
        tc.advanceWith(1); // 改行を除去
    }
}
