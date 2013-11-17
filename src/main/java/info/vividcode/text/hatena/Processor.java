package info.vividcode.text.hatena;

import java.util.regex.Pattern;

public interface Processor {
    public Pattern triggerPattern();
    public void process(
            TextCursor tacp,
            StructuredTextBuilder stacp,
            ProcessorCursorList pr);
}
