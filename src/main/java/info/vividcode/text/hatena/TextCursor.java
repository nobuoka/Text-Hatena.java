package info.vividcode.text.hatena;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文字列 1 つとその文字列中の位置 1 箇所を保持するクラス。
 */
public class TextCursor {
    /** 文字列 */
    final String text;
    /** 文字列中の位置 */
    private int curPos = 0;

    public TextCursor(String text) {
        this.text = text;
    }

    /**
     * @return 現在地が文字列の最後に到達しているかどうか。
     */
    public boolean eof() {
        return text.length() == curPos;
    }

    /**
     * @return 現在地。
     */
    public int pos() {
        return curPos;
    }

    /**
     * 指定の位置まで文字列中の位置を進める。
     * @param pos 進める先の位置。
     * @return 現在地から進めた先の位置までの文字列を返す。
     */
    public String advanceTo(int pos) {
        String substr = text.substring(curPos, pos);
        curPos = pos;
        return substr;
    }

    /**
     * 指定の文字数だけ文字列中の位置を進める。
     * @param length 進める文字数。
     * @return 現在地から進めた先の位置までの文字列を返す。
     */
    public String advanceWith(int length) {
        return advanceTo(curPos + length);
    }

    /**
     * 指定のパターンにマッチする位置まで現在地を進める。
     * パターンにマッチしなかった場合は現在位置を進めず、空文字列を返す。
     * @param p 進める先のパターン。
     * @return 現在地から進めた先の位置までの文字列を返す。 パターンにマッチしなかった場合は空文字列。
     */
    public String advanceTo(Pattern p) {
        Matcher m = p.matcher(text);
        String substr;
        // m.find(curPos) だと先頭に \A がマッチしないけど region で絞ればマッチする
        m.region(curPos, text.length());
        if (m.find()) {
            substr = text.substring(curPos, m.end());
            curPos = m.end();
        } else {
            substr = "";
        }
        return substr;
    }
}
