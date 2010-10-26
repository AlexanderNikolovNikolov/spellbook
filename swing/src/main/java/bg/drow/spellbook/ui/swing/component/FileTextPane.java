package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.core.i18n.Translator;
import org.slf4j.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;

/**
 * @author ikkari
 *         Date: Jun 14, 2010
 *         Time: 5:02:52 PM
 */
public class FileTextPane extends JTextPane {

    private File file;
    private NoFileHandler handler;
    private boolean contentChanged = false;
    private String fileName = TRANSLATOR.translate("UnsavedDocument");
    private DocumentListener documentListener = new FileTextPaneDocumentListener();

    private final static Logger LOGGER = LoggerFactory.getLogger(FileTextPane.class);
    private final static Translator TRANSLATOR = Translator.getTranslator("FileTextPane");

    public FileTextPane(File file) throws IOException {
        init();
        setFile(file);
        readFromFile(file);
    }

    public FileTextPane() {
        init();
        attachDocumentListener();
    }

    private void attachDocumentListener() {
        contentChanged = false;
        getDocument().addDocumentListener(documentListener);
    }

    private void init() {
        setBackground(Color.WHITE);
    }

    private void setFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }

        if (!file.isFile()) {
            throw new IllegalArgumentException("file is not file");
        }


        this.file = file;
        this.fileName = file.getName();
    }

    public void open(File file) throws IOException {
        setFile(file);
        readFromFile(file);
    }

    public void save() throws IOException {
        if (!contentChanged) return;

        if (file == null) {
            saveAs();
        }
        writeToFile(file);
    }

    public void saveAs() throws IOException {
        if (handler == null) {
            throw new IllegalStateException("no NoFileHandler set");
        }
        File file0 = handler.handle();
        if (file0 == null)
            return;

        if (!file0.createNewFile()) {

        }
        setFile(file0);
        writeToFile(file0);
    }

    public boolean isSaved() {
        return !contentChanged;
    }

    private void readFromFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }

        if (!file.canRead()) {
            throw new IllegalArgumentException("file is not readable");
        }


        Reader reader = new BufferedReader(new FileReader(file));
        try {
            read(reader, null);
        } finally {
            try {
                reader.close();
                attachDocumentListener();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private void writeToFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }

        if (!file.canWrite()) {
            throw new IOException("file is not writable");
        }

        Writer writer = new BufferedWriter(new FileWriter(file));
        try {
            writer.write(this.getText());
        }
        finally {
            try {
                writer.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        contentChanged = false;
    }


    public String getFileName() {
        return fileName;
    }

    public void setHandler(NoFileHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler is null");
        }
        this.handler = handler;
    }

    public static interface NoFileHandler {
        File handle();
    }

    private class FileTextPaneDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            contentChanged = true;
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            contentChanged = true;
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            contentChanged = true;
        }
    }
}

