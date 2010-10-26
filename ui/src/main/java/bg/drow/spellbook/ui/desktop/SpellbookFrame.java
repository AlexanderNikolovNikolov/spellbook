package bg.drow.spellbook.ui.desktop;

import bg.drow.spellbook.core.Inflection;
import bg.drow.spellbook.core.SpellbookConstants;
import bg.drow.spellbook.core.i18n.Translator;
import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.model.Version;
import bg.drow.spellbook.core.preferences.PreferencesManager;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.Lookup;
import bg.drow.spellbook.ui.desktop.exam.ExamDialog;
import bg.drow.spellbook.ui.desktop.game.HangmanDialog;
import bg.drow.spellbook.ui.desktop.study.StudyWordsDialog;
import bg.drow.spellbook.ui.swing.component.WordOfTheDayDialog;
import bg.drow.spellbook.ui.swing.model.ListBackedListModel;
import bg.drow.spellbook.ui.swing.util.IconManager;
import bg.drow.spellbook.ui.swing.util.SwingUtil;
import bg.drow.spellbook.util.CaseInsensitiveStringComparator;
import bg.drow.spellbook.util.SearchUtils;
import com.google.common.collect.Lists;
import com.jidesoft.dialog.StandardDialog;
import com.jidesoft.hints.ListDataIntelliHints;
import com.jidesoft.swing.*;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Spellbook's main application frame.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.1
 */
public class SpellbookFrame extends JFrame {
    public static final Version VERSION = new Version("0.4.0");
    private static final String VERSION_FILE_URL = "http://spellbook-dictionary.googlecode.com/svn/trunk/desktop/app/spellbook-version.txt";

    private static final Logger LOGGER = LoggerFactory.getLogger(SpellbookFrame.class);
    private static final Translator TRANSLATOR = Translator.getTranslator("SpellbookFrame");
    private static final PreferencesManager PM = PreferencesManager.getInstance();
    private List<String> words;
    private ClipboardIntegration clipboardIntegration;
    private ScheduledExecutorService memoryUsageExecutorService;
    private TrayIcon trayIcon;
    private boolean exactMatch = false;
    private List<String> searchedWords = Lists.newArrayList();
    private int searchWordsIndex = -1;
    private static final int BYTES_IN_ONE_MEGABYTE = 1024 * 1024;
    private static DictionaryService dictionaryService;
    private static Dictionary selectedDictionary;

    private JButton backButton;
    private JButton clearButton;
    private JButton copyButton;
    private JMenuItem copyMenuItem;
    private JButton cutButton;
    private JMenuItem cutMenuItem;
    private JButton deleteWordButton;
    private JMenuItem deleteWordMenuItem;
    private JideSplitButton dictionaryButton;
    private JMenu dictionaryMenu;
    private JButton forwardButton;
    private JPanel topPanel;
    private JToolBar.Separator lastToolbarSeparator;
    private JButton memoryButton;
    private JButton pasteButton;
    private JMenuItem pasteMenuItem;
    private JSplitPane splitPane;
    private JButton updateWordButton;
    private JMenuItem updateWordMenuItem;
    private JTextField wordSearchField;
    private JTextPane wordTranslationTextPane;
    private JList wordsList;
    private JLabel wordSearchFieldStatusLabel = new JLabel();
    private boolean initialized = false;
    private static final int DEFAULT_FONT_SIZE = 14;
    private static final int DIVIDER_LOCATION = 180;
    private static final int MIN_FRAME_WIDTH = 640;
    private static final int MIN_FRAME_HEIGHT = 200;
    private JToolBar mainToolBar;
    private JPanel statusBar;
    private JLabel dictionaryInfoLabel;
    private JProgressBar memoryProgressBar;
 
    public SpellbookFrame(boolean dbPresent) {
        setMinimumSize(new Dimension(MIN_FRAME_WIDTH, MIN_FRAME_HEIGHT));

        if (PM.getBoolean(PreferencesManager.Preference.CLOSE_TO_TRAY, false)) {
            LOGGER.info("Minimize to tray on close is enabled");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        } else {
            LOGGER.info("Minimize to tray on close is disabled");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        setAlwaysOnTop(PM.getBoolean(PreferencesManager.Preference.ALWAYS_ON_TOP, false));

        //set the frame title
        setTitle(TRANSLATOR.translate("ApplicationName(Title)"));

        //set the frame icon
        setIconImage(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE16).getImage());

        //create tray
        trayIcon = SpellbookTray.createTraySection(this);

        // implemented a very nasty clipboard ownership hack to simulate notifications
        clipboardIntegration = ClipboardIntegration.getInstance(this);

        if (dbPresent) {
            init();
        }
    }

