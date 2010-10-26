package bg.drow.spellbook.ui.desktop.exam;

import bg.drow.spellbook.core.model.ExamGrade;
import bg.drow.spellbook.core.model.ExamScoreEntry;
import bg.drow.spellbook.core.service.DictionaryService;
import bg.drow.spellbook.core.service.exam.ExamService;
import bg.drow.spellbook.ui.swing.component.BaseDialog;
import bg.drow.spellbook.ui.swing.model.ListBackedListModel;
import bg.drow.spellbook.ui.swing.util.IconManager;
import bg.drow.spellbook.ui.swing.util.SwingUtil;
import bg.drow.spellbook.ui.swing.validation.ButtonControllingDocumentListener;
import bg.drow.spellbook.util.DateUtils;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialogPane;
import com.jidesoft.swing.JideBoxLayout;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A dialog showing exam summary after an exam completion.
 *
 * @author <a href="mailto:bozhidar@drow.bg">Bozhidar Batsov</a>
 * @since 0.3
 */
public class ExamSummaryDialog extends BaseDialog {

    private ExamService examService = ExamService.getInstance();
    private DictionaryService dictionaryService = DictionaryService.getInstance();

    private JLabel incorrectWords;
    private JLabel correctWords;
    private JLabel score;
    private JLabel grade;
    private JLabel totalWords;
    private JLabel totalTime;
    private JLabel averageTime;
    private JTextField nameTextField;
    private JButton submitScoreButton;
    private JPanel incorrectWordPanel;
    private JPanel scoreboardPanel;
    private ExamStats examStats;
    private Dialog owner;
    private static final int MIN_PASSING_SCORE = 60;
    private ScoreboardTableModel scoreboardTableModel;
    private boolean submitted = false;

