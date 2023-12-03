import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class ConsoleTextArea extends JTextPane {
    private Color defaultColor;

    public ConsoleTextArea() {
        this(300, 200);
    }

    public ConsoleTextArea(int w, int h) {
        super();
        setEditable(false);
        this.defaultColor = Color.BLACK;
        setPreferredSize(new Dimension(w, h));
    }

    public void append(String text, Color color) {
        StyledDocument doc = getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);

        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void append(String text) {
        append(text, this.defaultColor);
    }

    public void clear() {
        setText("");
    }

    public void setDefaultColor(Color color) {
        this.defaultColor = color;
    }
}