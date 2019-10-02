/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package update;

import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.Position;

/**
 * Starts SQL update application
 *
 * @author Vladimír Župka 2017
 *
 */
public class U_MainWindow_Parameters extends JFrame {

    final Color RED_LIGHTER = Color.getHSBColor(0.95f, 0.07f, 1); // red level 2

    int windowWidth = 650;
    int windowHeight = 800;

    Connection conn;
    U_ConnectDB connectDB = new U_ConnectDB();

    ResourceBundle titles;
    String selRun, parApp, titMenu;

    // Localized buttons
    ResourceBundle buttons;
    String run, param, sav;

    // Localized msgTextAreas
    ResourceBundle locMessages;
    String parSaved, invalFile, invalCharset;
    String curDir, noConnection, invalSchema, correctPar;

    // Localized labels
    String defParApp, adrSvr, usrName, library, file, member, fileSelect, autWin, winWidth,
            winHeight;
    String nullMark, colSpaces, fontSize, decPattern, fetchFirst, orEnter, char_set, charSelect;
    String noTable, noCharset, printFontSize, maxFldLen;
    String fileName = "";
    String libraryName;
    String charset;

    Path errPath = Paths.get(System.getProperty("user.dir"), "logfiles", "err.txt");
    Path outPath = Paths.get(System.getProperty("user.dir"), "logfiles", "out.txt");
    OutputStream errStream;
    OutputStream outStream;

    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    String encoding = System.getProperty("file.encoding");
    BufferedReader infile;
    BufferedWriter outfile;
    Container cont = getContentPane();
    GridBagLayout gridBagLayout = new GridBagLayout();

    GridBagConstraints gbc = new GridBagConstraints();

    JMenuBar menuBar;
    JMenu helpMenu;
    JMenuItem helpMenuItemEN;
    JMenuItem helpMenuItemCZ;

    JPanel titlePanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JPanel dataPanel = new JPanel();
    JPanel globalPanel = new JPanel();
    BoxLayout boxLayout = new BoxLayout(globalPanel, BoxLayout.Y_AXIS);

    JTextArea title;

    JRadioButton englishButton = new JRadioButton("English");
    JRadioButton czechButton = new JRadioButton("Česky");
    JCheckBox autoSizeButton = new JCheckBox("");

    ArrayList<String> libraries;
    ArrayList<String> fileNames;
    JComboBox<String> fileSelectButton;
    ArrayList<String> charsets;
    JComboBox<String> charSelectButton;

    // Initial parameter values - not to be empty when the application is
    // installed
    String language;
    JTextField hostTf = new JTextField();
    JTextField userNameTf = new JTextField();
    JTextField librariesTf = new JTextField();
    JTextField memberTf = new JTextField();
    JTextField charsetTf = new JTextField();
    String autoWindowSize = new String();
    JTextField windowWidthTf = new JTextField();
    JTextField windowHeightTf = new JTextField();
    JTextField nullMarkTf = new JTextField();
    JTextField fontSizeTf = new JTextField();
    JTextField fetchFirstTf = new JTextField();
    JTextField maxFldLengthTf = new JTextField();

    // These labels are NOT localized
    JTextArea englishLbl = new JTextArea("Application language. Restart the application after change.");
    JTextArea czechLbl = new JTextArea("Jazyk aplikace. Po změně spusťte aplikaci znovu.");

    // Labels for text fields to localize
    JTextArea hostLbl;
    JTextArea userNameLbl;
    JTextArea librariesLbl;
    JTextArea fileLbl;
    JTextArea memberLbl;
    JTextArea fileSelectLbl;
    JTextArea charsetLbl;
    JTextArea charSelectLbl;
    JTextArea autoSizeLbl;
    JTextArea windowWidthLbl;
    JTextArea windowHeightLbl;
    JTextArea nullMarkLbl;
    JTextArea fontSizeLbl;
    JTextArea fetchFirstLbl;
    JTextArea maxFldLengthLbl;

