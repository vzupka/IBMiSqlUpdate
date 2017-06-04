package update;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * Sets parameters for printing Clob contents
 * 
 * @author Vladimír Župka 2016
 */
public class U_ClobPrintSetting extends JDialog {

    final String MEDIA_SIZE = "MEDIA_SIZE";
    final String PRINT_FONT_SIZE = "PRINT_FONT_SIZE";
    final String ORIENTATION = "ORIENTATION";
    final String LEFT_MARGIN = "LEFT_MARGIN";
    final String RIGHT_MARGIN = "RIGHT_MARGIN";
    final String TOP_MARGIN = "TOP_MARGIN";
    final String BOTTOM_MARGIN = "BOTTOM_MARGIN";

    Properties properties;
    String language;
    final String PROP_COMMENT = "Table Update for IBM i, © Vladimír Župka 2016";
    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    String encoding = System.getProperty("file.encoding");
    BufferedReader infile;
    BufferedWriter outfile;

    GridBagLayout gridBagLayout = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    JTextField tfPaperSize = new JTextField();
    JTextField tfFontSize = new JTextField();
    JTextField tfOrientation = new JTextField();
    JTextField tfLeftMargin = new JTextField();
    JTextField tfRightMargin = new JTextField();
    JTextField tfTopMargin = new JTextField();
    JTextField tfBottomMargin = new JTextField();

    Locale locale;
    ResourceBundle titles;
    String pageSetTitle, paperSiz, fontSiz, orient, leftMar, rightMar, topMar, bottomMar;
    ResourceBundle buttons;
    String _return, saveReturn, save;
    ResourceBundle messages;
    String invalPaperSize, invalOrient, invalFontSiz, invalLeftMar, invalRightMar,
            invalTopMar, invalBottomMar;

    Container dataContentPane;

    JPanel dataGlobalPanel;
    JPanel dataMsgPanel = new JPanel();
    JScrollPane scrollPane = new JScrollPane();
    JLabel message = new JLabel();

    
    final Color DIM_BLUE = new Color(50, 60, 160);
    final Color DIM_RED = new Color(190, 60, 50);

    /**
     * Constructor
     */
    public U_ClobPrintSetting() {
        super();
        super.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    }