    public ExamSummaryDialog(final Dialog owner, final ExamStats examStats) {
        super(owner, true);

        this.examStats = examStats;
        this.owner = owner;

        incorrectWords = new JLabel();
        correctWords = new JLabel();
        totalTime = new JLabel();
        averageTime = new JLabel();
        grade = new JLabel();
        totalWords = new JLabel();
        score = new JLabel();
        nameTextField = new JTextField();
        submitScoreButton = new JButton(getTranslator().translate("SubmitScore(Button)"));
        nameTextField.getDocument().addDocumentListener(new ButtonControllingDocumentListener(nameTextField, submitScoreButton));

        submitScoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!submitted) {
                    final ExamScoreEntry tEntry = examStats.createExamScoreEntry(nameTextField.getText());
                    examService.addScoreboardResult(tEntry);
                    scoreboardTableModel.getScoreEntries().add(tEntry);
                    scoreboardTableModel.fireTableRowsInserted(0, scoreboardTableModel.getRowCount());
                    submitted = true;
                } else {
                    JOptionPane.showMessageDialog(ExamSummaryDialog.this, getTranslator().translate("ScoreAlreadySubmitted(Message)"));
                }
            }
        });

        incorrectWordPanel = createIncorrectWordsPanel();
    }

    @Override
    public JComponent createBannerPanel() {

        ImageIcon tImageIcon = examStats.getScore() >= MIN_PASSING_SCORE ? IconManager.getImageIcon("bell2_green.png", IconManager.IconSize.SIZE48) :
                IconManager.getImageIcon("bell2_red.png", IconManager.IconSize.SIZE48);

        BannerPanel bannerPanel = new BannerPanel(examStats.getScore() >= MIN_PASSING_SCORE ? getTranslator().translate("Success(Title)") : getTranslator().translate("Failure(Title)"),
                examStats.getScore() >= MIN_PASSING_SCORE ? getTranslator().translate("Passed(Message)") : getTranslator().translate("Failed(Message)"),
                tImageIcon);
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, 12));
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return bannerPanel;
    }

    @Override
    protected StandardDialogPane createStandardDialogPane() {
        return new DefaultStandardDialogPane() {
            @Override
            protected void layoutComponents(Component bannerPanel, Component contentPanel, ButtonPanel buttonPanel) {
                setLayout(new JideBoxLayout(this, BoxLayout.Y_AXIS));
                if (bannerPanel != null) {
                    add(bannerPanel);
                }
                if (contentPanel != null) {
                    add(contentPanel);
                }
                add(buttonPanel, JideBoxLayout.FIX);
                incorrectWordPanel = createIncorrectWordsPanel();
                add(incorrectWordPanel, JideBoxLayout.FIX);
                incorrectWordPanel.setVisible(false);
                scoreboardPanel = createScoreboardPanel();
                add(scoreboardPanel, JideBoxLayout.FIX);
                scoreboardPanel.setVisible(false);
            }
        };
    }

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[][grow]"));

        panel.add(new JLabel(getTranslator().translate("Score(Label)")));
        panel.add(score);
        panel.add(new JLabel(getTranslator().translate("Grade(Label)")));
        panel.add(grade);
        panel.add(new JLabel(getTranslator().translate("Total(Label)")));
        panel.add(totalWords);
        panel.add(new JLabel(getTranslator().translate("NumberOfCorrect(Label)")));
        panel.add(correctWords);
        panel.add(new JLabel(getTranslator().translate("NumberOfIncorrect(Label)")));
        panel.add(incorrectWords);
        panel.add(new JLabel(getTranslator().translate("TotalTime(Label)")));
        panel.add(totalTime);
        panel.add(new JLabel(getTranslator().translate("AvgTime(Label)")));
        panel.add(averageTime);
        panel.add(new JLabel(getTranslator().translate("EnterName(Label)")));
        panel.add(nameTextField, "w 150, growx, split 2");
        panel.add(submitScoreButton);

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton closeButton = createCloseButton();
        JButton incorrectWordsButton = new JButton();
        JButton scoreboardButton = new JButton();
        incorrectWordsButton.setMnemonic('D');

        buttonPanel.addButton(closeButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(incorrectWordsButton, ButtonPanel.OTHER_BUTTON);
        buttonPanel.addButton(scoreboardButton, ButtonPanel.OTHER_BUTTON);

        incorrectWordsButton.setAction(new AbstractAction("View incorrect >>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (incorrectWordPanel.isVisible()) {
                    incorrectWordPanel.setVisible(false);
                    putValue(Action.NAME, "View incorrect <<");
                    pack();
                } else {
                    incorrectWordPanel.setVisible(true);
                    putValue(Action.NAME, "<< View incorrect");
                    pack();
                }
            }
        });

        scoreboardButton.setAction(new AbstractAction("View scoreboard >>") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scoreboardPanel.isVisible()) {
                    scoreboardPanel.setVisible(false);
                    putValue(Action.NAME, "View scoreboard <<");
                    pack();
                } else {
                    scoreboardPanel.setVisible(true);
                    putValue(Action.NAME, "<< View scoreboard");
                    pack();
                }
            }
        });

        setDefaultCancelAction(closeButton.getAction());
        setDefaultAction(closeButton.getAction());
        getRootPane().setDefaultButton(closeButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN); // since the checkbox is quite wide, we don't want all of them have the same size.
        submitScoreButton.setEnabled(false);
        return buttonPanel;
    }

    public JPanel createIncorrectWordsPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[100:150:, grow 40][200:300:, grow 60]", "[grow]"));
        panel.setName("IW");

        final JList incorrectWords = new JList(new ListBackedListModel(examStats.getIncorrectWords()));
        panel.add(new JScrollPane(incorrectWords), "grow");

        final JTextPane translationPane = new JTextPane();
        translationPane.setContentType("text/html");
        panel.add(new JScrollPane(translationPane), "grow");

        incorrectWords.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                final String selectedWord = (String) incorrectWords.getSelectedValue();
                translationPane.setText(SwingUtil.formatTranslation(selectedWord,
                        dictionaryService.getTranslation(selectedWord, examStats.getDictionary())));
            }
        });

        return panel;
    }

    public JPanel createScoreboardPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap", "[grow]", "[200]"));
        panel.setName("SB");

        List<ExamScoreEntry> examScoreEntryList = examService.getExamScores();

        scoreboardTableModel = new ScoreboardTableModel(examScoreEntryList);
        panel.add(new JScrollPane(new JTable(scoreboardTableModel)), "grow");

        return panel;
    }

    public void showExamResult() {
        int correctWords = examStats.getCorrectWords().size();
        int totalWords = examStats.getTotalWords();

        int score = examStats.getScore();

        this.score.setText(Integer.toString(score) + "%");
        this.grade.setText(ExamGrade.getGrade(score).toString());
        this.correctWords.setText(Integer.toString(correctWords));
        this.incorrectWords.setText(Integer.toString(examStats.getIncorrectWords().size()));
        this.totalWords.setText(Integer.toString(totalWords));
        this.totalTime.setText(DateUtils.dateDifference(examStats.getStartTime(), examStats.getEndTime()));
        this.averageTime.setText(DateUtils.getAvgDuration(examStats.getStartTime(), examStats.getEndTime(), totalWords));

        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }
}

class ScoreboardTableModel extends AbstractTableModel {
    private String[] columnNames = {"Name", "From", "To", "Score"};
    private List<ExamScoreEntry> scoreEntries;

    ScoreboardTableModel(final List<ExamScoreEntry> pScoreEntries) {
        scoreEntries = pScoreEntries;
    }

    public List<ExamScoreEntry> getScoreEntries() {
        return scoreEntries;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return scoreEntries.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        return scoreEntries.get(row).toArray()[col];
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
