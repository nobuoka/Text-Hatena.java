package info.vividcode.text.hatena;

import java.util.Collection;
import java.util.TreeSet;

public class ProcessorCursorList extends TreeSet<ProcessorWithCursor> {
    private static final long serialVersionUID = -5445284683959685240L;

    TextCursor textCursor;
    public ProcessorCursorList(TextCursor tacp, Collection<Processor> processors) {
        super(ProcessorWithCursor.COMPARATOR);
        this.textCursor = tacp;
        for (Processor p : processors) {
            ProcessorWithCursor pc = new NormalProcessorWithCursor(p, tacp.text);
            if (pc.next()) this.add(pc);
        }
    }
    public void registerOnetimeProcessor(Processor p) {
        ProcessorWithCursor pc =
                new OnetimeProcessorWithCursor(p, textCursor.text, textCursor.pos());
        if (pc.next()) this.add(pc);
    }
    public void registerOnetimeProcessor(Processor p, int priorityOffset) {
        ProcessorWithCursor pc =
                new OnetimeProcessorWithCursor(p, textCursor.text, textCursor.pos(), priorityOffset);
        if (pc.next()) this.add(pc);
    }

    public void progressProcessorCursorsAccordingToTextCursor() {
        while (!this.isEmpty() && this.first().pos() < textCursor.pos()) {
            ProcessorWithCursor pc2 = this.pollFirst();
            if (pc2.next()) this.add(pc2);
        }
    }
}
