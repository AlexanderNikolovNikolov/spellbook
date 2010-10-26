package bg.drow.spellbook.ui.desktop.game;

import bg.drow.spellbook.core.model.Dictionary;
import bg.drow.spellbook.core.model.Difficulty;
import bg.drow.spellbook.core.model.Language;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.exam.ExamService;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import bg.drow.spellbook.ui.swing.component.DictionaryComboBox;
import bg.drow.spellbook.ui.swing.component.DifficultyComboBox;
import bg.drow.spellbook.ui.swing.util.IconManager;
import com.google.common.collect.Sets;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.swing.DefaultOverlayable;
import com.jidesoft.swing.OverlayTextField;
import com.jidesoft.swing.OverlayableIconsFactory;
import com.jidesoft.swing.OverlayableUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * The classic Hangman game.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.4
 */
public class HangmanDialog extends BaseDialog {
    private ExamService examService = ExamService.getInstance();
    private final DictionaryService dictionaryService = DictionaryService.getInstance();

    private Set<Character> guessedChars = Sets.newHashSet();
    private int attempts = 0;

    private static final int MAX_ATTEMPTS = 7;

    private JComboBox dictionaryComboBox;
    private DifficultyComboBox difficultyComboBox;
    private JLabel guessIconLabel;
    private JLabel feedbackField;
    private JButton startButton;
    private JButton stopButton;
    private JButton guessButton;
    private JLabel wordField;
    private JTextField translationField;
    private JTextField guessField;
    private JLabel guessFieldOverlayLabel;
    private JLabel remainingGuessesLabel;

    private HangmanDrawing hangmanDrawing = new HangmanDrawing();

    private String currentWord;

    public HangmanDialog(Frame parent, boolean modal) {
        super(parent, modal);

        getTranslator().reset();

        dictionaryComboBox = new DictionaryComboBox(examService.getSuitableDictionaries());
        difficultyComboBox = new DifficultyComboBox();
        startButton = new JButton();
        wordField = new JLabel();
        translationField = new JTextField();
        guessField = new OverlayTextField();
        guessFieldOverlayLabel = new JLabel();
        stopButton = new JButton();
        guessButton = new JButton();
        guessIconLabel = new JLabel();
        feedbackField = new JLabel();
    }

    @Override
    public JComponent createContentPanel() {
        JPanel contentPanel = new JPanel(new MigLayout("wrap 4", "[grow][grow][grow][grow]", "[grow][][][][grow][grow][grow][grow][][grow][grow][][][]"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        contentPanel.add(new JLabel(getTranslator().translate("Languages(Label)")), "span, left");
        contentPanel.add(new JLabel(IconManager.getImageIcon("dictionary.png", IconManager.IconSize.SIZE48)), "center");
        contentPanel.add(dictionaryComboBox, "growx");

        dictionaryComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

            }
        });

        contentPanel.add(new JLabel(getTranslator().translate("Difficulty(Label)")), "left");
        contentPanel.add(difficultyComboBox, "growx");

        JPanel buttonPanel = new JPanel(new MigLayout("wrap 2", "[grow][grow]", "[grow]"));