    String[] charSetNames = {"UTF-8", "US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32",
        "UTF-32BE", "UTF-32LE", "windows-1250", "windows-1251", "ISO-8859-1", "ISO-8859-2", "Big5",
        "Big5-HKSCS", "CESU-8", "EUC-JP", "EUC-KR", "GB18030", "GB2312", "GBK", "IBM-Thai",
        "IBM00858", "IBM01140", "IBM01141", "IBM01142", "IBM01143", "IBM01144", "IBM01145",
        "IBM01146", "IBM01147", "IBM01148", "IBM01149", "IBM037", "IBM1026", "IBM1047", "IBM273",
        "IBM277", "IBM278", "IBM280", "IBM284", "IBM285", "IBM290", "IBM297", "IBM420", "IBM424",
        "IBM437", "IBM500", "IBM775", "IBM850", "IBM852", "IBM855", "IBM857", "IBM860", "IBM861",
        "IBM862", "IBM863", "IBM864", "IBM865", "IBM866", "IBM868", "IBM869", "IBM870", "IBM871",
        "IBM918", "ISO-2022-CN", "ISO-2022-JP", "ISO-2022-JP-2", "ISO-2022-KR", "ISO-8859-13",
        "ISO-8859-15", "ISO-8859-3", "ISO-8859-4", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7",
        "ISO-8859-8", "ISO-8859-9", "JIS_X0201", "JIS_X0212-1990", "KOI8-R", "KOI8-U",
        "PDFDocEncoding", "Shift_JIS", "TIS-620", "windows-1252", "windows-1253", "windows-1254",
        "windows-1255", "windows-1256", "windows-1257", "windows-1258", "windows-31j",
        "x-Big5-HKSCS-2001", "x-Big5-Solaris", "x-COMPOUND_TEXT", "x-euc-jp-linux", "x-EUC-TW",
        "x-eucJP-Open", "x-IBM1006", "x-IBM1025", "x-IBM1046", "x-IBM1097", "x-IBM1098",
        "x-IBM1112", "x-IBM1122", "x-IBM1123", "x-IBM1124", "x-IBM1364", "x-IBM1381", "x-IBM1383",
        "x-IBM300", "x-IBM33722", "x-IBM737", "x-IBM833", "x-IBM834", "x-IBM856", "x-IBM874",
        "x-IBM875", "x-IBM921", "x-IBM922", "x-IBM930", "x-IBM933", "x-IBM935", "x-IBM937",
        "x-IBM939", "x-IBM942", "x-IBM942C", "x-IBM943", "x-IBM943C", "x-IBM948", "x-IBM949",
        "x-IBM949C", " x-IBM950", "x-IBM964", "x-IBM970", "x-ISCII91", "x-ISO-2022-CN-CNS",
        "x-ISO-2022-CN-GB", "x-iso-8859-11", "x-JIS0208", "x-JISAutoDetect", "x-Johab",
        "x-MacArabic", "x-MacCentralEurope", "x-MacCroatian", "x-MacCyrillic", "x-MacDingbat",
        "x-MacGreek", "x-MacHebrew", "x-MacIceland", "x-MacRoman", "x-MacRomania", "x-MacSymbol",
        "x-MacThai", "x-MacTurkish", "x-MacUkraine", "x-MS932_0213", "x-MS950-HKSCS",
        "x-MS950-HKSCS-XP", "x-mswin-936", "x-PCK", "x-SJIS_0213", "x-UTF-16LE-BOM",
        "X-UTF-32BE-BOM", "X-UTF-32LE-BOM", "x-windows-50220", "x-windows-50221", "x-windows-874",
        "x-windows-949", "x-windows-950", "x-windows-iso2022jp",};

    Properties properties;
    final String PROP_COMMENT = "Table Update for IBM i, © Vladimír Župka 2015";

    // JLabel menuMessage = new JLabel();

    // Button for saving data to parameter properties
    JButton saveButton;
    // Button to run the update data table
    JButton runUpdateButton;
    // Label at saveButton
    JTextArea orPressEnterLbl;

    // Messages are in text area
    JTextArea msgTextArea = new JTextArea();

    // The msgTextArea text area is in scroll pane
    JScrollPane scrollMessagePane = new JScrollPane(msgTextArea);

    final Color DIM_BLUE = new Color(50, 60, 160);
    final Color DIM_RED = new Color(190, 60, 50);
    Color VERY_LIGHT_BLUE = Color.getHSBColor(0.60f, 0.05f, 0.98f);