    public void init() {
        TRANSLATOR.reset();

        File dbFile = new File(SpellbookConstants.SPELLBOOK_DB_PATH);

        if (!dbFile.exists()) {
            SpellbookApp.closeSplashWindow();
            JOptionPane.showMessageDialog(this, TRANSLATOR.translate("MissingDb(Message)", SpellbookConstants.SPELLBOOK_DB_PATH), TRANSLATOR.translate("Error(Title)"), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        Lookup.init();

        dictionaryService = Lookup.lookup(DictionaryService.class);

        // if there are no dictionaries - something is wrong with the database
        if (dictionaryService.getDictionaries().size() == 0) {
            SpellbookApp.closeSplashWindow();
            JOptionPane.showMessageDialog(null, TRANSLATOR.translate("NoDictionaries(Message)"), TRANSLATOR.translate("Error(Title)"), JOptionPane.WARNING_MESSAGE);

            System.exit(0);
        }

        // select default dictionary if set
        String defaultDictionaryName = PM.get(PreferencesManager.Preference.DEFAULT_DICTIONARY, "NONE");

        if (defaultDictionaryName.equals("NONE")) {
            setSelectedDictionary(dictionaryService.getDictionaries().get(0));
        } else {
            setSelectedDictionary(dictionaryService.getDictionary(defaultDictionaryName));
        }

        initComponents();

        initDictionaries();

        addListeners();

        // restore last size and position of the frame
        if (PM.getDouble(PreferencesManager.Preference.FRAME_X, 0.0) > 0) {
            double x = PM.getDouble(PreferencesManager.Preference.FRAME_X, 0.0);
            double y = PM.getDouble(PreferencesManager.Preference.FRAME_Y, 0.0);
            double width = PM.getDouble(PreferencesManager.Preference.FRAME_WIDTH, 0.0);
            double height = PM.getDouble(PreferencesManager.Preference.FRAME_HEIGHT, 0.0);

            setBounds((int) x, (int) y, (int) width, (int) height);
        } else {
            //or dynamically determine an adequate frame size
            Toolkit toolkit = Toolkit.getDefaultToolkit();

            Dimension screenSize = toolkit.getScreenSize();

            setSize(screenSize.width / 2, screenSize.height / 2);
            // center on screen
            setLocationRelativeTo(null);
        }

        initialized = true;
    }

    public void showWordOfTheDay() {
        if (PM.getBoolean(PreferencesManager.Preference.WORD_OF_THE_DAY, true)) {
            WordOfTheDayDialog wordOfTheDayDialog = new WordOfTheDayDialog(this, words, selectedDictionary);
            wordOfTheDayDialog.setLocationRelativeTo(this);
            wordOfTheDayDialog.setVisible(true);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    private void addListeners() {
        // we need this to intercept events such as frame minimize/close
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowIconified(WindowEvent e) {
                if (PM.getBoolean(PreferencesManager.Preference.MIN_TO_TRAY, false)) {
                    LOGGER.info("Minimizing Spellbook to tray");
                    setVisible(false);
                }
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                LOGGER.info("deiconified");
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (PM.getBoolean(PreferencesManager.Preference.CLOSE_TO_TRAY, false)) {
                    LOGGER.info("Minimizing Spellbook to tray on window close");
                    setVisible(false);
                } else {
                    saveFrameState();
                }
            }
        });

        // monitor any changes in the search text field
        wordSearchField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                // needs to be run in a separate thread
                // because we may need to switch the dictionary
                // based on the user input
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        onSearchChange(true);
                    }
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSearchChange(false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSearchChange(false);
            }
        });