        buttonPanel.add(startButton, "growx");
        startButton.setIcon(IconManager.getMenuIcon("media_play_green.png"));
        startButton.setText(getTranslator().translate("Start(Button)"));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                startGame();
            }
        });

        buttonPanel.add(stopButton, "growx, wrap");
        stopButton.setIcon(IconManager.getMenuIcon("media_stop_red.png"));
        stopButton.setText(getTranslator().translate("Stop(Button)"));
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                stopGame();
            }
        });

        contentPanel.add(buttonPanel, "span");

        remainingGuessesLabel = new JLabel();
        remainingGuessesLabel.setText(getTranslator().translate("RemainingGuesses(Label)", MAX_ATTEMPTS - attempts));
        contentPanel.add(remainingGuessesLabel, "span");

        contentPanel.add(hangmanDrawing, "center, span, width 150::, height 150::");

        contentPanel.add(new JLabel(getTranslator().translate("WordField(Label)")), "span, center");

        contentPanel.add(wordField, "span, center");
        wordField.setFont(new Font("Tahoma", Font.BOLD, 16));

        contentPanel.add(new JLabel(getTranslator().translate("TranslationField(Label)")), "span, left");

        contentPanel.add(translationField, "span, left, growx");
        translationField.setEditable(false);

        contentPanel.add(new JLabel(getTranslator().translate("GuessField(Label)")), "span, left");

        contentPanel.add(new DefaultOverlayable(guessField, guessFieldOverlayLabel, DefaultOverlayable.SOUTH_EAST), "span 5, left, growx");
        guessField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                guessed();
            }
        });

        guessField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (guessField.getText().length() == 1 && guessedChars.contains(guessField.getText().charAt(0))) {
                    guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
                    guessButton.setEnabled(false);
                } else {
                    guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
                    guessButton.setEnabled(true);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (guessField.getText().isEmpty()) {
                    guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
                    guessButton.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        contentPanel.add(guessIconLabel, "span 3, right");
        guessIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        contentPanel.add(guessButton, "left, span, growx");
        guessButton.setIcon(IconManager.getMenuIcon("check2.png"));
        guessButton.setText(getTranslator().translate("Guess(Button)"));
        guessButton.setEnabled(false);
        guessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                guessed();
                guessField.requestFocus();
            }
        });

        // the default the field is empty
        guessFieldOverlayLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
        guessButton.setEnabled(false);

        contentPanel.add(feedbackField, "span, left");
        feedbackField.setText(getTranslator().translate("Feedback(Field)"));

        return contentPanel;
    }

    private void startGame() {
        Dictionary selectedDictionary = (Dictionary) dictionaryComboBox.getSelectedItem();
        assert selectedDictionary != null;

        Difficulty difficulty = (Difficulty) difficultyComboBox.getSelectedItem();
        assert difficulty != null;

        getLogger().info("Selected difficulty " + difficulty);
        getLogger().info("Selected dictionary is " + selectedDictionary);


        hangmanDrawing.setStage(0);
        hangmanDrawing.repaint();

        // get the current word
        currentWord = examService.getWordsForExam(selectedDictionary, difficulty, 1).get(0);

        translationField.setText(dictionaryService.getTranslation(currentWord, selectedDictionary).split("\n")[selectedDictionary.getFromLanguage() == Language.ENGLISH ? 1 : 0]);

        nextGuess();

        enableComponents(false);

        feedbackField.setText(getTranslator().translate("GameStarted(Label)"));
        guessField.requestFocus();
    }

    private void stopGame() {
        guessIconLabel.setIcon(IconManager.getImageIcon("bell2_grey.png", IconManager.IconSize.SIZE24));
        feedbackField.setText(getTranslator().translate("EndOfGame(Message)"));

        enableComponents(true);
        wordField.setText(null);
        guessField.setText(null);
        translationField.setText(null);

        attempts = 0;
        guessedChars.clear();
    }

    private void nextGuess() {

        final String maskedWord = maskWord(currentWord);
        wordField.setText(maskedWord);

        if (!maskedWord.contains("_")) {
            JOptionPane.showMessageDialog(this, getTranslator().translate("Success(Message)"));
            stopGame();
        }
    }

    private void guessed() {
        final String currentGuess = guessField.getText();

        if (currentWord.equals(currentGuess)) {
            JOptionPane.showMessageDialog(this, getTranslator().translate("Success(Message)"));
            stopGame();
        } else {
            // we take only the first character
            guessedChars.add(currentGuess.charAt(0));

            if (currentWord.indexOf(currentGuess) != -1) {
                feedbackField.setText(getTranslator().translate("CorrectGuess(Label)"));
                guessIconLabel.setIcon(IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE24));
            } else {
                feedbackField.setText(getTranslator().translate("WrongGuess(Label)"));
                guessIconLabel.setIcon(IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE24));

                // the attempts count is increased only for wrong attempts
                if (++attempts >= MAX_ATTEMPTS) {
                    JOptionPane.showMessageDialog(this, getTranslator().translate("Failure(Message)"));
                    stopGame();
                }
            }

            nextGuess();

            hangmanDrawing.setStage(attempts);
            hangmanDrawing.repaint();
            remainingGuessesLabel.setText(getTranslator().translate("RemainingGuesses(Label)", MAX_ATTEMPTS - attempts));

            guessField.setText(null);
            guessField.requestFocusInWindow();
        }
    }

    private void enableComponents(Boolean enable) {
        dictionaryComboBox.setEnabled(enable);
        startButton.setEnabled(enable);
        stopButton.setEnabled(!enable);
        guessButton.setEnabled(!enable);
        guessField.setEnabled(!enable);
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();

        JButton quitButton = new JButton();

        quitButton.setName(getTranslator().translate("Quit(Button)"));
        buttonPanel.add(quitButton, ButtonPanel.CANCEL_BUTTON);

        quitButton.setAction(new AbstractAction(getTranslator().translate("Quit(Button)")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setVisible(false);
            }
        });
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return buttonPanel;
    }

    private String maskWord(String word) {
        StringBuilder result = new StringBuilder(word.length() * 3);

        for (int i = 0; i < word.length(); i++) {
            if (guessedChars.contains(word.charAt(i))) {
                result.append(word.charAt(i));
            } else {
                result.append("_");
            }

            result.append(" ");
        }

        return result.toString();
    }
}
