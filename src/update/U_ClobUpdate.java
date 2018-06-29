package update;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Chromaticity;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.GroupLayout.Alignment;

/**
 * Update CLOB contents - display, update or print
 *
 * @author Vladimír Župka 2016
 *
 */
public class U_ClobUpdate extends JDialog {

    static final long serialVersionUID = 1L;

    U_DataTable dataTable;
    String colName;

    Locale locale;
    ResourceBundle titles;
    String updateColumn, startOfText, lengthOfText, findText;
    ResourceBundle buttons;
    String _return, refresh, save, putFile, getFile, pagset, print;
    ResourceBundle messages;
    String colNotText, invalCharSet, colLenZero, clobCapacity, notFound,
            colUndef, contentLoaded, colLengthIs, colUnchg;

    Properties properties;
    BufferedReader infile;
    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    String encoding = System.getProperty("file.encoding");
    String language;
    int windowHeight;
    int windowWidth;
    int screenWidth;
    int screenHeight;
    String autoWindowSize;
    int fontSize;
    int printFontSize;
    int fetchFirst;
    String charCode;
    Font font;

    // Measuring units millimeters for MediaPrintableArea
    private static final int MM = 1000;

    PageFormat pageFormat;

    FilePageRenderer pageRenderer;

    JTextArea workTextArea = new JTextArea();
    U_ClobReturnedValues retValues;

    int nbrHdrLines;
    ArrayList<String[]> headerArrayList;

    JLabel labelStart;
    JLabel labelLength;
    JLabel labelFind;

    JTextField textStart;
    JTextField textLength;
    JTextField textFind;

    long startPosition;
    long inputLength;
    long positionFound;
    long initLength;
    long clobLength;
    Clob clob;

    JPanel inputPanel;

    JButton returnButton;
    JButton refreshButton;
    JButton saveButton;
    JButton getButton;
    JButton putButton;
    JButton previewButton;
    JButton printButton;

    JPanel buttonPanel;

    JPanel msgPanel;

    // Text in inputTextArea will contain the file contents as a string
    JScrollPane scrollPane = new JScrollPane();
    JPanel globalPanel = new JPanel();
    Container container;
    GroupLayout layout = new GroupLayout(globalPanel);

    JLabel title;

    // Lines from the print file
    ArrayList<String> lineList = new ArrayList<>();

    // Printer job
    PrinterJob printerJob;
    PrintRequestAttributeSet attr_set;

    // Font size in number of print points 
    // 1 point is 1/72 of an inch
    String fontSizeScript; // Font size as String from script

    // Media size - A4 (default) or A3
    MediaSize mediaSize;
    String paperSize;

    // Page orientation 
    // PORTRAIT (default) or LANDSCAPE
    String orientation;

    // Page margins
    int leftMargin; // LMn
    int rightMargin; // RMn
    int topMargin; // TMn
    int bottomMargin; // BMn

    // Difference between heights of A4 and LETTER
    float LETTER_LENGTH_CORRECTION;
    float LETTER_WIDTH_CORRECTION;

    // Number of print points in 1 millimeter
    float pointsInMM = (float) (72 / 25.4);
    float mmInPoint = (float) (25.4 / 72);
    float mmWidth;
    float mmHeight;
    float imWidth;
    float imHeight;
    float pagePrintableWidth;
    float pagePrintableHeight;
    float pageLength; // in number of lines
    float pageWidth; // in number of characters
    float pageLengthInPoints; // in number of print points
    float pageWidthInPoints; // in number of print points
    float nbrFooterLines;
    float os_correction;

    ArrayList<String> lineVector;
    ArrayList<ArrayList<String>> pageVector;
    int pageNumber;

    final Color DIM_BLUE = new Color(50, 60, 160);
    final Color DIM_RED = new Color(190, 60, 50);
    Color VERY_LIGHT_BLUE = Color.getHSBColor(0.60f, 0.05f, 0.98f);

