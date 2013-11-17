package info.vividcode.text.hatena;

import java.util.regex.Matcher;

class NormalProcessorWithCursor implements ProcessorWithCursor {
    final Matcher matcher;
    private final Processor processor;
    public NormalProcessorWithCursor(Processor p, String in) {
        matcher = p.triggerPattern().matcher(in);
        processor = p;
    }
    @Override
    public boolean next() {
        return matcher.find();
    }
    @Override
    public int pos() {
        return matcher.start();
    }
    @Override
    public void process(
            TextCursor tacp,
            StructuredTextBuilder stacp,
            ProcessorCursorList pr) {
        processor.process(tacp, stacp, pr);
    }
    @Override
    public int priority() {
        return matcher.end() - matcher.start();
    }
}