    /**
     * Building data window
     */
    protected void buildDataWindow() {

        properties = new Properties();
        try {
            infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);

            tfPaperSize.setText(properties.getProperty(MEDIA_SIZE));
            tfFontSize.setText(properties.getProperty(PRINT_FONT_SIZE));
            tfOrientation.setText(properties.getProperty(ORIENTATION));
            tfLeftMargin.setText(properties.getProperty(LEFT_MARGIN));
            tfRightMargin.setText(properties.getProperty(RIGHT_MARGIN));
            tfTopMargin.setText(properties.getProperty(TOP_MARGIN));
            tfBottomMargin.setText(properties.getProperty(BOTTOM_MARGIN));

            infile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        language = properties.getProperty("LANGUAGE");
        Locale currentLocale = Locale.forLanguageTag(language);

        // Get resource bundle classes
        titles = ResourceBundle.getBundle("locales.L_TitleLabelBundle", currentLocale);
        buttons = ResourceBundle.getBundle("locales.L_ButtonBundle", currentLocale);
        messages = ResourceBundle.getBundle("locales.L_MessageBundle", currentLocale);

        // Localized titles
        pageSetTitle = titles.getString("PageSetTitle");
        paperSiz = titles.getString("PaperSiz");
        fontSiz = titles.getString("FontSiz");
        orient = titles.getString("Orient");
        leftMar = titles.getString("LeftMar");
        rightMar = titles.getString("RightMar");
        topMar = titles.getString("TopMar");
        bottomMar = titles.getString("BottomMar");

        JLabel lblPaperSize = new JLabel(paperSiz);
        JLabel lblFontSize = new JLabel(fontSiz);
        JLabel lblOrientation = new JLabel(orient);
        JLabel lblLeftMargin = new JLabel(leftMar);
        JLabel lblRightMargin = new JLabel(rightMar);
        JLabel lblTopMargin = new JLabel(topMar);
        JLabel lblBottomMargin = new JLabel(bottomMar);

        // Localized button labels
        _return = buttons.getString("Return");
        saveReturn = buttons.getString("SaveReturn");
        save = buttons.getString("Save");

        // Localized messages
        invalPaperSize = messages.getString("InvalPaperSize");
        invalOrient = messages.getString("InvalOrient");
        invalFontSiz = messages.getString("InvalFontSiz");
        invalLeftMar = messages.getString("InvalLeftMar");
        invalRightMar = messages.getString("InvalRightMar");
        invalTopMar = messages.getString("InvalTopMar");
        invalBottomMar = messages.getString("InvalBottomMar");

        // Start building the window
        // -------------------------
        JLabel dataPanelTitle = new JLabel(pageSetTitle);
        dataPanelTitle.setFont(new Font("Helvetica", Font.PLAIN, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        titlePanel.add(dataPanelTitle);
        titlePanel.setMinimumSize(new Dimension(dataPanelTitle.getPreferredSize().width, 40));
        titlePanel.setPreferredSize(new Dimension(dataPanelTitle.getPreferredSize().width, 40));
        titlePanel.setMaximumSize(new Dimension(dataPanelTitle.getPreferredSize().width, 40));

        // Place data fields in grid bag for all columns
        // to input data panel
        // ---------------------------------------------
        JPanel inputDataPanel = new JPanel();
        // Grid bag layout used to lay out components
        inputDataPanel.setLayout(gridBagLayout);

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;
        gbc.gridx = 0;

        gbc.anchor = GridBagConstraints.WEST;

        inputDataPanel.add(lblPaperSize, gbc);
        gbc.gridy++;
        inputDataPanel.add(lblFontSize, gbc);
        gbc.gridy++;
        inputDataPanel.add(lblOrientation, gbc);
        gbc.gridy++;
        inputDataPanel.add(lblLeftMargin, gbc);
        gbc.gridy++;
        inputDataPanel.add(lblRightMargin, gbc);
        gbc.gridy++;
        inputDataPanel.add(lblTopMargin, gbc);
        gbc.gridy++;
        inputDataPanel.add(lblBottomMargin, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;

        gbc.anchor = GridBagConstraints.WEST;
        inputDataPanel.add(tfPaperSize, gbc);
        gbc.gridy++;
        inputDataPanel.add(tfFontSize, gbc);
        gbc.gridy++;
        inputDataPanel.add(tfOrientation, gbc);
        gbc.gridy++;
        inputDataPanel.add(tfLeftMargin, gbc);
        gbc.gridy++;
        inputDataPanel.add(tfRightMargin, gbc);
        gbc.gridy++;
        inputDataPanel.add(tfTopMargin, gbc);
        gbc.gridy++;
        inputDataPanel.add(tfBottomMargin, gbc);

        // Build button row panel
        JButton saveButton = new JButton(save);
        saveButton.setMinimumSize(new Dimension(100, 35));
        saveButton.setMaximumSize(new Dimension(100, 35));
        saveButton.setPreferredSize(new Dimension(100, 35));

        JButton saveAndReturnButton = new JButton(saveReturn);
        saveAndReturnButton.setMinimumSize(new Dimension(160, 35));
        saveAndReturnButton.setMaximumSize(new Dimension(160, 35));
        saveAndReturnButton.setPreferredSize(new Dimension(160, 35));

        JButton dataPanelReturnButton = new JButton(_return);
        dataPanelReturnButton.setMinimumSize(new Dimension(80, 35));
        dataPanelReturnButton.setMaximumSize(new Dimension(80, 35));
        dataPanelReturnButton.setPreferredSize(new Dimension(80, 35));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.LINE_AXIS));
        buttonRow.setAlignmentX(Box.LEFT_ALIGNMENT);
        buttonRow.add(saveButton);
        buttonRow.add(Box.createRigidArea(new Dimension(10, 60)));
        buttonRow.add(saveAndReturnButton);
        buttonRow.add(Box.createRigidArea(new Dimension(10, 60)));
        buttonRow.add(dataPanelReturnButton);

        // Message panels
        BoxLayout msgLayoutX = new BoxLayout(dataMsgPanel, BoxLayout.LINE_AXIS);
        dataMsgPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        dataMsgPanel.setLayout(msgLayoutX);

        message.setText(" ");
        dataMsgPanel.removeAll();
        dataMsgPanel.add(message);

        // Lay out components in groups
        dataGlobalPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dataGlobalPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
                layout.createParallelGroup(Alignment.LEADING).addComponent(titlePanel)
                .addComponent(inputDataPanel).addComponent(buttonRow).addComponent(dataMsgPanel)));
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
                layout.createSequentialGroup().addComponent(titlePanel)
                .addComponent(inputDataPanel).addComponent(buttonRow).addComponent(dataMsgPanel)));

        dataGlobalPanel.setLayout(layout);

        // Put data global panel to the scroll pane
        scrollPane = new JScrollPane(dataGlobalPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollPane.setBackground(dataGlobalPanel.getBackground());

        // Set Save button activity
        // --------------------------
        saveButton.addActionListener(a -> {
            if (!saveData()) {
                pack();
                repaint();
                setVisible(true);
            }
        });

        // Set Save and Return button activity
        // -----------------------------------
        saveAndReturnButton.addActionListener(a -> {
            if (!saveData()) {
                pack();
                repaint();
                setVisible(true);
            } else {
                dispose();
            }
        });

        // Set Return button activity
        // --------------------------
        dataPanelReturnButton.addActionListener(a -> {
            dispose();
        });

        // Enable ENTER key to save and return action
        // ------------------------------------------
        dataGlobalPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ENTER"), "saveData");
        dataGlobalPanel.getActionMap().put("saveData", new SaveAction());

        // Display data panel
        dataContentPane = getContentPane();
        dataContentPane.add(scrollPane);
        pack();
        setVisible(true);
    }

    /**
     * Save data of the inserted or updated row
     *
     * @return boolean true (if no error)
     */
    protected boolean saveData() {
        boolean OK = true;
        do {
            message.setText("");
            message.setForeground(DIM_RED);
            if (tfPaperSize.getText().toUpperCase().equals("A4")
                    || tfPaperSize.getText().toUpperCase().equals("A3")
                    || tfPaperSize.getText().toUpperCase().equals("LETTER")) {
                tfPaperSize.setForeground(Color.BLACK);
                properties.setProperty("MEDIA_SIZE", tfPaperSize.getText().toUpperCase());
            } else {
                tfPaperSize.setForeground(DIM_RED);
                message.setText(invalPaperSize);
                OK = false;
                break;
            }
            if (tfOrientation.getText().toUpperCase().equals("PORTRAIT")
                    || tfOrientation.getText().toUpperCase().equals("P")
                    || tfOrientation.getText().toUpperCase().equals("LANDSCAPE")
                    || tfOrientation.getText().toUpperCase().equals("L")) {
                tfOrientation.setForeground(Color.BLACK);
                properties.setProperty("ORIENTATION", tfOrientation.getText().toUpperCase());
            } else {
                tfOrientation.setForeground(DIM_RED);
                message.setText(invalOrient);
                OK = false;
                break;
            }
            try {
                Integer.parseUnsignedInt(tfFontSize.getText());
                tfFontSize.setForeground(Color.BLACK);
                properties.setProperty("PRINT_FONT_SIZE", tfFontSize.getText());
            } catch (NumberFormatException nfe) {
                tfFontSize.setForeground(DIM_RED);
                message.setText(invalFontSiz);
                OK = false;
                break;
            }
            try {
                Integer.parseUnsignedInt(tfLeftMargin.getText());
                tfLeftMargin.setForeground(Color.BLACK);
                properties.setProperty("LEFT_MARGIN", tfLeftMargin.getText());
            } catch (NumberFormatException nfe) {
                tfLeftMargin.setForeground(DIM_RED);
                message.setText(invalLeftMar);
                OK = false;
                break;
            }
            try {
                Integer.parseUnsignedInt(tfRightMargin.getText());
                tfRightMargin.setForeground(Color.BLACK);
                properties.setProperty("RIGHT_MARGIN", tfRightMargin.getText());
            } catch (NumberFormatException nfe) {
                tfRightMargin.setForeground(DIM_RED);
                message.setText(invalRightMar);
                OK = false;
                break;
            }
            try {
                Integer.parseUnsignedInt(tfTopMargin.getText());
                tfTopMargin.setForeground(Color.BLACK);
                properties.setProperty("TOP_MARGIN", tfTopMargin.getText());
            } catch (NumberFormatException nfe) {
                tfTopMargin.setForeground(DIM_RED);
                message.setText(invalTopMar);
                OK = false;
                break;
            }
            try {
                Integer.parseUnsignedInt(tfBottomMargin.getText());
                tfBottomMargin.setForeground(Color.BLACK);
                properties.setProperty("BOTTOM_MARGIN", tfBottomMargin.getText());
            } catch (NumberFormatException nfe) {
                tfBottomMargin.setForeground(DIM_RED);
                message.setText(invalBottomMar);
                OK = false;
                break;
            }
        } while (!OK);

        try {
            // Create a new text file in directory "paramfiles"
            outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
            properties.store(outfile, PROP_COMMENT);
            outfile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        dataMsgPanel.add(message);
        pack();
        setVisible(true);
        return OK;
    }

    /**
     * Inner class for saving data and returning to the list
     */
    class SaveAction extends AbstractAction {

        protected static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // Return to the list if data is OK, else do nothing
            // - error message is displayed here, on data panel
            boolean isOK = saveData();
            if (isOK) {
                setVisible(true);
            }
        }
    }

}
