package bg.drow.spellbook.ui.desktop;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Simple number filter for swing components, such as
 * text fields.
 *
 * @author <a mailto="bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.1
 */
public class NumberDocument extends PlainDocument {
    public static final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String NUMERIC = "0123456789";
    public static final String ALPHA_NUMERIC = ALPHA + NUMERIC;
    protected String acceptedChars = null;
    protected boolean negativeAccepted = false;

    public NumberDocument() {
        this(NUMERIC);
    }

    public NumberDocument(String acceptedchars) {
        acceptedChars = acceptedchars;
    }

    public void setNegativeAccepted(boolean negativeaccepted) {
        if (acceptedChars.equals(NUMERIC)) {
            negativeAccepted = negativeaccepted;
            acceptedChars += "-";
        }
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) {
            return;
        }
        for (int i = 0; i < str.length(); i++) {
            if (acceptedChars.indexOf(String.valueOf(str.charAt(i))) == -1) {
                return;
            }
        }

        if (negativeAccepted) {
            if (str.indexOf(".") != -1) {
                if (getText(0, getLength()).indexOf(".") != -1) {
                    return;
                }
            }
        }

        if (negativeAccepted && str.indexOf("-") != -1) {
            if (str.indexOf("-") != 0 || offset != 0) {
                return;
            }
        }

        super.insertString(offset, str, attr);
    }
}