    /**
     * Constructor creates the window with application parameters
     */
    U_MainWindow_Parameters() {
        // Get or set application properties
        // ---------------------------------        
        Properties sysProp = System.getProperties();
        // Menu bar in Mac operating system will be in the system menu bar
        if (sysProp.get("os.name").toString().toUpperCase().contains("MAC")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        try {
            // If "paramfiles" directory doesn't exist, create one
            Path paramfilesPath = Paths.get(System.getProperty("user.dir"), "paramfiles");
            if (!Files.exists(paramfilesPath)) {
                Files.createDirectory(paramfilesPath);
            }
            // If "logfiles" directory doesn't exist, create one
            Path logfilesPath = Paths.get(System.getProperty("user.dir"), "logfiles");
            if (!Files.exists(logfilesPath)) {
                Files.createDirectory(logfilesPath);
            }
            // If "selectfiles" directory doesn't exist, create one
            Path selectfilesPath = Paths.get(System.getProperty("user.dir"), "selectfiles");
            if (!Files.exists(selectfilesPath)) {
                Files.createDirectory(selectfilesPath);
            }
            // If "printfiles" directory doesn't exist, create one
            Path columnfilesPath = Paths.get(System.getProperty("user.dir"), "columnfiles");
            if (!Files.exists(columnfilesPath)) {
                Files.createDirectory(columnfilesPath);
            }

            // Redirect System.err, System.out to log files err.txt, out.txt in directory "logfiles"
            errStream = Files.newOutputStream(errPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            outStream = Files.newOutputStream(outPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrintStream errPrintStream = new PrintStream(errStream);
        PrintStream outPrintStream = new PrintStream(outStream);
        // PrintStream console = System.out;
        System.setErr(errPrintStream);
        System.setOut(outPrintStream);
        // System.setOut(console);

        properties = new Properties();
        try {
            // If the Parameters.txt file does not exist, create one
            // with default values.
            if (!Files.exists(parPath)) {
                Files.createFile(parPath);
                properties.setProperty("LANGUAGE", "cs-CZ");
                properties.setProperty("HOST", "193.179.195.133");
                properties.setProperty("USER_NAME", "VZUPKA");
                properties.setProperty("LIBRARY", "KOLEKCE");
                properties.setProperty("FILE", "CENY");
                properties.setProperty("MEMBER", "*FIRST");
                properties.setProperty("CHARSET", "UTF-8");
                properties.setProperty("AUTO_WINDOW_SIZE", "Y");
                properties.setProperty("RESULT_WINDOW_WIDTH", "950");
                properties.setProperty("RESULT_WINDOW_HEIGHT", "890");
                properties.setProperty("NULL_PRINT_MARK", "null");
                properties.setProperty("FONT_SIZE", "12");
                properties.setProperty("FETCH_FIRST", "1000");
                properties.setProperty("MAX_FIELD_LENGTH", "1000");
                properties.setProperty("PAPER_SIZE", "A4");
                properties.setProperty("PRINT_FONT_SIZE", "12");
                properties.setProperty("ORIENTATION", "PORTRAIT");
                properties.setProperty("LEFT_MARGIN", "10");
                properties.setProperty("RIGHT_MARGIN", "10");
                properties.setProperty("TOP_MARGIN", "10");
                properties.setProperty("BOTTOM_MARGIN", "10");

                // Create a new text file in directory "paramfiles"
                outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
                properties.store(outfile, PROP_COMMENT);
                outfile.close();
            }
            infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);
            infile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        libraryName = properties.getProperty("LIBRARY");
        fileName = properties.getProperty("FILE");
        memberTf.setText(properties.getProperty("MEMBER"));

        language = properties.getProperty("LANGUAGE");
        Locale currentLocale = Locale.forLanguageTag(language);

        // Get resource bundle classes
        titles = ResourceBundle.getBundle("locales.L_TitleLabelBundle", currentLocale);
        buttons = ResourceBundle.getBundle("locales.L_ButtonBundle", currentLocale);
        locMessages = ResourceBundle.getBundle("locales.L_MessageBundle", currentLocale);

        // Localized titles
        defParApp = titles.getString("DefParApp");
        adrSvr = titles.getString("AdrSvr");
        usrName = titles.getString("UsrName");
        library = titles.getString("Library");
        file = titles.getString("File");
        member = titles.getString("Member");
        fileSelect = titles.getString("FileSelect");
        char_set = titles.getString("Char_set");
        charSelect = titles.getString("CharSelect");
        noTable = titles.getString("NoTable");
        invalSchema = titles.getString("InvalSchema");

        autWin = titles.getString("AutWin");
        winWidth = titles.getString("WinWidth");
        winHeight = titles.getString("WinHeight");
        nullMark = titles.getString("NullMark");
        fontSize = titles.getString("FontSize");
        printFontSize = titles.getString("PrintFontSize");
        fetchFirst = titles.getString("FetchFirst");
        maxFldLen = titles.getString("MaxFldLen");

        orEnter = titles.getString("OrEnter");

        // Localized button label
        sav = buttons.getString("Sav");
        run = buttons.getString("Run");

        // Localized msgTextAreas
        curDir = locMessages.getString("CurDir");
        parSaved = locMessages.getString("ParSaved");
        invalFile = locMessages.getString("InvalFile");
        invalCharset = locMessages.getString("InvalCharset");
        noConnection = locMessages.getString("NoConnection");

        menuBar = new JMenuBar();
        helpMenu = new JMenu("Help");
        helpMenuItemEN = new JMenuItem("Help English");
        helpMenuItemCZ = new JMenuItem("Nápověda česky");

        helpMenu.add(helpMenuItemEN);
        helpMenu.add(helpMenuItemCZ);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar); // In macOS on the main system menu bar above, in Windows on the window menu bar
        // End of constructor
    }

    /**
     *
     */
    protected void createWindow() {

        // Title of the window
        title = new JTextArea(defParApp);

        // Labels for text fields
        hostLbl = new JTextArea(adrSvr);
        userNameLbl = new JTextArea(usrName);
        librariesLbl = new JTextArea(library);
        memberLbl = new JTextArea(member);
        fileSelectLbl = new JTextArea(fileSelect);
        charsetLbl = new JTextArea(char_set);
        charSelectLbl = new JTextArea(charSelect);
        autoSizeLbl = new JTextArea(autWin);
        windowWidthLbl = new JTextArea(winWidth);
        windowHeightLbl = new JTextArea(winHeight);
        nullMarkLbl = new JTextArea(nullMark);
        fontSizeLbl = new JTextArea(fontSize);
        fetchFirstLbl = new JTextArea(fetchFirst);
        maxFldLengthLbl = new JTextArea(maxFldLen);
        // Label near saveButton
        orPressEnterLbl = new JTextArea(orEnter);

        // Labels will have the same colors as background
        englishLbl.setBackground(titlePanel.getBackground());
        czechLbl.setBackground(titlePanel.getBackground());
        hostLbl.setBackground(titlePanel.getBackground());
        userNameLbl.setBackground(titlePanel.getBackground());
        librariesLbl.setBackground(titlePanel.getBackground());
        memberLbl.setBackground(titlePanel.getBackground());
        fileSelectLbl.setBackground(titlePanel.getBackground());
        charsetLbl.setBackground(titlePanel.getBackground());
        charSelectLbl.setBackground(titlePanel.getBackground());
        autoSizeLbl.setBackground(titlePanel.getBackground());
        windowWidthLbl.setBackground(titlePanel.getBackground());
        windowHeightLbl.setBackground(titlePanel.getBackground());
        nullMarkLbl.setBackground(titlePanel.getBackground());
        fontSizeLbl.setBackground(titlePanel.getBackground());
        fetchFirstLbl.setBackground(titlePanel.getBackground());
        maxFldLengthLbl.setBackground(titlePanel.getBackground());

        orPressEnterLbl.setBackground(titlePanel.getBackground());

        englishLbl.setEditable(false);
        czechLbl.setEditable(false);
        hostLbl.setEditable(false);
        userNameLbl.setEditable(false);
        librariesLbl.setEditable(false);
        memberLbl.setEditable(false);
        fileSelectLbl.setEditable(false);
        charsetLbl.setEditable(false);
        charSelectLbl.setEditable(false);
        autoSizeLbl.setEditable(false);
        windowWidthLbl.setEditable(false);
        windowHeightLbl.setEditable(false);
        nullMarkLbl.setEditable(false);
        fontSizeLbl.setEditable(false);
        fetchFirstLbl.setEditable(false);
        maxFldLengthLbl.setEditable(false);

        englishLbl.setFont(englishButton.getFont());
        czechLbl.setFont(englishButton.getFont());
        hostLbl.setFont(englishButton.getFont());
        userNameLbl.setFont(englishButton.getFont());
        librariesLbl.setFont(englishButton.getFont());
        memberLbl.setFont(englishButton.getFont());
        fileSelectLbl.setFont(englishButton.getFont());
        charsetLbl.setFont(englishButton.getFont());
        charSelectLbl.setFont(englishButton.getFont());
        autoSizeLbl.setFont(englishButton.getFont());
        windowWidthLbl.setFont(englishButton.getFont());
        windowHeightLbl.setFont(englishButton.getFont());
        nullMarkLbl.setFont(englishButton.getFont());
        fontSizeLbl.setFont(englishButton.getFont());
        fetchFirstLbl.setFont(englishButton.getFont());
        maxFldLengthLbl.setFont(englishButton.getFont());
        orPressEnterLbl.setFont(englishButton.getFont());

        // Label at the press button
        orPressEnterLbl.setEditable(false);
        orPressEnterLbl.setForeground(DIM_BLUE); // Dim blue

        // Message text area to contain messages
        msgTextArea.setEditable(false);
        msgTextArea.setBackground(titlePanel.getBackground());
        msgTextArea.setText(msgTextArea.getText() + curDir + System.getProperty("user.dir") + "\n");
        msgTextArea.setFont(englishButton.getFont());

        // Scroll pane for message text area
        scrollMessagePane.setBorder(null);

        // Register HelpWindow menu item listener
        helpMenuItemEN.addActionListener(ae -> {
            String command = ae.getActionCommand();
            if (command.equals("Help English")) {
                if (Desktop.isDesktopSupported()) {
                    String uri = Paths
                            .get(System.getProperty("user.dir"), "helpfiles", "IBMiSqlUpdateUserDocEn.pdf").toString();
                    // Replace backslashes by forward slashes in Windows
                    uri = uri.replace('\\', '/');
                    uri = uri.replace(" ", "%20");
                    try {
                        // Invoke the standard browser in the operating system
                        Desktop.getDesktop().browse(new URI("file://" + uri));
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            }
        });

        // Register HelpWindow menu item listener
        helpMenuItemCZ.addActionListener(ae -> {
            String command = ae.getActionCommand();
            if (command.equals("Nápověda česky")) {
                if (Desktop.isDesktopSupported()) {
                    String uri = Paths
                            .get(System.getProperty("user.dir"), "helpfiles", "IBMiSqlUpdateUserDocCz.pdf").toString();
                    // Replace backslashes by forward slashes in Windows
                    uri = uri.replace('\\', '/');
                    uri = uri.replace(" ", "%20");
                    try {
                        // Invoke the standard browser in the operating system
                        Desktop.getDesktop().browse(new URI("file://" + uri));
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
            }
        });

        title.setBackground(titlePanel.getBackground());
        title.setEditable(false);
        title.setFont(new Font("SansSerif", Font.PLAIN, 20));

        // Language radio buttons
        englishButton.setMnemonic(KeyEvent.VK_E);
        englishButton.setActionCommand("English");
        englishButton.setSelected(true);
        englishButton.setHorizontalTextPosition(SwingConstants.LEFT);

        czechButton.setMnemonic(KeyEvent.VK_C);
        czechButton.setActionCommand("Česky");
        czechButton.setSelected(false);
        czechButton.setHorizontalTextPosition(SwingConstants.LEFT);

        autoSizeButton.setSelected(false);

        // Button for saving data to parameter properties
        saveButton = new JButton(sav);

        saveButton.setPreferredSize(new Dimension(100, 40));
        saveButton.setMaximumSize(new Dimension(100, 40));
        saveButton.setMinimumSize(new Dimension(100, 40));

        runUpdateButton = new JButton(run);
        runUpdateButton.setPreferredSize(new Dimension(100, 40));
        runUpdateButton.setMaximumSize(new Dimension(100, 40));
        runUpdateButton.setMinimumSize(new Dimension(100, 40));

        // Radio and check buttons listeners
        // ---------------------------------
        // Set on English, set off Czech
        englishButton.addActionListener(ae -> {
            englishButton.setSelected(true);
            czechButton.setSelected(false);
            language = "en-US";
            System.out.println(ae.getActionCommand());
            System.out.println(language);
        });

        // Set on Czech, set off English
        czechButton.addActionListener(ae -> {
            czechButton.setSelected(true);
            englishButton.setSelected(false);
            language = "cs-CZ";
            System.out.println(ae.getActionCommand());
            System.out.println(language);
        });

        // Select or deselect automatic window size
        autoSizeButton.addItemListener(il -> {
            Object source = il.getSource();
            if (source == autoSizeButton) {
                if (autoSizeButton.isSelected()) {
                    autoWindowSize = "Y";
                } else {
                    autoWindowSize = "N";
                }
            }
        });

        // Get parameter properties
        // ------------------------
        // This parameter comes from radio buttons
        language = properties.getProperty("LANGUAGE");
        if (language.equals("en-US")) {
            englishButton.setSelected(true);
            czechButton.setSelected(false);
        } else if (language.equals("cs-CZ")) {
            czechButton.setSelected(true);
            englishButton.setSelected(false);
        }

        // The following parameters are editable
        hostTf.setText(properties.getProperty("HOST"));
        hostTf.setPreferredSize(new Dimension(120, 20));
        hostTf.setMinimumSize(new Dimension(120, 20));
        userNameTf.setText(properties.getProperty("USER_NAME"));
        userNameTf.setPreferredSize(new Dimension(120, 20));
        userNameTf.setMinimumSize(new Dimension(120, 20));
        librariesTf.setText(properties.getProperty("LIBRARY"));
        librariesTf.setPreferredSize(new Dimension(120, 20));
        librariesTf.setMinimumSize(new Dimension(120, 20));
        librariesTf.setForeground(Color.BLACK);

        memberTf.setPreferredSize(new Dimension(120, 20));
        memberTf.setMinimumSize(new Dimension(120, 20));
        charsetTf.setText(properties.getProperty("CHARSET"));
        charsetTf.setPreferredSize(new Dimension(160, 20));
        charsetTf.setMinimumSize(new Dimension(160, 20));

        // String "Y" or "N"
        autoWindowSize = properties.getProperty("AUTO_WINDOW_SIZE");
        //
        windowWidthTf.setText(properties.getProperty("RESULT_WINDOW_WIDTH"));
        windowHeightTf.setText(properties.getProperty("RESULT_WINDOW_HEIGHT"));
        nullMarkTf.setText(properties.getProperty("NULL_MARK"));
        fontSizeTf.setText(properties.getProperty("FONT_SIZE"));
        fetchFirstTf.setText(properties.getProperty("FETCH_FIRST"));
        maxFldLengthTf.setText(properties.getProperty("MAX_FIELD_LENGTH"));

        // Automatic size of the window with script results
        if (autoWindowSize.equals("Y")) {
            autoSizeButton.setSelected(true);
        } else {
            autoSizeButton.setSelected(false);
        }
        autoSizeButton.setHorizontalTextPosition(SwingConstants.LEFT);

        // Prepare file selection combo box
        // --------------------------------
        // Create file selection combo box filled by file names
        fileSelectButton = new JComboBox();
        fileSelectButton.setPreferredSize(new Dimension(140, 20));
        fileNames = new ArrayList<>(); // Empty list of file names

        // Set initial value for combo box
        // Check Library text field and set file names in the combo box if the library is correct
        if (checkLibrary()) {
            fileSelectButton.removeAllItems();
            for (int idx = 0; idx < fileNames.size(); idx++) {
                fileSelectButton.addItem(fileNames.get(idx));
            }
            fileSelectButton.setSelectedItem(fileName);
            librariesTf.setForeground(Color.BLACK);
        } else {
            librariesTf.setForeground(DIM_RED); // dim red
        }

        // Select file name from a list in combo box - listener
        // ----------------------------------------------------
        fileSelectButton.addItemListener(il -> {
            // If the file name list is empty - send message and return
            librariesTf.setForeground(Color.BLACK);
            /*
            if (fileNames.isEmpty()) {
                fileNames.add(noTable);
                librariesTf.setForeground(DIM_RED); // dim red
                return;
            }
             */
            // 
            JComboBox<String> source = (JComboBox<String>) il.getSource();
            memberTf.setForeground(Color.BLACK);
            //if (!fileNames.isEmpty()) {
            if (fileSelectButton.getItemCount() > 0) {
                fileName = (String) source.getSelectedItem();
            } else {
                memberTf.setForeground(DIM_RED);
                fileName = properties.getProperty("FILE");
                //memberTf.setText("*FIRST");
            }
            repaint();
        });

        // Libraries text field - listener
        librariesTf.addActionListener(al -> {
            saveData();
            checkLibrary();
        });

        // Prepare charset selection combo box
        // -----------------------------------
        charsets = new ArrayList<>(); // Empty vector of charset names
        // First element is the title
        // charsets.add(selectCharset);
        // Add charset names to the vector

        charsets.addAll(Arrays.asList(charSetNames));
        // Create a combo box
        charSelectButton = new JComboBox(charsets.toArray());

        // Selection is initialized by the input field value

        charSelectButton.setSelectedItem(charsetTf.getText());

        // Select charset name from the list in combo box - listener
        charSelectButton.addItemListener(il -> {
            JComboBox source = (JComboBox) il.getSource();
            charsetTf.setForeground(Color.BLACK);
            if (!charsets.isEmpty()) {
                charset = (String) source.getSelectedItem();
                // Set the new charset name in text field for properties
                charsetTf.setText(charset);
            } else {
                charsetTf.setText(noCharset);
            }
            repaint();
        });

        // Build the window
        // ================

        // Build title panel
        // -----------------
        titlePanel.add(title);
        titlePanel.setBorder(BorderFactory.createLineBorder(DIM_BLUE)); // blue
        titlePanel.setPreferredSize(new Dimension(windowWidth, 90));
        titlePanel.setMaximumSize(new Dimension(windowWidth, 90));
        titlePanel.setMinimumSize(new Dimension(windowWidth, 90));

        // Build button panel
        // ------------------
        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanelLayout.setAutoCreateGaps(false);
        buttonPanelLayout.setAutoCreateContainerGaps(true);
        buttonPanelLayout.setHorizontalGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(35)
                .addComponent(saveButton)
                .addComponent(orPressEnterLbl)
                .addComponent(runUpdateButton));
        buttonPanelLayout.setVerticalGroup(buttonPanelLayout
                .createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addGap(35)
                .addComponent(saveButton)
                .addComponent(orPressEnterLbl)
                .addComponent(runUpdateButton));
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanel.setPreferredSize(new Dimension(windowWidth - 240, 55));
        buttonPanel.setMaximumSize(new Dimension(windowWidth - 240, 55));
        buttonPanel.setMinimumSize(new Dimension(windowWidth - 240, 55));

        // Build data panel
        // ----------------
        dataPanel.setLayout(gridBagLayout);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);

        // internal padding of components
        gbc.ipadx = 0; // vodorovně
        gbc.ipady = 0; // svisle

        // Place text fields in column 0
        gbc.anchor = GridBagConstraints.EAST;

        gbc.gridx = 0;
        gbc.gridy = 0;

        dataPanel.add(englishButton, gbc);
        gbc.gridy++;
        dataPanel.add(czechButton, gbc);
        gbc.gridy++;
        dataPanel.add(hostTf, gbc);
        gbc.gridy++;
        dataPanel.add(userNameTf, gbc);
        gbc.gridy++;
        dataPanel.add(autoSizeButton, gbc);
        gbc.gridy++;
        dataPanel.add(windowWidthTf, gbc);
        gbc.gridy++;
        dataPanel.add(windowHeightTf, gbc);
        gbc.gridy++;
        dataPanel.add(nullMarkTf, gbc);
        gbc.gridy++;
        dataPanel.add(fontSizeTf, gbc);
        gbc.gridy++;
        dataPanel.add(fetchFirstTf, gbc);
        gbc.gridy++;
        dataPanel.add(maxFldLengthTf, gbc);
        gbc.gridy++;
        dataPanel.add(charsetTf, gbc);
        gbc.gridy++;
        dataPanel.add(charSelectButton, gbc);
        gbc.gridy++;
        dataPanel.add(librariesTf, gbc);
        gbc.gridy++;
        dataPanel.add(fileSelectButton, gbc);
        gbc.gridy++;
        dataPanel.add(memberTf, gbc);
        gbc.gridy++;

        // Place labels in column 1
        gbc.anchor = GridBagConstraints.WEST;
        // gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 1;
        gbc.gridy = 0;

        dataPanel.add(englishLbl, gbc);
        gbc.gridy++;
        dataPanel.add(czechLbl, gbc);
        gbc.gridy++;
        dataPanel.add(hostLbl, gbc);
        gbc.gridy++;
        dataPanel.add(userNameLbl, gbc);
        gbc.gridy++;
        dataPanel.add(autoSizeLbl, gbc);
        gbc.gridy++;
        dataPanel.add(windowWidthLbl, gbc);
        gbc.gridy++;
        dataPanel.add(windowHeightLbl, gbc);
        gbc.gridy++;
        dataPanel.add(nullMarkLbl, gbc);
        gbc.gridy++;
        dataPanel.add(fontSizeLbl, gbc);
        gbc.gridy++;
        dataPanel.add(fetchFirstLbl, gbc);
        gbc.gridy++;
        dataPanel.add(maxFldLengthLbl, gbc);
        gbc.gridy++;
        dataPanel.add(charsetLbl, gbc);
        gbc.gridy++;
        dataPanel.add(charSelectLbl, gbc);
        gbc.gridy++;
        dataPanel.add(librariesLbl, gbc);
        gbc.gridy++;
        dataPanel.add(fileSelectLbl, gbc);
        gbc.gridy++;
        dataPanel.add(memberLbl, gbc);
        gbc.gridy++;

        dataPanel.setPreferredSize(new Dimension(windowWidth, 550));
        dataPanel.setMaximumSize(new Dimension(windowWidth, 550));
        dataPanel.setMinimumSize(new Dimension(windowWidth, 550));

        // Button Save - listener
        saveButton.addActionListener(al -> {
            saveData();
            System.out.println(fileName + " saveButton  " + properties.getProperty("FILE"));
            checkLibrary();
        });

        // Button Run - listener
        runUpdateButton.addActionListener(al -> {
            runUpdate();
        });

        scrollMessagePane.setPreferredSize(new Dimension(windowWidth, 200));
        //      scrollMessagePane.setMinimumSize(new Dimension(windowWidth, 200));
        //      scrollMessagePane.setMaximumSize(new Dimension(windowWidth, 200));

        GroupLayout globalPanelLayout = new GroupLayout(globalPanel);
        globalPanelLayout.setAutoCreateGaps(false);
        globalPanelLayout.setAutoCreateContainerGaps(false);
        globalPanelLayout.setHorizontalGroup(globalPanelLayout
                .createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(titlePanel)
                .addComponent(dataPanel)
                .addComponent(buttonPanel)
                .addComponent(scrollMessagePane));
        globalPanelLayout.setVerticalGroup(globalPanelLayout.createSequentialGroup()
                .addComponent(titlePanel)
                .addComponent(dataPanel)
                .addComponent(buttonPanel)
                .addComponent(scrollMessagePane));
        globalPanel.setLayout(globalPanelLayout);

        // Enable ENTER key to save action
        globalPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ENTER"), "save");
        globalPanel.getActionMap().put("save", new SaveAction());

        //      globalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cont.add(globalPanel);

        // Make window visible
        setSize(windowWidth, windowHeight);
        setLocation(200, 40);
        //pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Saves input data to the parameters file
     */
    private boolean saveData() {

        // Check numeric values of some parameters
        String windowWidthString = checkNumber(windowWidthTf.getText());
        String windowHeightString = checkNumber(windowHeightTf.getText());
        String fontSiz = checkNumber(fontSizeTf.getText());
        // Check if charset from text field is in the list of charsets
        if (charsets.indexOf(charsetTf.getText()) < 0 || library.isEmpty()) {
            // charsetTf.setForeground(RED_LIGHTER); // dim red
            charsetTf.setBackground(RED_LIGHTER); // dim red
            charsetTf.setText(invalCharset);
        }

        // Put corrected parameters back to input fields for display
        windowWidthTf.setText(windowWidthString);
        windowHeightTf.setText(windowHeightString);
        fontSizeTf.setText(fontSiz);

        librariesTf.setText(librariesTf.getText().toUpperCase());
        if (memberTf.getText().isEmpty()) {
            memberTf.setText("*FIRST");
        }

        // Set properties with input values
        properties.setProperty("LANGUAGE", language);
        properties.setProperty("HOST", hostTf.getText());
        properties.setProperty("USER_NAME", userNameTf.getText());
        properties.setProperty("LIBRARY", librariesTf.getText().toUpperCase());
        properties.setProperty("FILE", fileName);
        properties.setProperty("MEMBER", memberTf.getText());
        properties.setProperty("CHARSET", charsetTf.getText());
        properties.setProperty("AUTO_WINDOW_SIZE", autoWindowSize);
        properties.setProperty("RESULT_WINDOW_WIDTH", windowWidthString);
        properties.setProperty("RESULT_WINDOW_HEIGHT", windowHeightString);
        properties.setProperty("NULL_MARK", nullMarkTf.getText());
        properties.setProperty("FONT_SIZE", fontSiz);
        properties.setProperty("FETCH_FIRST", fetchFirstTf.getText());
        properties.setProperty("MAX_FIELD_LENGTH", maxFldLengthTf.getText());

        // Store properties to output file
        try {
            // Store properties
            try (BufferedWriter outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding))) {
                // Store properties
                properties.store(outfile, PROP_COMMENT);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        /// msgTextArea.append(parSaved + "\n   " + parPath + "\n");
        return true;
    }

    /**
     * Run SQL update class
     */
    protected void runUpdate() {

        // Save new parameter data and show them
        saveData();
        checkLibrary();
        setVisible(true);

        // Call update table
        // =================
        U_DataTable dataTable = new U_DataTable(connectDB);
        dataTable.buildListWindow(dataTable.condition, dataTable.ordering);

        // Set caret at end of the text area, the slider is also set at end of the scroll pane.
        Document doc = msgTextArea.getDocument();
        Position endPos = doc.getEndPosition();
        msgTextArea.setCaretPosition(endPos.getOffset() - 1);

        // Reset file name in the combo box from properties
        fileSelectButton.setSelectedItem(properties.getProperty("FILE"));
    }

    /**
     * Check correctness of the library and set file list to the combo box
     *
     * @return
     */
    protected boolean checkLibrary() {
        library = librariesTf.getText();
        librariesTf.setForeground(Color.BLACK);
        librariesTf.setBackground(Color.WHITE);
        try {
            conn = connectDB.connect();
            if (conn != null) {
                DatabaseMetaData dmd = conn.getMetaData();
                ResultSet rs;
                // First check if the library name is in the list of libraries
                // (schemas)
                libraries = new ArrayList<>();
                rs = dmd.getSchemas();
                while (rs.next()) {
                    libraries.add(rs.getString(1));
                }
                rs.close();
                if (libraries.indexOf(library.toUpperCase()) == -1 || library.isEmpty()) {
                    // If the library name is not found in the list of libraries or is empty
                    // report message and color the field.
                    msgTextArea.append(invalSchema + " - " + library + "\n");
                    librariesTf.setBackground(RED_LIGHTER); // dim red
                    fileSelectButton.removeAllItems();
                    return false;
                }
                // Create list of database files (tables and views)
                // - physical and logical files
                // - from the schema - library - specified in Parameters
                fileNames = new ArrayList<>();
                // Build SELECT statement
                String stmtText = "select varchar(TABLE_SCHEMA, 10 ) " + librariesTf.getText()
                        + ", varchar(TABLE_NAME, 10) FILE"
                        + " from QSYS2.SYSTABLES"
                        + " where TABLE_SCHEMA = '" + librariesTf.getText()
                        + "' order by FILE";
                Statement stmt = conn.createStatement();
                ResultSet resSet = stmt.executeQuery(stmtText);
                while (resSet.next()) {
                    fileNames.add(resSet.getString("FILE"));
                }
                resSet.close();

                // Fill combo box with the list of file names
                fileSelectButton.removeAllItems();
                for (int idx = 0; idx < fileNames.size(); idx++) {
                    fileSelectButton.addItem(fileNames.get(idx));
                }
                // If the library names differ select the old file name from application parameters
                if (library.equals(libraryName)) {
                    fileSelectButton.setSelectedItem(properties.getProperty("FILE"));
                } else {
                    // Else select the first name from the list                
                    fileSelectButton.setSelectedItem(0);
                    // Replace library name in the field
                    librariesTf.setText(library);
                    // Make library names equal
                    libraryName = library;
                }
            } else {
                msgTextArea.append(noConnection + "\n");
            }
        } catch (Exception sqle) {
            // No connection to the database server
            System.out.println(noConnection);
            sqle.printStackTrace();
            msgTextArea.append(noConnection + "\n");
        }

        librariesTf.setText(librariesTf.getText().toUpperCase());
        memberTf.setText(memberTf.getText().toUpperCase());
        return true;
    }

    /**
     * Check input of a text field if it is numeric
     *
     * @param charNumber
     * @return String - echo if correct, "0" if not integer
     */
    protected String checkNumber(String charNumber) {
        try {
            Integer.getInteger(charNumber);
        } catch (NumberFormatException nfe) {
            charNumber = "0";
        }
        return charNumber;
    }

    /**
     * Class for saving data
     */
    class SaveAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            saveData();
            System.out.println(fileName + " SaveAction  " + properties.getProperty("FILE"));
            checkLibrary();
            setVisible(true);
        }
    }

    /**
     * Adjustment listener for MESSAGE scroll pane.
     */
    class MessageScrollPaneAdjustmentListenerMax implements AdjustmentListener {

        @Override
        public void adjustmentValueChanged(AdjustmentEvent ae) {
            // Set scroll pane to the bottom - the last element
            ae.getAdjustable().setValue(ae.getAdjustable().getMaximum());
        }
    }

    /**
     * Main class creates the object of this class and calls method to create the
     * window.
     *
     * @param strings
     * not used
     */
    public static void main(String... strings) {
        try {
            U_MainWindow_Parameters sqlUpdate = new U_MainWindow_Parameters();
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            sqlUpdate.createWindow();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

}
