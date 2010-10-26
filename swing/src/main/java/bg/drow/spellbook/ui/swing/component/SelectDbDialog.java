package bg.drow.spellbook.ui.swing.component;

import bg.drow.spellbook.ui.swing.util.IconManager;
import com.jidesoft.dialog.BannerPanel;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.icons.JideIconsFactory;
import com.jidesoft.swing.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.URL;

public class SelectDbDialog extends BaseDialog implements PropertyChangeListener {
    private JButton downloadButton;
    private JButton changeFolderButton;
    private JTextField downloadUrlTextField;
    private JTextField localDbFolderTextField;
    private ProgressMonitor progressMonitor;
    private Task task;
    private String localDbFolder;
    private JButton okButton = createOkButton();

    private static final String DB_URL = "http://spellbook-dictionary.googlecode.com/files/spellbook-db-0.4.zip";
    private static final String DOWNLOAD_DIR = System.getProperty("java.io.tmpdir");
    private static final int FONT_SIZE = 14;
    private JLabel localDbFolderValidationLabel;
    private boolean downloaded = false;

    public SelectDbDialog() {
        super((Frame) null, true);

        localDbFolderValidationLabel = new JLabel();
        localDbFolderTextField = new OverlayTextField();
        localDbFolderTextField.setEditable(false);
        downloadUrlTextField = new JTextField(DB_URL);
        downloadUrlTextField.setEditable(false);
        downloadButton = new JButton(getTranslator().translate("Download(Button)"), IconManager.getImageIcon("data_down.png", IconManager.IconSize.SIZE24));
        changeFolderButton = new JButton(getTranslator().translate("ChangeFolder(Button)"), IconManager.getImageIcon("data_find.png", IconManager.IconSize.SIZE24));
        progressMonitor = new ProgressMonitor(this, "Downloading url " + DB_URL, "Downloading", 0, 100);

        localDbFolder = System.getProperty("user.home");
        localDbFolderTextField.setText(localDbFolder);

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // todo - change this flag - it caused a serious problem in the past
                downloaded = true;

                //careful not to overwrite existing files
                File file = new File(getDbPath());
                if (file.exists() &&
                        JOptionPane.showConfirmDialog(SelectDbDialog.this,
                                getTranslator().translate("Overwrite(Message)")) != JOptionPane.YES_OPTION) {
                    getLogger().info("don't overwrite existing file");
                } else {
                    task = new Task();
                    task.addPropertyChangeListener(SelectDbDialog.this);
                    task.execute();
                    downloadButton.setEnabled(false);
                }
            }
        });

        changeFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FolderChooser folderChooser = new FolderChooser();

                int result = folderChooser.showOpenDialog(SelectDbDialog.this);

                if (result == FolderChooser.APPROVE_OPTION) {
                    localDbFolder = folderChooser.getSelectedFolder().getAbsolutePath();
                    localDbFolderTextField.setText(localDbFolder);

                    validateLocalDbPath();
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void validateLocalDbPath() {
        File dbFile = new File(localDbFolder + File.separator + getFileName());

        if (dbFile.exists()) {
            okButton.getAction().setEnabled(true);
            localDbFolderValidationLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.CORRECT));
        } else {
            okButton.getAction().setEnabled(false);
            localDbFolderValidationLabel.setIcon(OverlayableUtils.getPredefinedOverlayIcon(OverlayableIconsFactory.ERROR));
            localDbFolderValidationLabel.setToolTipText(getTranslator().translate("InvalidFolder(Label)", getFileName()));
        }
    }

    @Override
    public JComponent createBannerPanel() {
        BannerPanel bannerPanel = new BannerPanel(getTranslator().translate("MissingDb(Title)"), getTranslator().translate("MissingDb(Message)", getFileName()),
                JideIconsFactory.getImageIcon("/icons/48x48/data_unknown.png"));
        bannerPanel.setFont(new Font("Tahoma", Font.PLAIN, FONT_SIZE));
        bannerPanel.setBackground(Color.WHITE);
        bannerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        return bannerPanel;
    }

    @Override
    public JComponent createContentPanel() {
        MigLayout layout = new MigLayout(
                "wrap 4",                 // Layout Constraints
                "[][][grow][]",   // Column constraints
                "[shrink 0][shrink 0]");    // Row constraints


        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        panel.add(new JLabel(getTranslator().translate("DownloadUrl(Label)")));
        panel.add(downloadUrlTextField, "span 2, growx");
        panel.add(downloadButton, "growx");
        panel.add(new JLabel(getTranslator().translate("DbFolder(Label)")));
        panel.add(new DefaultOverlayable(localDbFolderTextField, localDbFolderValidationLabel, DefaultOverlayable.SOUTH_EAST), "span 2, growx");
        panel.add(changeFolderButton, "growx");

        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        ButtonPanel buttonPanel = new ButtonPanel();
        JButton cancelButton = createCancelButton();
        JButton helpButton = createHelpButton();

        buttonPanel.addButton(okButton, ButtonPanel.AFFIRMATIVE_BUTTON);
        buttonPanel.addButton(cancelButton, ButtonPanel.CANCEL_BUTTON);
        buttonPanel.addButton(helpButton, ButtonPanel.HELP_BUTTON);

        setDefaultCancelAction(cancelButton.getAction());
        setDefaultAction(okButton.getAction());
        getRootPane().setDefaultButton(okButton);

        validateLocalDbPath();

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return buttonPanel;
    }

    @Override
    public int showDialog() {
        pack();

        setVisible(true);

        return getDialogResult();
    }

    class Task extends SwingWorker<Void, Void> {
        private static final int BUFFER_SIZE = 1024;

        @Override
        public Void doInBackground() {
            try {
                URL dbUrl = new URL(DB_URL);
                setProgress(0);

                int contentLength = dbUrl.openConnection().getContentLength();

                BufferedInputStream in = new BufferedInputStream(dbUrl.openStream());
                FileOutputStream fos = new FileOutputStream(getDbPath());
                BufferedOutputStream bout = new BufferedOutputStream(fos, BUFFER_SIZE);
                byte[] data = new byte[BUFFER_SIZE];
                int x;
                int total = 0;

                getLogger().info("Downloading file " + DB_URL);

                while ((x = in.read(data, 0, BUFFER_SIZE)) >= 0) {
                    total += x;
                    final int percents = (int) (((double) total / contentLength) * 100);
                    setProgress(percents);
                    bout.write(data, 0, x);
                }

                bout.close();
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            downloadButton.setEnabled(true);
            okButton.getAction().setEnabled(true);
        }

    }

    public String getDbPath() {
        return downloaded ? DOWNLOAD_DIR + File.separator + getFileName() : localDbFolder + File.separator + getFileName();
    }

    private String getFileName() {
        return DB_URL.substring(DB_URL.lastIndexOf("/") + 1);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message = String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            if (progressMonitor.isCanceled() || task.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    task.cancel(true);
                    File file = new File(getDbPath());

                    if (file.exists()) {
                        // removing partially downloaded file
                        if (file.delete()) {
                            getLogger().info("partial download successfully deleted");
                        } else {
                            getLogger().info("failed to delete partially downloaded file " + file.getAbsolutePath());
                        }
                    }

                    getLogger().info("Task canceled.\n");
                } else {
                    okButton.getAction().setEnabled(true);

                    getLogger().info("Task completed.\n");
                }

                downloadButton.setEnabled(true);
            }
        }

    }
}
