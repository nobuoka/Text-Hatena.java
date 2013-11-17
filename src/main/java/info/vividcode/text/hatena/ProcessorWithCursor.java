package info.vividcode.text.hatena;

import java.util.Comparator;

public interface ProcessorWithCursor {
    public boolean next();
    public int pos();
    public int priority();
    public void process(
            TextCursor tacp,
            StructuredTextBuilder stacp,
            ProcessorCursorList pr);

    static Comparator<ProcessorWithCursor> COMPARATOR =
    new Comparator<ProcessorWithCursor>() {
        @Override public int compare(ProcessorWithCursor o1, ProcessorWithCursor o2) {
            return o1.pos() < o2.pos() ? -1 :
                o1.pos() > o2.pos() ?  1 :
                o2.priority() < o1.priority() ? -1 : 1;
        }
    };
}
