package info.vividcode.text.hatena;

class OnetimeProcessorWithCursor extends NormalProcessorWithCursor {
    private int curPos;
    private boolean processDone = false;
    private final int priorityOffset;
    public OnetimeProcessorWithCursor(Processor p, String in, int curPos) {
        this(p, in, curPos, 0);
    }
    public OnetimeProcessorWithCursor(Processor p, String in, int curPos, int priorityOffset) {
        super(p, in);
        this.curPos = curPos;
        this.priorityOffset = priorityOffset;
    }
    @Override
    public boolean next() {
        if (processDone) return false;
        boolean match = (curPos >= 0 ? matcher.find(curPos) : matcher.find());
        curPos = -1;
        return match;
    }
    @Override
    public void process(
            TextCursor tacp,
            StructuredTextBuilder stacp,
            ProcessorCursorList pr) {
        super.process(tacp, stacp, pr);
        processDone = true;
    }
    @Override
    public int priority() {
        return super.priority() + priorityOffset;
    }
}