        wordSearchField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (wordSearchField.getText().isEmpty()) {
                    clear();
                }
            }
        });

        // needed to update the state of the clipboard controls
        wordSearchField.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                if (wordSearchField.getSelectedText() != null && !wordSearchField.getSelectedText().isEmpty()) {
                    cutButton.setEnabled(true);
                    cutMenuItem.setEnabled(true);
                    copyButton.setEnabled(true);
                    copyMenuItem.setEnabled(true);
                } else {
                    cutButton.setEnabled(false);
                    cutMenuItem.setEnabled(false);
                    copyButton.setEnabled(false);
                    copyButton.setEnabled(false);
                }
            }
        });

        // paste should only work when the focus is in the search field
        wordSearchField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                pasteButton.setEnabled(true);
                pasteMenuItem.setEnabled(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                pasteButton.setEnabled(false);
                pasteMenuItem.setEnabled(false);
                cutButton.setEnabled(false);
                cutMenuItem.setEnabled(false);
            }
        });

        wordTranslationTextPane.addCaretListener(new CaretListener() {

            @Override
            public void caretUpdate(CaretEvent e) {
                if (wordTranslationTextPane.getSelectedText() != null && !wordTranslationTextPane.getSelectedText().isEmpty()) {
                    copyButton.setEnabled(true);
                    copyMenuItem.setEnabled(true);
                } else {
                    copyButton.setEnabled(false);
                    copyButton.setEnabled(false);
                }
            }
        });

        // add the context popup
        ContextMenuMouseListener contextMenuMouseListener = new ContextMenuMouseListener();

        wordSearchField.addMouseListener(contextMenuMouseListener);
        wordTranslationTextPane.addMouseListener(contextMenuMouseListener);
    }

    private void addWordDefinition() throws HeadlessException {
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true, true);
        addUpdateWordDialog.setWhetherAddWord(true);
        addUpdateWordDialog.setDictionary(selectedDictionary);
        addUpdateWordDialog.setLocationRelativeTo(this);
        addUpdateWordDialog.setVisible(true);

        if (addUpdateWordDialog.getDialogResult() == StandardDialog.RESULT_AFFIRMED) {
            if (words.contains(addUpdateWordDialog.getWord())) {
                JOptionPane.showMessageDialog(null, TRANSLATOR.translate("WordAlreadyExists(Message)"), "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int insertionIndex = SearchUtils.findInsertionIndex(words, addUpdateWordDialog.getWord());
            System.out.println("insertion index is " + insertionIndex);
            // this is a references to the cache as well
            words.add(insertionIndex, addUpdateWordDialog.getWord());
            wordsList.setModel(new ListBackedListModel(words));

            dictionaryService.addWord(addUpdateWordDialog.getWord(), addUpdateWordDialog.getTranslation(), selectedDictionary);

            // select the freshly inserted word
            wordsList.setSelectedIndex(insertionIndex);
        }
    }

    private void onSearchChange(boolean insert) {
        clearButton.setEnabled(true);

        String searchString = wordSearchField.getText();

        if (!searchString.isEmpty() && insert) {
            // switches to complementary dictionary if needed
            // this can only happen on insert for obvious reasons
            autoCorrectDictionary(searchString);
        }

        String approximation;

        int index = Collections.binarySearch(words, searchString, new CaseInsensitiveStringComparator());

        // if we have an exact match for the search string or the search string in lowercase
        if (index >= 0) {
            // invoking this method will trigger the list value changed listener,
            // so there is no need to obtain the translation explicitly here
            wordsList.setSelectedIndex(index);
            wordsList.ensureIndexIsVisible(index);

            wordSearchFieldStatusLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
            wordSearchFieldStatusLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));

            exactMatch = true;
        } else if (words.contains(normalize(searchString))) {
            int index2 = words.indexOf(normalize(searchString));

            wordsList.setSelectedIndex(index2);
            wordsList.ensureIndexIsVisible(index2);

            wordSearchFieldStatusLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
            wordSearchFieldStatusLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
        } else if ((approximation = getApproximation(searchString)) != null) {

            int index2 = words.indexOf(approximation);

            wordsList.setSelectedIndex(index2);
            wordsList.ensureIndexIsVisible(index2);

            wordSearchFieldStatusLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ATTENTION));
            wordSearchFieldStatusLabel.setToolTipText(TRANSLATOR.translate("PartialMatchFound(ToolTip)"));

            exactMatch = false;
        } else {
            exactMatch = false;
        }
    }

    public void showMemoryUsage() {
        lastToolbarSeparator.setVisible(true);
        memoryButton.setVisible(true);
        memoryProgressBar.setVisible(true);

        if (memoryUsageExecutorService == null) {
            Runnable memoryRunnable = new Runnable() {

                @Override
                public void run() {
                    final Runtime runtime = Runtime.getRuntime();
                    final long freeMemory = runtime.freeMemory();
                    final long totalMemory = runtime.totalMemory();

                    final int usedMemInMb = (int) (totalMemory - freeMemory) / BYTES_IN_ONE_MEGABYTE;
                    final int totalMemInMb = (int) totalMemory / BYTES_IN_ONE_MEGABYTE;
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            memoryProgressBar.setString(usedMemInMb + "M of " + totalMemInMb + "M");
                            memoryProgressBar.setValue((int) (usedMemInMb * 100 / (double) totalMemInMb));
                            memoryProgressBar.setToolTipText(String.format(TRANSLATOR.translate("MemoryUsage(ToolTip)"), totalMemInMb, usedMemInMb));
                            memoryButton.setToolTipText(String.format(TRANSLATOR.translate("MemoryUsage(ToolTip)"), totalMemInMb, usedMemInMb));
                        }
                    });

                }
            };

            memoryUsageExecutorService = Executors.newSingleThreadScheduledExecutor();
            memoryUsageExecutorService.scheduleAtFixedRate(memoryRunnable, 0, 10, TimeUnit.SECONDS);
        }
    }

    public void hideMemoryUsage() {
        lastToolbarSeparator.setVisible(false);
        memoryButton.setVisible(false);
        memoryProgressBar.setVisible(false);
    }

    private void clear() {
        LOGGER.info("Clear action invoked");

        wordSearchField.setText(null);
        wordSearchField.requestFocusInWindow();
        wordsList.ensureIndexIsVisible(0);
        wordsList.clearSelection();
        updateWordButton.setEnabled(false);
        updateWordMenuItem.setEnabled(false);
        deleteWordButton.setEnabled(false);
        deleteWordMenuItem.setEnabled(false);
        wordTranslationTextPane.setText(null);
        wordSearchFieldStatusLabel.setIcon(null);
        clearButton.setEnabled(false);
    }


    public void clipboardCallback() {
        String transferredText = clipboardIntegration.getClipboardContents().trim();

        // ignore empty text and urls
        if (!transferredText.isEmpty()
                && !transferredText.startsWith("http://")
                && !transferredText.startsWith("mailto:")) {
            LOGGER.info("'" + transferredText + "' received from clipboard");
            String searchString = transferredText.split("\\s")[0].toLowerCase();
            String foundWord = "";
            LOGGER.info("Search string from clipboard is " + searchString);
            wordSearchField.setText(searchString);
            wordSearchField.selectAll();

            String approximation;
            boolean match = false;

            autoCorrectDictionary(searchString);

            int index = Collections.binarySearch(words, searchString, new CaseInsensitiveStringComparator());

            if (index >= 0) {
                foundWord = searchString;

                wordsList.setSelectedIndex(index);
                wordsList.ensureIndexIsVisible(index);

                match = true;

                wordSearchFieldStatusLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
                wordSearchFieldStatusLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
            } else if (words.contains(normalize(searchString))) {
                foundWord = normalize(searchString);
                int index2 = words.indexOf(normalize(searchString));

                wordsList.setSelectedIndex(index2);
                wordsList.ensureIndexIsVisible(index2);

                match = true;

                wordSearchFieldStatusLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
                wordSearchFieldStatusLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
            } else if ((approximation = getApproximation(searchString)) != null) {
                foundWord = approximation;
                int index2 = words.indexOf(foundWord);

                wordsList.setSelectedIndex(index2);
                wordsList.ensureIndexIsVisible(index2);

                match = true;

                wordSearchFieldStatusLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ATTENTION));
                wordSearchFieldStatusLabel.setToolTipText(TRANSLATOR.translate("PartialMatchFound(ToolTip)"));
            }

            // the tray popup translation should appear is the main frame is not active and not always on top
            if ((trayIcon != null) && match && !SpellbookFrame.this.isActive() && !SpellbookFrame.this.isAlwaysOnTop()
                    && PM.getBoolean(PreferencesManager.Preference.TRAY_POPUP, true)) {
                trayIcon.displayMessage(foundWord, dictionaryService.getTranslation((String) wordsList.getSelectedValue(), selectedDictionary),
                        TrayIcon.MessageType.INFO);
            }
        }
    }


    public String getApproximation(String searchKey) {
        if (searchKey != null && !searchKey.isEmpty()) {

            LOGGER.info("Getting approximation for " + searchKey);

            final int index = SearchUtils.findInsertionIndex(words, searchKey);

            // special consideration must be take if the insertion index is past the last index
            return words.get(index == words.size() ? index - 1 : index);
        }

        return null;
    }

    public void restart() {
        this.dispose();
        SpellbookTray.destroyTrayIcon();
        SpellbookApp.init();
    }

    public void setDefaultFont() {
        if (PM.get(PreferencesManager.Preference.FONT_NAME, "").isEmpty()) {
            // dirty fix for windows - it seem that the default font there is too small, so we set
            // a more appropriate one
            String osName = System.getProperty("os.name");

            if (osName.contains("Windows")) {
                wordTranslationTextPane.setFont(new Font("Arial", Font.PLAIN, DEFAULT_FONT_SIZE));
            }
        } else {
            String fontName = PM.get(PreferencesManager.Preference.FONT_NAME, "SansSerif");
            int fontSize = PM.getInt(PreferencesManager.Preference.FONT_SIZE, DEFAULT_FONT_SIZE);
            int fontStyle = PM.getInt(PreferencesManager.Preference.FONT_STYLE, Font.PLAIN);

            setSelectedFont(new Font(fontName, fontStyle, fontSize));
        }
    }

    private void onWordSearchFieldAction() {
        wordSearchField.selectAll();
        if (exactMatch) {
            LOGGER.info("Attempting to add " + wordSearchField.getText() + " to completions list");

            // don't add consecutively the same word
            if (searchedWords.isEmpty() || !searchedWords.get(searchedWords.size() - 1).equals(wordSearchField.getText())) {
                searchedWords.add(wordSearchField.getText());
                searchWordsIndex = searchedWords.size();
                backButton.setEnabled(true);
                forwardButton.setEnabled(false);
            }
        }
    }

    private void saveFrameState() {
        Rectangle r = getBounds();
        PM.putDouble(PreferencesManager.Preference.FRAME_X, r.getX());
        PM.putDouble(PreferencesManager.Preference.FRAME_Y, r.getY());
        PM.putDouble(PreferencesManager.Preference.FRAME_WIDTH, r.getWidth());
        PM.putDouble(PreferencesManager.Preference.FRAME_HEIGHT, r.getHeight());
        PM.putInt(PreferencesManager.Preference.DIVIDER_LOCATION, splitPane.getDividerLocation());
    }

    private void updateWordDefinition() throws IllegalStateException {
        AddUpdateWordDialog addUpdateWordDialog = new AddUpdateWordDialog(this, true, false);
        addUpdateWordDialog.setDictionary(selectedDictionary);

        if (wordsList.isSelectionEmpty()) {
            throw new IllegalStateException("No word selected");
        }

        final String word = (String) wordsList.getSelectedValue();
        addUpdateWordDialog.setWord(word);
        addUpdateWordDialog.setTranslation(dictionaryService.getTranslation(word, selectedDictionary));
        addUpdateWordDialog.setLocationRelativeTo(this);
        addUpdateWordDialog.setVisible(true);

        if (addUpdateWordDialog.getDialogResult() == StandardDialog.RESULT_AFFIRMED) {
            String newTranslation = addUpdateWordDialog.getTranslation();

            dictionaryService.updateWord(word, newTranslation, selectedDictionary);

            // the translation was changed - we need to update it manually
            wordTranslationTextPane.setText(SwingUtil.formatTranslation(word, newTranslation));
        }
    }

    private void setSelectedDictionary(Dictionary dictionary) {
        selectedDictionary = dictionary;
        words = dictionaryService.getWordsFromDictionary(dictionary);
    }

    private void updateDictionaryButton(Dictionary dictionary) {
        dictionaryButton.setToolTipText(TRANSLATOR.translate("DictSize(Label)", selectedDictionary, words.size()));
        dictionaryButton.setIcon(new ImageIcon(dictionary.getIconBig()));

        dictionaryInfoLabel.setText(TRANSLATOR.translate("DictSize(Label)", selectedDictionary, words.size()));
        dictionaryInfoLabel.setIcon(new ImageIcon(dictionary.getIconSmall()));
    }

    public void setSelectedFont(Font font) {
        wordSearchField.setFont(font);
        wordsList.setFont(font);
        wordTranslationTextPane.setFont(font);
    }

    public void selectDictionary(Dictionary dictionary, boolean clear) {
        // if we select the currently selected dictionary we don't have to do nothing
        if (selectedDictionary == dictionary) {
            LOGGER.info("Dictionary " + dictionary + " is already selected");
            return;
        }

        if (clear) {
            // otherwise begin the switch to the new dictionary by cleaning everything in the UI
            clear();
        }

        setSelectedDictionary(dictionary);

        wordsList.setModel(new ListBackedListModel(words));
        updateDictionaryButton(dictionary);

        SwingUtil.showBalloonTip(dictionaryInfoLabel, TRANSLATOR.translate("DictLoaded(Message)", selectedDictionary.toString()));
    }

    private void initComponents() {
        topPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));

        // this is where the magic happens
        setContentPane(topPanel);

        splitPane = new JSplitPane();

        wordsList = new JList();
        wordSearchField = new OverlayTextField();

        splitPane.setBorder(null);
        splitPane.setDividerLocation(DIVIDER_LOCATION);

        topPanel.add(splitPane, "grow");

        wordsList.setModel(new ListBackedListModel(words));
        wordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    updateWordDefinition();
                }
            }
        });
        wordsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent evt) {
                // TODO refine synchronization
                if (!wordsList.isSelectionEmpty()) {
                    // word field needs to be updated in a separate thread
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            onWordSelectionChange();
                        }
                    });
                }
            }
        });

        wordSearchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                onWordSearchFieldAction();
            }
        });

        JPanel searchPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[][grow]"));

        searchPanel.add(new DefaultOverlayable(wordSearchField, wordSearchFieldStatusLabel, DefaultOverlayable.SOUTH_EAST), "growx");
        searchPanel.add(new JScrollPane(wordsList), "grow");

        splitPane.setLeftComponent(searchPanel);

        JPanel translationPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));
        wordTranslationTextPane = new JTextPane();

        wordTranslationTextPane.setContentType("text/html");
        wordTranslationTextPane.setEditable(false);

        translationPanel.add(new JScrollPane(wordTranslationTextPane), "grow");

        splitPane.setRightComponent(translationPanel);

        initToolBar();

        initMenuBar();

        initStatusBar();

        // restore the divider location from the last session
        splitPane.setDividerLocation(PM.getInt(PreferencesManager.Preference.DIVIDER_LOCATION, DIVIDER_LOCATION));

        updateDictionaryButton(selectedDictionary);

        // setup intellihints for words search field
        ListDataIntelliHints<String> intelliHints = new ListDataIntelliHints<String>(wordSearchField, searchedWords);
        intelliHints.setCaseSensitive(false);

        setDefaultFont();

        if (PM.getBoolean(PreferencesManager.Preference.SHOW_MEMORY_USAGE, false)) {
            showMemoryUsage();
        } else {
            hideMemoryUsage();
        }

        initKeyboardShortcuts();

        pack();
    }

    private void initKeyboardShortcuts() {
        Action goToSearchField = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                wordSearchField.requestFocusInWindow();
                wordSearchField.selectAll();
            }
        };

        topPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"),
                "goToSearchField");
        topPanel.getActionMap().put("goToSearchField",
                goToSearchField);

        Action showHelp = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelpContents();
            }
        };

        topPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F1"),
                "showHelp");
        topPanel.getActionMap().put("showHelp",
                showHelp);

        Action addWord = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addWordDefinition();
            }
        };

        topPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"),
                "addWord");
        topPanel.getActionMap().put("addWord",
                addWord);


        Action editWord = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateWordDefinition();
            }
        };

        topPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F6"),
                "editWord");
        topPanel.getActionMap().put("editWord",
                editWord);

        Action deleteWord = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteWordDefinition();
            }
        };

        topPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"),
                "deleteWord");
        topPanel.getActionMap().put("deleteWord",
                deleteWord);

        Action clear = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        };

        topPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F2"),
                "clear");
        topPanel.getActionMap().put("clear",
                clear);
    }

    private void initMenuBar() {
        JMenuBar spellbookMenuBar = new JMenuBar();

        // build the file menu
        JMenu fileMenu = new JMenu();
        JMenuItem restartMenuItem = new JMenuItem();
        JMenuItem exitMenuItem = new JMenuItem();

        fileMenu.setMnemonic('f');
        fileMenu.setText(TRANSLATOR.translate("File(Menu)"));

        restartMenuItem.setIcon(IconManager.getMenuIcon("refresh.png"));
        restartMenuItem.setMnemonic('r');
        restartMenuItem.setText(TRANSLATOR.translate("FileRestart(MenuItem)"));
        restartMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                restart();
            }
        });
        fileMenu.add(restartMenuItem);

        exitMenuItem.setIcon(IconManager.getMenuIcon("exit.png"));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText(TRANSLATOR.translate("FileExit(MenuItem)"));
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                saveFrameState();

                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);

        spellbookMenuBar.add(fileMenu);

        // build the edit menu
        JMenu editMenu = new JMenu();
        JMenuItem addWordMenuItem = new JMenuItem();
        updateWordMenuItem = new JMenuItem();
        deleteWordMenuItem = new JMenuItem();
        cutMenuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        copyMenuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        pasteMenuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        JMenuItem prefsMenuItem = new JMenuItem();

        editMenu.setMnemonic('e');
        editMenu.setText(TRANSLATOR.translate("Edit(Menu)"));

        addWordMenuItem.setIcon(IconManager.getMenuIcon("add2.png"));
        addWordMenuItem.setText(TRANSLATOR.translate("EditAddWord(MenuItem)"));
        addWordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addWordDefinition();
            }
        });
        editMenu.add(addWordMenuItem);

        updateWordMenuItem.setIcon(IconManager.getMenuIcon("edit.png"));
        updateWordMenuItem.setText(TRANSLATOR.translate("EditUpdateWord(MenuItem)"));
        updateWordMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateWordDefinition();
            }
        });
        editMenu.add(updateWordMenuItem);

        deleteWordMenuItem.setIcon(IconManager.getMenuIcon("delete2.png"));
        deleteWordMenuItem.setText(TRANSLATOR.translate("EditDeleteWord(MenuItem)"));
        editMenu.add(deleteWordMenuItem);
        editMenu.add(new JPopupMenu.Separator());

        cutMenuItem.setIcon(IconManager.getMenuIcon("cut.png"));
        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText(TRANSLATOR.translate("EditCut(MenuItem)"));
        editMenu.add(cutMenuItem);

        copyMenuItem.setIcon(IconManager.getMenuIcon("copy.png"));
        copyMenuItem.setMnemonic('c');
        copyMenuItem.setText(TRANSLATOR.translate("EditCopy(MenuItem)"));
        editMenu.add(copyMenuItem);

        pasteMenuItem.setIcon(IconManager.getMenuIcon("paste.png"));
        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText(TRANSLATOR.translate("EditPaste(MenuItem)"));
        editMenu.add(pasteMenuItem);
        editMenu.add(new JPopupMenu.Separator());

        prefsMenuItem.setIcon(IconManager.getMenuIcon("preferences.png"));
        prefsMenuItem.setMnemonic('e');
        prefsMenuItem.setText(TRANSLATOR.translate("EditPreferences(MenuItem)"));
        prefsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showPreferencesDialog();
            }
        });
        editMenu.add(prefsMenuItem);

        spellbookMenuBar.add(editMenu);

        //build the vew menu
        JMenu viewMenu = new JMenu();

        viewMenu.setMnemonic('v');
        viewMenu.setText(TRANSLATOR.translate("View(Menu)"));
        spellbookMenuBar.add(viewMenu);

        final JMenuItem toolBarMenuItem = new JCheckBoxMenuItem();
        toolBarMenuItem.setText(TRANSLATOR.translate("ToolBar(MenuItem)"));
        toolBarMenuItem.setSelected(PM.getBoolean(PreferencesManager.Preference.SHOW_TOOLBAR, true));

        toolBarMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!toolBarMenuItem.isSelected()) {
                    topPanel.remove(mainToolBar);
                    PM.putBoolean(PreferencesManager.Preference.SHOW_TOOLBAR, false);
                } else {
                    topPanel.add(mainToolBar, "north, growx");
                    PM.putBoolean(PreferencesManager.Preference.SHOW_TOOLBAR, true);
                }

                topPanel.validate();
            }
        }
        );

        viewMenu.add(toolBarMenuItem);

        final JMenuItem statusBarMenuItem = new JCheckBoxMenuItem();
        statusBarMenuItem.setText(TRANSLATOR.translate("StatusBar(MenuItem)"));
        statusBarMenuItem.setSelected(PM.getBoolean(PreferencesManager.Preference.SHOW_STATUSBAR, true));
        viewMenu.add(statusBarMenuItem);

        statusBarMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!statusBarMenuItem.isSelected()) {
                    topPanel.remove(statusBar);
                    PM.putBoolean(PreferencesManager.Preference.SHOW_STATUSBAR, false);
                } else {
                    topPanel.add(statusBar, "south, growx");
                    PM.putBoolean(PreferencesManager.Preference.SHOW_STATUSBAR, true);
                }

                topPanel.validate();
            }
        }
        );

        // build dictionary menu
        dictionaryMenu = new JMenu();

        dictionaryMenu.setMnemonic('d');
        dictionaryMenu.setText(TRANSLATOR.translate("Dictionaries(Menu)"));
        spellbookMenuBar.add(dictionaryMenu);

        // build tools menu
        JMenu toolMenu = new JMenu();
        JMenuItem studyWordsMenuItem = new JMenuItem();
        JMenuItem examMenuItem = new JMenuItem();

        toolMenu.setMnemonic('t');
        toolMenu.setText(TRANSLATOR.translate("Tools(Menu)"));

        studyWordsMenuItem.setIcon(IconManager.getMenuIcon("teacher.png"));
        studyWordsMenuItem.setMnemonic('w');
        studyWordsMenuItem.setText(TRANSLATOR.translate("StudyWords(MenuItem)"));
        studyWordsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showStudyDialog();
            }
        });
        toolMenu.add(studyWordsMenuItem);

        examMenuItem.setIcon(IconManager.getMenuIcon("blackboard.png"));
        examMenuItem.setMnemonic('e');
        examMenuItem.setText(TRANSLATOR.translate("Exam(MenuItem)"));
        examMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showExamDialog();
            }
        });
        toolMenu.add(examMenuItem);

        spellbookMenuBar.add(toolMenu);

        JMenu gamesMenu = new JMenu(TRANSLATOR.translate("Games(Menu)"));
        gamesMenu.setMnemonic('g');

        JMenuItem hangmanMenuItem = new JMenuItem(TRANSLATOR.translate("GamesHangman(MenuItem)"));

        hangmanMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHangmanDialog();
            }
        });

        hangmanMenuItem.setIcon(IconManager.getMenuIcon("brain.png"));

        gamesMenu.add(hangmanMenuItem);

        spellbookMenuBar.add(gamesMenu);

        // build help menu
        JMenu helpMenu = new JMenu();
        JMenuItem helpContentsMenuItem = new JMenuItem();
        JMenuItem aboutMenuItem = new JMenuItem();

        helpMenu.setMnemonic('h');
        helpMenu.setText(TRANSLATOR.translate("Help(Menu)"));

        helpContentsMenuItem.setIcon(IconManager.getMenuIcon("help2.png"));
        helpContentsMenuItem.setText(TRANSLATOR.translate("HelpContents(MenuItem)"));

        helpContentsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelpContents();
            }
        });

        helpMenu.add(helpContentsMenuItem);

        JMenuItem reportBugMenuItem = new JMenuItem(TRANSLATOR.translate("HelpReportBug(MenuItem)"), IconManager.getMenuIcon("bug-mail.png"));

        reportBugMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Desktop.getDesktop().browse(new URI(SpellbookConstants.REPORT_ISSUE_URL));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

        helpMenu.add(reportBugMenuItem);

        JMenuItem checkForUpdates = new JMenuItem(TRANSLATOR.translate("HelpCheckForUpdates(MenuItem)"), IconManager.getMenuIcon("telescope.png"));

        checkForUpdates.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                checkForUpdates(false);

            }
        });

        helpMenu.add(checkForUpdates);

        JMenuItem wordOfTheDay = new JMenuItem(TRANSLATOR.translate("WordOfTheDay(MenuItem)"), IconManager.getMenuIcon("lightbulb_on.png"));

        wordOfTheDay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WordOfTheDayDialog wordOfTheDayDialog = new WordOfTheDayDialog(SpellbookFrame.this, words, selectedDictionary);
                wordOfTheDayDialog.showDialog();
            }
        });

        helpMenu.add(wordOfTheDay);

        aboutMenuItem.setIcon(IconManager.getMenuIcon("about.png"));
        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText(TRANSLATOR.translate("HelpAbout(MenuItem)"));
        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showAboutDialog();
            }
        });
        helpMenu.add(aboutMenuItem);

        spellbookMenuBar.add(helpMenu);

        cutMenuItem.setEnabled(false);
        // update word menu item is initially disabled
        updateWordMenuItem.setEnabled(false);
        deleteWordMenuItem.setEnabled(false);

        setJMenuBar(spellbookMenuBar);
    }

    public void checkForUpdates(boolean startup) {
        try {
            URL versionUrl = new URL(VERSION_FILE_URL);

            Scanner in = new Scanner(versionUrl.openStream());

            Version availableVersion = new Version(in.next());

            if (VERSION.compareTo(availableVersion) < 0) {
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("NewVersion(Message)", availableVersion),
                        TRANSLATOR.translate("NewVersion(Title)"), JOptionPane.INFORMATION_MESSAGE);
            } else if (!startup) {
                JOptionPane.showMessageDialog(this, TRANSLATOR.translate("NoNewVersion(Message)"),
                        TRANSLATOR.translate("NoNewVersion(Title)"), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initToolBar() {
        mainToolBar = new JToolBar();
        backButton = new JButton();
        forwardButton = new JButton();
        clearButton = new JButton();
        dictionaryButton = new JideSplitButton();
        dictionaryButton.setButtonStyle(JideSplitButton.TOOLBAR_STYLE);
        JButton addWordButton = new JButton();
        updateWordButton = new JButton();
        deleteWordButton = new JButton();
        JToolBar.Separator jSeparator4 = new JToolBar.Separator();
        cutButton = new JButton(new DefaultEditorKit.CutAction());
        cutButton.setText(null);
        copyButton = new JButton(new DefaultEditorKit.CopyAction());
        copyButton.setText(null);
        pasteButton = new JButton(new DefaultEditorKit.PasteAction());
        pasteButton.setText(null);
        JButton studyButton = new JButton();
        JButton examButton = new JButton();
        lastToolbarSeparator = new JToolBar.Separator();
        memoryButton = new JButton();

        mainToolBar.setFloatable(false);
        mainToolBar.setRollover(true);

        backButton.setIcon(IconManager.getImageIcon("arrow_left_blue.png", IconManager.IconSize.SIZE24));
        backButton.setToolTipText(TRANSLATOR.translate("PreviousWord(Label)"));
        backButton.setFocusable(false);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (searchedWords.size() > 0 && searchWordsIndex >= 1) {
                    wordSearchField.setText(searchedWords.get(--searchWordsIndex));

                    updateHistoryButtonsState();
                }
            }
        });
        mainToolBar.add(backButton);

        forwardButton.setIcon(IconManager.getImageIcon("arrow_right_blue.png", IconManager.IconSize.SIZE24));
        forwardButton.setToolTipText(TRANSLATOR.translate("NextWord(Label)"));
        forwardButton.setFocusable(false);
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (searchedWords.size() - 1 > searchWordsIndex) {
                    wordSearchField.setText(searchedWords.get(++searchWordsIndex));

                    updateHistoryButtonsState();
                }
            }
        });
        mainToolBar.add(forwardButton);

        clearButton.setIcon(IconManager.getImageIcon("eraser.png", IconManager.IconSize.SIZE24));
        clearButton.setToolTipText(TRANSLATOR.translate("ClearButton(ToolTip)"));
        clearButton.setFocusable(false);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                clear();
            }
        });
        mainToolBar.add(clearButton);

        dictionaryButton.setIcon(new ImageIcon(selectedDictionary.getIconBig()));
        dictionaryButton.setFocusable(false);
        dictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if (dictionaryService.isComplemented(selectedDictionary)) {
                    selectDictionary(dictionaryService.getComplement(selectedDictionary), true);
                }
            }
        });

        final List<Dictionary> availableDictionaries = dictionaryService.getDictionaries();
        for (Dictionary dictionary : availableDictionaries) {
            dictionaryButton.add(new DictionaryItem(dictionary));
        }

        mainToolBar.add(dictionaryButton);

        JButton syncButton = new JButton();
        syncButton.setFocusable(false);
        syncButton.setIcon(IconManager.getImageIcon("replace2.png", IconManager.IconSize.SIZE24));
        syncButton.setToolTipText(TRANSLATOR.translate("Sync(ToolTip)"));

        syncButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSyncDialog();
            }
        });

        mainToolBar.add(syncButton);
        mainToolBar.add(new JToolBar.Separator());

        addWordButton.setIcon(IconManager.getImageIcon("add2.png", IconManager.IconSize.SIZE24));
        addWordButton.setToolTipText(TRANSLATOR.translate("EditAddWord(MenuItem)"));
        addWordButton.setFocusable(false);
        addWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                addWordDefinition();
            }
        });
        mainToolBar.add(addWordButton);

        updateWordButton.setIcon(IconManager.getImageIcon("edit.png", IconManager.IconSize.SIZE24));
        updateWordButton.setToolTipText(TRANSLATOR.translate("EditUpdateWord(MenuItem)"));
        updateWordButton.setFocusable(false);
        updateWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                updateWordDefinition();
            }
        });
        mainToolBar.add(updateWordButton);

        deleteWordButton.setIcon(IconManager.getImageIcon("delete2.png", IconManager.IconSize.SIZE24));
        deleteWordButton.setToolTipText(TRANSLATOR.translate("EditDeleteWord(MenuItem)"));
        deleteWordButton.setFocusable(false);
        deleteWordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                deleteWordDefinition();
            }
        });
        mainToolBar.add(deleteWordButton);
        mainToolBar.add(jSeparator4);

        cutButton.setIcon(IconManager.getImageIcon("cut.png", IconManager.IconSize.SIZE24));
        cutButton.setToolTipText(TRANSLATOR.translate("EditCut(MenuItem)"));
        cutButton.setFocusable(false);
        mainToolBar.add(cutButton);

        copyButton.setIcon(IconManager.getImageIcon("copy.png", IconManager.IconSize.SIZE24));
        copyButton.setToolTipText(TRANSLATOR.translate("EditCopy(MenuItem)"));
        copyButton.setFocusable(false);
        mainToolBar.add(copyButton);

        pasteButton.setIcon(IconManager.getImageIcon("paste.png", IconManager.IconSize.SIZE24));
        pasteButton.setToolTipText(TRANSLATOR.translate("EditPaste(MenuItem)"));
        pasteButton.setFocusable(false);
        mainToolBar.add(pasteButton);
        mainToolBar.add(new JToolBar.Separator());

        studyButton.setIcon(IconManager.getImageIcon("teacher.png", IconManager.IconSize.SIZE24));
        studyButton.setToolTipText(TRANSLATOR.translate("StudyWords(MenuItem)"));
        studyButton.setFocusable(false);
        studyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showStudyDialog();
            }
        });
        mainToolBar.add(studyButton);

        examButton.setIcon(IconManager.getImageIcon("blackboard.png", IconManager.IconSize.SIZE24));
        examButton.setToolTipText(TRANSLATOR.translate("Exam(MenuItem)"));
        examButton.setFocusable(false);
        examButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                showExamDialog();
            }
        });
        mainToolBar.add(examButton);

        mainToolBar.add(lastToolbarSeparator);

        memoryButton.setIcon(IconManager.getImageIcon("memory.png", IconManager.IconSize.SIZE24));
        memoryButton.setFocusable(false);
        memoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                System.gc();
            }
        });
        mainToolBar.add(memoryButton);

        // history buttons should be disabled initially
        forwardButton.setEnabled(false);
        backButton.setEnabled(false);

        // clear button is disabled initially
        clearButton.setEnabled(false);

        cutButton.setEnabled(false);
        copyButton.setEnabled(false);
        copyButton.setEnabled(false);

        updateWordButton.setEnabled(false);
        deleteWordButton.setEnabled(false);

        if (PM.getBoolean(PreferencesManager.Preference.SHOW_TOOLBAR, true)) {
            topPanel.add(mainToolBar, "north, growx");
        }
    }

    private void initStatusBar() {
        statusBar = new JPanel(new MigLayout("wrap 2", "[grow][]"));
        dictionaryInfoLabel = new JLabel(new ImageIcon(selectedDictionary.getIconBig()));
        dictionaryInfoLabel.setText(TRANSLATOR.translate("DictSize(Label)", selectedDictionary, words.size()));
        statusBar.add(dictionaryInfoLabel);

        memoryProgressBar = new JProgressBar(0, 100);
        memoryProgressBar.setStringPainted(true);

        statusBar.add(memoryProgressBar, "right");

        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

        if (PM.getBoolean(PreferencesManager.Preference.SHOW_STATUSBAR, true)) {
            topPanel.add(statusBar, "south, growx");
        }
    }

    private void showPreferencesDialog() {
        PreferencesDialog preferencesDialog = new PreferencesDialog(this, true);

        // tray options should be disabled is the tray is not supported
        if (trayIcon == null) {
            preferencesDialog.disableTrayOptions();
        }

        preferencesDialog.setLocationRelativeTo(this);

        PreferencesExtractor.extract(this, preferencesDialog);
    }

    private void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }

    private void showStudyDialog() {
        StudyWordsDialog studyWords = new StudyWordsDialog(this, true);
        studyWords.setLocationRelativeTo(this);
        studyWords.showDialog();
    }

    private void showExamDialog() {
        ExamDialog examDialog = new ExamDialog(this, true);
        examDialog.showDialog();
    }

    private void showHangmanDialog() {
        HangmanDialog hangmanDialog = new HangmanDialog(this, true);
        hangmanDialog.showDialog();
    }

    private void showSyncDialog() {
        SyncDialog syncDialog = new SyncDialog(this, true);
        syncDialog.showDialog();
    }

    private void showImportDialog() {
        ImportDialog importDialog = new ImportDialog(this, true);
        importDialog.showDialog();
    }

    private void initDictionaries() {
        dictionaryMenu.removeAll();

        final List<Dictionary> availableDictionaries = dictionaryService.getDictionaries();
        for (Dictionary dictionary : availableDictionaries) {
            dictionaryMenu.add(new DictionaryItem(dictionary));
        }

        dictionaryMenu.add(new JSeparator());

        JMenuItem syncMenuItem = new JMenuItem(TRANSLATOR.translate("Sync(MenuItem)"), IconManager.getMenuIcon("replace2.png"));

        syncMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSyncDialog();
            }
        });

        dictionaryMenu.add(syncMenuItem);

        JMenuItem importMenuItem = new JMenuItem(TRANSLATOR.translate("Import(MenuItem)"), IconManager.getMenuIcon("import1.png"));

        importMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showImportDialog();
            }
        });

        dictionaryMenu.add(importMenuItem);
    }

    private void autoCorrectDictionary(String searchString) {
        if (searchString == null || searchString.isEmpty()) {
            return;
        }

        Language from = selectedDictionary.getFromLanguage();
        boolean valid = true;

        for (String letter : explodeString(searchString)) {
            if (!from.getAlphabet().contains(letter.toLowerCase())) {
                valid = false;
                break;
            }
        }

        if (!valid && dictionaryService.isComplemented(selectedDictionary)) {
            Language to = selectedDictionary.getToLanguage();
            valid = true;

            for (String letter : explodeString(searchString)) {
                if (!to.getAlphabet().contains(letter.toLowerCase())) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                LOGGER.info("Auto switching to complementing dictionary...");
                selectDictionary(dictionaryService.getComplement(selectedDictionary), false);
            }
        }
    }

    private List<String> explodeString(String string) {
        List<String> result = Lists.newArrayList();

        for (int i = 0; i < string.length(); i++) {
            result.add(string.substring(i, i + 1));
        }

        return result;
    }

    private void deleteWordDefinition() {
        if (JOptionPane.showConfirmDialog(this, TRANSLATOR.translate("ConfirmWordDeletion(Message)"),
                TRANSLATOR.translate("ConfirmWordDeletion(Title)"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {

            String selectedWord = (String) wordsList.getSelectedValue();
            int selectedIndex = wordsList.getSelectedIndex();

            words.remove(selectedWord);
            wordsList.setModel(new ListBackedListModel(words));
            // this selects the word after the deleted word
            wordsList.setSelectedIndex(selectedIndex);
            dictionaryService.deleteWord(selectedWord, selectedDictionary);
        }
    }

    private void showHelpContents() {
        try {
            Desktop.getDesktop().browse(new URI(SpellbookConstants.HELP_URL + (Locale.getDefault().getCountry().equals("BG") ? "Bulgarian" : "English")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private class DictionaryItem extends JMenuItem implements ActionListener {

        private final String dictionaryName;

        public DictionaryItem(Dictionary dictionary) {
            if (dictionary == null) {
                throw new IllegalArgumentException("dictionary is null");
            }

            dictionaryName = dictionary.getName();
            setIcon(new ImageIcon(dictionary.getIconSmall()));
            setText(dictionary.toString());
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selectDictionary(dictionaryService.getDictionary(dictionaryName), true);
        }
    }

    private synchronized void onWordSelectionChange() {
        final int selectedIndex = wordsList.getSelectedIndex();

        // safeguard against no selection
        if (selectedIndex < 0) {
            return;
        }

        final String selectedWord = words.get(selectedIndex);

        // some things should happen only if the user is selecting words directly from the words list
        if (!wordSearchField.hasFocus()) {
            wordSearchField.setText(selectedWord);

            wordSearchFieldStatusLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
            wordSearchFieldStatusLabel.setToolTipText(TRANSLATOR.translate("MatchFound(ToolTip)"));
        }

        wordTranslationTextPane.setText(SwingUtil.formatTranslation(selectedWord, dictionaryService.getTranslation(words.get(selectedIndex), selectedDictionary)));
        wordTranslationTextPane.setCaretPosition(0);

        // words can be updated only when selected
        updateWordMenuItem.setEnabled(true);
        updateWordButton.setEnabled(true);

        deleteWordButton.setEnabled(true);
    }

    private void updateHistoryButtonsState() {
        backButton.setEnabled(searchWordsIndex > 0);
        forwardButton.setEnabled(searchWordsIndex < searchedWords.size() - 1);
    }

    private String normalize(String word) {
        Language from = selectedDictionary.getFromLanguage();

        switch (from) {
            case ENGLISH:
                // todo make this segment more elegant/generic
                if (word.endsWith("ing")) {
                    return word.substring(0, word.length() - 3);
                } else if (word.endsWith("ed")) {
                    return word.substring(0, word.length() - 2);
                } else {
                    return Inflection.singularize(word);
                }
            default: return word;
        }
    }
}