    /**
     * Constructor
     *
     * @param dataTable
     */
    @SuppressWarnings({"ConvertToStringSwitch", "OverridableMethodCallInConstructor"})
    public U_ClobUpdate(U_DataTable dataTable) {
        super();
        super.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        this.dataTable = dataTable;

        // Get application properties
        properties = new Properties();
        try {
           BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
           properties.load(infile);
           infile.close();
        } catch (IOException ioe) {
           ioe.printStackTrace();
        }
        windowHeight = new Integer(properties.getProperty("RESULT_WINDOW_HEIGHT"));
        windowWidth = new Integer(properties.getProperty("RESULT_WINDOW_WIDTH"));
        autoWindowSize = properties.getProperty("AUTO_WINDOW_SIZE");
        fontSize = new Integer(properties.getProperty("FONT_SIZE"));
        fetchFirst = new Integer(properties.getProperty("FETCH_FIRST"));
        charCode = properties.getProperty("CHARSET");

        retValues = new U_ClobReturnedValues();

        // Set parameters for page format
        // ------------------------------      
        // Set print request attributes
        attr_set = new HashPrintRequestAttributeSet();

        setPrintParams();
    }

    JLabel msg = new JLabel();

    /**
     * Create and show window
     *
     * @param colName
     * @param clob
     * @param initStartPos
     * @param inputLength
     */
    @SuppressWarnings("UseSpecificCatch")
    public void createWindow(String colName, Clob clob, long initStartPos, long inputLength) {
        this.colName = colName;
        this.startPosition = initStartPos;
        this.inputLength = inputLength;
        this.clob = clob;

        if (startPosition <= 0) {
            startPosition = 1;
        }
        if (inputLength <= 0) {
            inputLength = fetchFirst;
        }

        try {
            clobLength = (int) this.clob.length();
            workTextArea.setText(this.clob.getSubString(startPosition, (int) inputLength));

        } catch (Exception e) {
            e.printStackTrace();
        }

        language = properties.getProperty("LANGUAGE");
        Locale currentLocale = Locale.forLanguageTag(language);

        // Get resource bundle classes
        titles = ResourceBundle.getBundle("locales.L_TitleLabelBundle", currentLocale);
        buttons = ResourceBundle.getBundle("locales.L_ButtonBundle", currentLocale);
        messages = ResourceBundle.getBundle("locales.L_MessageBundle", currentLocale);

        updateColumn = titles.getString("UpdateColumn");
        startOfText = titles.getString("StartOfText");
        lengthOfText = titles.getString("LengthOfText");
        findText = titles.getString("Find");

        // Localized button labels
        _return = buttons.getString("Return");
        refresh = buttons.getString("Refresh");
        save = buttons.getString("Save");
        getFile = buttons.getString("GetFile");
        putFile = buttons.getString("PutFile");
        pagset = buttons.getString("Pagset");
        print = buttons.getString("Print");

        // Localized messages
        clobCapacity = messages.getString("ClobCapacity");
        notFound = messages.getString("NotFound");
        colUndef = messages.getString("ColUndef");
        contentLoaded = messages.getString("ContentLoaded");
        colLengthIs = messages.getString("ColLengthIs");
        colNotText = messages.getString("ColNotText");
        invalCharSet = messages.getString("InvalCharSet");
        colLenZero = messages.getString("ColLenZero");
        colUnchg = messages.getString("ColUnchg");

        labelStart = new JLabel(startOfText);
        labelLength = new JLabel(lengthOfText);
        labelFind = new JLabel(findText);

        textStart = new JTextField("");
        textStart.setText(String.valueOf(startPosition));
        textLength = new JTextField("");
        textLength.setText(String.valueOf(inputLength));
        textFind = new JTextField("");
        textStart.setMinimumSize(new Dimension(100, 25));
        textStart.setMaximumSize(new Dimension(100, 25));
        textStart.setPreferredSize(new Dimension(100, 25));
        textStart.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        textLength.setMinimumSize(new Dimension(100, 25));
        textLength.setMaximumSize(new Dimension(100, 25));
        textLength.setPreferredSize(new Dimension(100, 25));
        textLength.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        textFind.setMinimumSize(new Dimension(200, 25));
        textFind.setMaximumSize(new Dimension(200, 25));
        textFind.setPreferredSize(new Dimension(200, 25));
        textFind.setFont(new Font("Monospaced", Font.PLAIN, fontSize));

        inputPanel = new JPanel();

        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
        inputPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        inputPanel.add(labelStart);
        inputPanel.add(textStart);
        inputPanel.add(labelLength);
        inputPanel.add(textLength);
        inputPanel.add(labelFind);
        inputPanel.add(textFind);

        returnButton = new JButton(_return);
        returnButton.setMinimumSize(new Dimension(95, 35));
        returnButton.setMaximumSize(new Dimension(95, 35));
        returnButton.setPreferredSize(new Dimension(95, 35));

        refreshButton = new JButton(refresh);
        refreshButton.setMinimumSize(new Dimension(150, 35));
        refreshButton.setMaximumSize(new Dimension(150, 35));
        refreshButton.setPreferredSize(new Dimension(150, 35));

        saveButton = new JButton(save);
        saveButton.setMinimumSize(new Dimension(95, 35));
        saveButton.setMaximumSize(new Dimension(95, 35));
        saveButton.setPreferredSize(new Dimension(95, 35));

        getButton = new JButton(getFile);
        getButton.setMinimumSize(new Dimension(110, 35));
        getButton.setMaximumSize(new Dimension(110, 35));
        getButton.setPreferredSize(new Dimension(110, 35));

        putButton = new JButton(putFile);
        putButton.setMinimumSize(new Dimension(110, 35));
        putButton.setMaximumSize(new Dimension(110, 35));
        putButton.setPreferredSize(new Dimension(110, 35));

        previewButton = new JButton(pagset);
        previewButton.setMinimumSize(new Dimension(130, 35));
        previewButton.setMaximumSize(new Dimension(130, 35));
        previewButton.setPreferredSize(new Dimension(130, 35));

        printButton = new JButton(print);
        printButton.setMinimumSize(new Dimension(95, 35));
        printButton.setMaximumSize(new Dimension(95, 35));
        printButton.setPreferredSize(new Dimension(95, 35));

        // Build window
        // ------------
        // Set text to inputTextArea
        workTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        workTextArea.setEditable(true);
        // Dim blue background
        workTextArea.setBackground(VERY_LIGHT_BLUE);

        JLabel title = new JLabel(updateColumn + colName + " - " + charCode);
        Font titleFont = new Font("Helvetica", Font.PLAIN, 20);
        title.setFont(titleFont);

        // Build button row
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        buttonPanel.add(returnButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(refreshButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(getButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(putButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonPanel.add(previewButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 60)));
        buttonPanel.add(printButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 60)));

        // Message panels
        msgPanel = new JPanel();
        BoxLayout msgLayoutX = new BoxLayout(msgPanel, BoxLayout.X_AXIS);
        msgPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        msgPanel.setLayout(msgLayoutX);

        if (clobLength < 0) {
            msg.setForeground(DIM_RED); // red
            msg.setText(colUndef);
            msgPanel.add(msg);
        } else if (clobLength >= 0) {
            msg.setForeground(DIM_BLUE); // blue
            msg.setText(colLengthIs + clobLength + ".");
            msgPanel.add(msg);
        }

        // Scroll pane with work text area
        scrollPane.setViewportView(workTextArea);
        // Maximum heigth - this big number ensures visibility of all the window.
        // If no max. size is specified the bottom of the window is
        // invisible under the screen.
        screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        scrollPane.setMaximumSize(new Dimension(screenWidth - 25, screenHeight - 215));

        // Lay out components in the window
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(
                layout.createParallelGroup(Alignment.LEADING).addComponent(title)
                .addComponent(inputPanel).addComponent(scrollPane).addComponent(buttonPanel)
                .addComponent(msgPanel)));
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
                layout.createSequentialGroup().addComponent(title).addComponent(inputPanel)
                .addComponent(scrollPane).addComponent(buttonPanel).addComponent(msgPanel)));
        globalPanel.setLayout(layout);
        globalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Set "Return" button activity
        // ----------------------------
        returnButton.addActionListener(a -> {
            // Save the updated CLOB object, start position, work area length
            // and message label into U_ClobReturnedValues object "retValues"
            // for the user (caller) of this class to get these values for further processing
            retValues.setClob(clob);
            retValues.setStartPos(startPosition);
            retValues.setLength(workTextArea.getText().length());
            retValues.setMsg(msg);
            dispose();
        });

        // Set "Refresh" button activity
        // -----------------------------
        refreshButton.addActionListener(a -> {
            msg.setForeground(DIM_BLUE); // blue
            msg.setText(" ");
            msgPanel.removeAll();
            msgPanel.add(msg);
            refreshWindow();
            pack();
            setVisible(true);
        });

        // Set "Save data" button activity
        // -------------------------------
        saveButton.addActionListener(a -> {
            Writer writer;
            long areaLength = workTextArea.getText().length();
            try {
                // clob.truncate(0);
                // Write contents of work area to the CLOB from the start position
                writer = this.clob.setCharacterStream(this.startPosition);
                writer.write(workTextArea.getText());
                retValues.setClob(this.clob);
                if (areaLength >= 0) {
                    textLength.setText(String.valueOf(areaLength));
                    msg.setForeground(DIM_BLUE); // blue
                    msg.setText(contentLoaded + areaLength);
                }
                msgPanel.add(msg);
            } catch (Exception e) {
                e.printStackTrace();
                msg.setForeground(DIM_RED); // red
                msg.setText(e.getCause().toString() + clobCapacity);
                msgPanel.add(msg);
            }
            refreshWindow();
            // Show the window
            pack();
            setVisible(true);
        });

        // Set Get file button activity
        // ----------------------------
        getButton.addActionListener(al -> {
            long fileLength;
            msg.setText(" ");
            msg.setForeground(DIM_BLUE); // Dim blue
            // Invoke file chooser dialog to choose a file for the CLOB column
            U_GetFile getFile = new U_GetFile();
            // The dialog delivers a File which contains data of the file
            this.dataTable.file = getFile.getFile(this.dataTable.file);
            Writer writer;
            String string;
            int resLen = (int) clobLength;

            // If the file is null (the user canceled the dialog)
            if (this.dataTable.file == null) {
                // Set CLOB to null and close the window
                this.clob = null;
                retValues.setClob(this.clob);
                retValues.setStartPos(1);
                retValues.setLength(0);
                retValues.setMsg(msg);
                dispose();
            } // If the file is non-null (the user chose a file)
            else {
                int colCapacity = 0;
                fileLength = this.dataTable.file.length();
                // Find column capacity
                for (int idx = 0; idx < this.dataTable.clobColNames.size(); idx++) {
                    if (colName.equals(this.dataTable.clobColNames.get(idx))) {
                        colCapacity = this.dataTable.clobColSizes.get(idx);
                        break;
                    }
                }
                if (fileLength > colCapacity) {
                    msg.setForeground(DIM_RED); // red
                    msg.setText(clobCapacity + fileLength + ".");
                    pack();
                    repaint();
//                  setVisible(true);
                } else {
                    try {
                        // Get list of all text lines from the file
                        List<String> list = Files.readAllLines(this.dataTable.file.toPath(),
                                Charset.forName(charCode));
                        if (list != null) {
                            // Set character stream to the CLOB and get a writer
                            writer = this.clob.setCharacterStream(1);
                            // Concatenate all text lines of the list
                            string = list.stream().reduce("", (a, b) -> a + b + "\n");
                            // Erase CLOB contents
                            this.clob.truncate(0);
                            // Set text from the file to the CLOB using a writer
                            writer.write(string);
                            resLen = string.length();
                            // Copy CLOB contents from input start position
                            // in the input length to work area to be displayed
                            workTextArea.setText(this.clob.getSubString(this.startPosition, resLen));
                            // Save the updated CLOB object, start position
                            // and work area length
                            // into U_ClobReturnedValues object "retValues"
                            // for the user (caller) of this class
                            // to get these values for further processing
                            retValues.setClob(this.clob);
                            retValues.setStartPos(1);
                            retValues.setLength(resLen);
                            msg.setForeground(DIM_BLUE); // blue
                            msg.setText(colLengthIs + resLen);
                        } else {
                            this.clob = null;
                            resLen = 0;
                            dispose();
                        }

                    } catch (MalformedInputException mie) {
                        resLen = (int) clobLength;
                        mie.printStackTrace();
                        msg.setText(invalCharSet + charCode + colUnchg + ", " + mie.getLocalizedMessage());
                        msg.setForeground(DIM_RED); // red
                    } catch (IOException ioe) {
                        resLen = (int) clobLength;
                        ioe.printStackTrace();
                        msg.setText(invalCharSet + charCode + colUnchg + ", " + ioe.getLocalizedMessage());
                        msg.setForeground(DIM_RED); // red
                    } catch (SQLException sqle) {
                        System.out.println(sqle.getCause());
                        sqle.printStackTrace();
                        msg.setText(invalCharSet + charCode + colUnchg + ", " + sqle.getLocalizedMessage());
                        msg.setForeground(DIM_RED); // red
                    } finally {
                        retValues.setClob(this.clob);
                        retValues.setStartPos(1);
                        retValues.setLength(resLen);
                    }
                }
                textLength.setText(String.valueOf(resLen));
                refreshWindow();
                msgPanel.removeAll();
                msgPanel.add(msg);
                // Show the window
                pack();
                repaint();
//               setVisible(true);
            }
        });

        // Set Preview button activity
        // ---------------------------
        previewButton.addActionListener(a -> {
            U_ClobPrintSetting printSetting = new U_ClobPrintSetting();
            printSetting.buildDataWindow();
            setPrintParams();
        });

        // Set Put file button activity
        // ----------------------------
        putButton.addActionListener(a -> {
            // Invoke file chooser dialog to choose a file for the CLOB column
            U_PutFile putFile = new U_PutFile();
            // The dialog delivers a Reader which contains data of the file
            this.dataTable.file = putFile.putFile(this.dataTable.file);
            if (this.dataTable.file != null) {
                try {
                    String string = clob.getSubString(1, (int) clob.length());
                    String[] lines = string.split("\n");
                    ArrayList<String> arrlines = new ArrayList<>();
                    for (int idx = 0; idx < lines.length; idx++) {
                        arrlines.add(idx, lines[idx]);
                    }
                    Files.write(this.dataTable.file.toPath(), arrlines, Charset.forName(charCode));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Set Print button activity
        // --------------------------
        printButton.addActionListener(a -> {
            // Convert lines in text area into array list of lines
            // needed for page rendering
            lineList = new ArrayList<>();
            String[] strArr = workTextArea.getText().split("\n");
            for (int idx = 0; idx < strArr.length; idx++) {
                lineList.add(idx, strArr[idx]);
            }

            // Print contents of the work area
            printResult();

            new U_ClobUpdate(this.dataTable);

            // Create and show the window again to make up the printing activity
//            createWindow(colName, clob, this.startPosition, this.inputLength);
        });

        // Window listener saves return values for the caller on closing the
        // window
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Save the updated CLOB object, start position, work area length
                // and message label into U_ClobReturnedValues object "retValues"
                // for the user (caller) of this class to get these values for
                // further
                // processing
                retValues.setClob(clob);
                retValues.setStartPos(startPosition);
                retValues.setLength(workTextArea.getText().length());
                retValues.setMsg(msg);
                dispose();
            }
        });

        // Enable ENTER key to refresh action
        // ----------------------------------
        globalPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke("ENTER"), "refresh");
        globalPanel.getActionMap().put("refresh", new Actiton());

        container = getContentPane();
        container.add(globalPanel);

        // Show the window
        // ---------------
        // Y = pack the window to actual contents, N = set fixed size
        if (autoWindowSize.equals("N")) {
            setSize(windowWidth, windowHeight);
        } else {
            pack();
        }
        setLocation(0, 0);
        // Refresh contents of the window
        refreshWindow();
        setVisible(true);
    } // End of createWindow()

    /**
     * Refresh window
     */
    protected void refreshWindow() {
        long startPosition = new Integer(textStart.getText());
        String searchText = textFind.getText();
        // If search text is not empty try to find it in the CLOB
        // from the start position on.
        if (!searchText.isEmpty()) {
            try {
                positionFound = this.clob.position(searchText, startPosition);
                System.out.println("positionFound: " + positionFound);
                if (positionFound > 0) // If the text is found - update the start position
                // to the position of the text found.
                {
                    this.startPosition = (int) positionFound;
                } else {
                    // If the tex was not found - send error message.
                    msg.setText(notFound);
                    msg.setForeground(DIM_RED); // red
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Get start position and length from text fields
        if (textFind.getText().isEmpty()) {
            this.startPosition = new Integer(textStart.getText());
        }

        // Input length from the text length
        inputLength = new Integer(textLength.getText());

        try {
            // Copy CLOB contents from input start position in the input
            // length to work area to be displayed
            workTextArea.setText(this.clob.getSubString(this.startPosition, (int) inputLength));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save position and length back to input fields
        textStart.setText(String.valueOf(this.startPosition));
        textLength.setText(String.valueOf(workTextArea.getText().length()));

        // Save the updated CLOB object, start position and work area length
        // into U_ClobReturnedValues object "retValues"
        // for the user (caller) of this class to get these values for
        // further processing
        retValues.setClob(this.clob);
        retValues.setStartPos(this.startPosition);
        retValues.setLength(workTextArea.getText().length());
    }

    /**
     * Returns values saved in U_ClobReturnedValues class
     *
     * @return
     */
    public U_ClobReturnedValues getReturnedValues() {
        return retValues;
    }

    @SuppressWarnings("ConvertToStringSwitch")
    protected void setPrintParams() {
        paperSize = properties.getProperty("PAPER_SIZE");
        printFontSize = new Integer(properties.getProperty("PRINT_FONT_SIZE"));
        orientation = properties.getProperty("ORIENTATION");
        if (orientation.equals("L") || orientation.equals("LANDSCAPE")) {
            orientation = "LANDSCAPE";
        } else if (orientation.equals("P") || orientation.equals("PORTRAIT")) {
            orientation = "PORTRAIT";
        }
        leftMargin = new Integer(properties.getProperty("LEFT_MARGIN"));
        rightMargin = new Integer(properties.getProperty("RIGHT_MARGIN"));
        topMargin = new Integer(properties.getProperty("TOP_MARGIN"));
        bottomMargin = new Integer(properties.getProperty("BOTTOM_MARGIN"));

        // Default media size A4
        mediaSize = MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4);
        attr_set.add(Chromaticity.MONOCHROME);
        // Get width and heigth of the page (A4, A3)
        if (paperSize.toUpperCase().equals("A4")) {
            mediaSize = MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4);
            attr_set.add(MediaSizeName.ISO_A4);
        } else if (paperSize.toUpperCase().equals("A3")) {
            mediaSize = MediaSize.getMediaSizeForName(MediaSizeName.ISO_A3);
            attr_set.add(MediaSizeName.ISO_A3);
        } else if (paperSize.toUpperCase().equals("LETTER")) {
            mediaSize = MediaSize.getMediaSizeForName(MediaSizeName.NA_LETTER);
            attr_set.add(MediaSizeName.NA_LETTER);
        } else {
            // Default A4
            mediaSize = MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4);
            attr_set.add(MediaSizeName.ISO_A4);
        }
        // Get media size in millimeters
        float[] mmSize = mediaSize.getSize(MediaPrintableArea.MM);
        mmWidth = mmSize[0];
        mmHeight = mmSize[1];

        Paper paper = new Paper();
        // Set dimensions in print points corresponding to dimensions in millimeters

        paper.setSize((mmWidth) * pointsInMM, (mmHeight) * pointsInMM);

        // Mac OS X prints out of paper when leftMargin = 0 in Landscape
        Properties sysProp = System.getProperties();
        if (sysProp.getProperty("os.name").contains("Mac OS") && orientation.equals("LANDSCAPE")) {
            os_correction = 4f;
        } else {
            os_correction = 0;
        }

        // Width of the printable area of the paper in print points
        pagePrintableWidth = (int) ((mmWidth) * pointsInMM);
        // Length of the printable area of the paper in print points
        pagePrintableHeight = (int) ((mmHeight) * pointsInMM);

        // Set the printable (and imageable) area of the paper
        paper.setImageableArea(0, 0, pagePrintableWidth, pagePrintableHeight);

        if (orientation.equals("LANDSCAPE")) {
            attr_set.add(OrientationRequested.LANDSCAPE);
        } else {
            attr_set.add(OrientationRequested.PORTRAIT);
        }

        // Create a printer job
        printerJob = PrinterJob.getPrinterJob();

        // Get page format from the printer job with for a set of attributes
        pageFormat = printerJob.getPageFormat(attr_set);

        // Set the page format to the printer job
        pageFormat.setPaper(paper);
    }

    /**
     * Prints contents of the query result (file Print.txt)
     *
     */
    protected void printResult() {        
        // Page renderer is an object which prepares and prints a page
        pageRenderer = new FilePageRenderer(pageFormat);
        JScrollPane jsp = new JScrollPane(pageRenderer);
        scrollPane.removeAll();
        scrollPane.add(jsp);
        scrollPane.setViewportView(jsp);
        validate();

        printerJob.setPrintable(pageRenderer, pageFormat);

        // Printing with dialog
        boolean ok = printerJob.printDialog(attr_set);
        if (ok) {
            try {
                printerJob.print();
            } catch (PrinterException pe) {
                System.out.println(pe);
                pe.printStackTrace();
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        } else {
            // System.out.println("Printer dialog canceled.");
        }

        /* // ????		
       // Printing wihout dialog
		 try { 
		    printerJob.print(attr_set); 
		 } catch (Exception e) {
		    e.printStackTrace(); 
		 }
         */ // ????			
    }

    /**
     * Inner class for ENTER key
     */
    class Actiton extends AbstractAction {

        protected static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshWindow();
            pack();
            setVisible(true);
        }
    }

    /**
     * Class to render contents of the file for printing
     */
    class FilePageRenderer extends JComponent implements Printable {

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         *
         * @param pageFormat
         */
        public FilePageRenderer(PageFormat pageFormat) {
            font = new Font("Monospaced", Font.PLAIN, printFontSize);
            // The line vector contains all lines of the file to print
            lineVector = new ArrayList<>();
            for (int i = 0; i < lineList.size(); i++) {
                lineVector.add(lineList.get(i));
            }
            // Divide the lines into formatted pages
            formatPages(pageFormat);
        }

        /**
         * Format printer pages - divide lines from the file into pages
         *
         * @param pageFormat
         */
        final void formatPages(PageFormat pageFormat) {
            pageVector = new ArrayList<>();
            ArrayList<String> page = new ArrayList<>();

            if (orientation.equals("LANDSCAPE")) {
                // Heigth and Width are reversed
                imWidth = mmHeight * pointsInMM;
                imHeight = mmWidth * pointsInMM - 8f * pointsInMM;
                pageLengthInPoints = imHeight - (2 * bottomMargin + 5) * pointsInMM;
                pageLength = pageLengthInPoints / fontSize;
                pageWidthInPoints = imWidth;
                pageWidth = pageWidthInPoints / fontSize;
                nbrFooterLines = 2;
            } else {
                // Orientation Portrait
                imWidth = mmWidth * pointsInMM;
                imHeight = mmHeight * pointsInMM - 8f * pointsInMM;
                pageLengthInPoints = imHeight - (2 * bottomMargin + 5) * pointsInMM;
                pageLength = pageLengthInPoints / fontSize;
                pageWidthInPoints = imWidth;
                pageWidth = pageWidthInPoints / fontSize;
                nbrFooterLines = 2;
            }

            // Current y-coordinate of the line is measured in points (1/72 of an
            // inch). E. g. font size 12 gives 12/72 of an inch (25.4 mm) = 4.23 mm
            // Number of header lines is ZERO when printing result of a non-query statement.
            // Set y-coordinate to 0 - beginning of the page
            float y = 0;
            pageNumber = 1;
            int rest;
            // Subtract 3 for constant title lines of the query (script description and date)
            // because the lines are already present on the first page along with column headers.
            int lineCount = -3;

            // Add ordinary (except last shorter) pages to the page vector
            // -----------------------------------------------------------
            for (int indx = 0; indx < lineVector.size(); indx++) {
                // The current line is the string from the line vector at the current index
                String line = (String) lineVector.get(indx);
                // Add font size to y-coordinate for the next line
                // Test y-coordinate for end of page.
                // If the data line y-coordinate is greater than the end of page
                // (i. e. page height),
                // the current page is completed:
                // - a footer is added, the page is added to the page vector,
                // - a new page is created,
                // - header lines are added at the beginning of the page,
                // - the y-coordinate of DATA lines is reset to 0
                if (y >= pageLengthInPoints - nbrFooterLines * fontSize) {
                    // Add footer (two lines) at the end of page 2 lines
                    page.add("");
//                    page.add(titPage + pageNumber);
                    // Add the page to the page vector
                    pageVector.add(page);
                    // Create a new page
                    page = new ArrayList<>();

                    // Reset y-coordinate to the line after header lines
                    y = fontSize * nbrHdrLines;
                    // Increase page number
                    pageNumber++;
                    // Reset line count
                    lineCount = 0;
                }
                // Add font size to y-coordinate for drawing the next line
                y += fontSize;
                // Add the line to the current page and increment line count               
                page.add(line);
                lineCount++;
            }
            // Last page
            // ---------
            // rest of lines on the last page
            rest = (int) (pageLength - lineCount - nbrHdrLines + 1);

            /*
            System.out.println("pageLength: " + pageLength);
            System.out.println("rest: " + rest);
            System.out.println("nbrHdrLines: " + nbrHdrLines);
             */
            // Pad rest of page by empty lines before printing footer
            if (rest > rest - nbrFooterLines) {
                // Some lines are reserved for the footer (page number)
                for (int i = 0; i < rest - nbrFooterLines; i++) {
                    page.add("");
                }
            }
            // Footer at the end of the report
            page.add("");
//            page.add(titPage + pageNumber);

            // Add the last page to page vector -
            // if its length is greater than number of header lines
            if (page.size() > nbrHdrLines) {
                pageVector.add(page);
            }
        }

        /**
         * Prints the graphical picture of current page
         */
        @Override
        public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
            if (pageIndex >= pageVector.size()) {
                return NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) g;

            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            /*
			// Draw rectangle for testing
			Rectangle2D rectangle = new Rectangle2D.Float(0, 0, imWidth, imHeight);
			g2.setPaint(Color.BLACK);
			g2.setStroke(new BasicStroke((float) 2.0));
			g2.draw(rectangle);
             */
            // Clip the page text drawn below with this clip rectangle to blank out right margin
            Rectangle2D clipRectangle
                    = new Rectangle2D.Float(0, 0, imWidth - rightMargin * pointsInMM, imHeight);
            g2.setPaint(Color.WHITE); // make clip rectangle invisible
            // g2.setPaint(Color.RED); // make clip rectangle red
            g2.draw(clipRectangle);
            g2.setClip(clipRectangle);

            // Draw the page text lines
            ArrayList<String> page = (ArrayList<String>) pageVector.get(pageIndex);
            g2.setFont(font);
            g2.setPaint(Color.BLACK);
            // Draw print lines of the page in graphics context
            float x = (leftMargin + os_correction) * pointsInMM;
            // os_correction is 4f for Mac OS, 0 for Windows
            float y = fontSize + topMargin * pointsInMM;
            for (int i = 0; i < page.size(); i++) {
                String line = (String) page.get(i);
                if (line.length() > 0) {
                    g2.drawString(line, x, y);
                }
                y += fontSize;
            }

            // Paint contents of the page
            paint(g2);
            return PAGE_EXISTS;
        }
    }
   
    /**
     * Window adapter setting current coordinates of the window to properties.
     */
    class MainWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent we) {
            // Save the updated CLOB object, start position, work area length
            // and message label into U_ClobReturnedValues object "retValues"
            // for the user (caller) of this class to get these values for further processing
            retValues.setClob(clob);
            retValues.setStartPos(startPosition);
            retValues.setLength(workTextArea.getText().length());
            retValues.setMsg(msg);
            dispose();
        }
    }
}
