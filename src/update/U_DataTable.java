package update;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Blob;
import java.sql.Clob;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Maintenance of database file member data
 *
 * @author Vladimír Župka 2016
 *
 */
public class U_DataTable extends JFrame {

    static final long serialVersionUID = 1L;

    // Path to the file containing modifications for SELECT statement
    Path selectPath;
    // Path to the file containing column list for SELECT statement
    Path columnsPath;

    String selectFileName;

    // Application parameters
    Properties properties;
    BufferedReader infile;
    BufferedWriter outfile;
    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    String encoding = System.getProperty("file.encoding");
    final String PROP_COMMENT = "SqlScripts for IBM i, © Vladimír Župka 2015";

    String library;
    String db_file;
    String file_member;
    String language;
    String nullMark;
    int fontSize;
    int windowHeight;
    int windowWidth;
    int screenWidth;
    int screenHeight;
    String autoWindowSize;
    String fetchFirst;
    String charCode;

    int maxFldWidth;

    // Connection to database
    Connection conn;
    // Object doing connection
    U_ConnectDB connectDB;

    // CLOB object
    Clob clob;
    U_ClobReturnedValues retClobValues;

    // BLOB object
    Blob blob;
    Blob blobReturned;
    long blobLength;
    U_BlobReturnedValues retBlobValues;

    // SQL attributes
    Statement stmt; // SQL statement object
    String stmtText; // SELECT statement text
    String condition; // condition in WHERE clause
    String whereClause; // WHERE clause (WHERE + condition)
    String ordering; // value in ORDER BY clause
    String orderByClause; // ORDER BY clause (ORDER BY + ordering)
    String actualColumnList = ""; // actual list of column names for SELECT
    String allColumnList = ""; // full list of column names for SELECT
    String normalColumnList = ""; // normal list of column names for SELECT
    String clobColumnList = ""; // list of CLOB type columns
    String blobColumnList = ""; // list of BLOB type columns

    ArrayList<String> allColNames; // all column names
    ArrayList<Integer> allColTypes; // all column types
    ArrayList<Integer> allColSizes; // all max. sizes of columns

    ArrayList<String> clobColNames; // clob column names
    ArrayList<Integer> clobColTypes; // clob column types
    ArrayList<Integer> clobColSizes; // clob max. sizes of columns

    ArrayList<String> blobColNames; // blob column names
    ArrayList<Integer> blobColTypes; // blob column types
    ArrayList<Integer> blobColSizes; // blob max. sizes of columns

    String[] colNames; // result set column names
    String[] colTypes; // result set column types
    int[] colSizes; // result set max. sizes of columns (number of characters)
    String[] colPrecisions; // number of digits
    String[] colScales; // number of decimal positions
    int colCapacity; // column capacity = size of CLOB or BLOB

    // Graphical table attributes
    JTable jTable; // graphical table
    TableModel tableModel; // JTable data model
    TableColumn[] tc; // table column array for rendering rows
    public Object[][] rows; // rows of JTable (two-dimensional array)
    int numOfRows; // number of rows in result set
    int numOfCols; // number of columns in result set
    int allNumOfCols; // number of all columns (including CLOB, ...)
    int clobNumOfCols; // number of CLOB columns
    int blobNumOfCols; // number of BLOB columns

    // Model for selection of rows in the table
    ListSelectionModel rowSelectionModel;
    ListSelectionModel rowIndexList;
    // Index of a table row selected (by the user or the program)
    int rowIndex;

    boolean addNewRecord = true; // flag when adding a new table row
    double cellFieldFactor; // factor for column widths in the jTable

    // Objects for SELECT modification values (WHERE, ORDER BY)
    JLabel labelWhere;
    JTextArea textAreaWhere;
    JLabel labelOrder;
    JTextArea textAreaOrder;

    // Objects for displaying the SQL statement
    JTextArea textAreaStmt;
    JPanel textAreaStmtPanel;

    // Components for building the list
    JLabel listTitle;
    JLabel listPrompt;
    JLabel message;
    JButton exitButton;
    JButton addButton;
    JButton updButton;
    JButton delButton;
    JButton refreshButton;
    JButton columnsButton;

    // Containers for building the list
    JPanel titlePanel;
    JPanel listPanel;
    JScrollPane scrollPaneList;
    JScrollPane scrollPaneStmt;
    JPanel buttonPanel;
    JPanel listMsgPanel;
    JPanel globalPanel;
    Container listContentPane;
    int listWidth, listHeight;
    int globalWidth, globalHeight;

    // Localized text objects
    Locale locale;
    ResourceBundle titles;
    String titEdit, changeCell, where, order, enterData;
    ResourceBundle buttons;
    String exit, insert, edit_sel, del_sel, refresh, columns, saveData, saveReturn, _return;
    ResourceBundle locMessages;
    String noRowUpd, noRowDel, noData, dataError, sqlError, invalidValue, invalidCharset, value, tooLong, length,
            tooLongForCol, contentLoaded, colValueNull, contentNotLoaded, colNotText, connLost;

    File file = null;

    final Color DIM_BLUE = new Color(50, 60, 160);
    final Color DIM_RED = new Color(190, 60, 50);
    Color VERY_LIGHT_BLUE = Color.getHSBColor(0.60f, 0.05f, 0.98f);

    /**
     * Constructor
     *
     * @param connectDB
     */
    @SuppressWarnings("ConvertToTryWithResources")

    public U_DataTable(U_ConnectDB connectDB) {
        this.connectDB = connectDB;

        // Try to connect database
        this.conn = connectDB.connect();
        try {
            // Application properties
            properties = new Properties();
            infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);
            infile.close();

            library = properties.getProperty("LIBRARY"); // library name
            db_file = properties.getProperty("FILE"); // file name
            file_member = properties.getProperty("MEMBER"); // member name
            language = properties.getProperty("LANGUAGE"); // local language
            charCode = properties.getProperty("CHARSET");
            windowHeight = new Integer(properties.getProperty("RESULT_WINDOW_HEIGHT"));
            windowWidth = new Integer(properties.getProperty("RESULT_WINDOW_WIDTH"));
            autoWindowSize = properties.getProperty("AUTO_WINDOW_SIZE");
            nullMark = properties.getProperty("NULL_MARK");
            fontSize = new Integer(properties.getProperty("FONT_SIZE"));
            // Factor to multiply cell width
            cellFieldFactor = fontSize * 0.75;
            // Max. number of rows in the result set
            fetchFirst = properties.getProperty("FETCH_FIRST");
            maxFldWidth = new Integer(properties.getProperty("MAX_FIELD_LENGTH"));

            // The first member has the same name as the file
            if (file_member.toUpperCase().equals("*FIRST")) {
                file_member = db_file;
                // But save *FIRST to properties
                properties.setProperty("MEMBER", "*FIRST");
            } else {
                // Save original member name to properties
                properties.setProperty("MEMBER", file_member.toUpperCase());
            }

            // Create a new text file in directory "paramfiles"
            outfile = Files.newBufferedWriter(parPath, Charset.forName(encoding));
            properties.store(outfile, PROP_COMMENT);
            outfile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Localization classes
        Locale currentLocale = Locale.forLanguageTag(language);
        titles = ResourceBundle.getBundle("locales.L_TitleLabelBundle", currentLocale);
        buttons = ResourceBundle.getBundle("locales.L_ButtonBundle", currentLocale);
        locMessages = ResourceBundle.getBundle("locales.L_MessageBundle", currentLocale);

        colNotText = locMessages.getString("ColNotText");

        // Localized button labels
        exit = buttons.getString("Exit");
        insert = buttons.getString("Insert");
        edit_sel = buttons.getString("Edit_sel");
        del_sel = buttons.getString("Del_sel");
        refresh = buttons.getString("Refresh");
        columns = buttons.getString("Columns");
        saveData = buttons.getString("SaveData");
        saveReturn = buttons.getString("SaveReturn");
        _return = buttons.getString("Return");

        // Name and path of the text file where user modifications
        // of SELECT statement (WHERE, ORDER BY) are preserved
        selectFileName = library.toUpperCase() + "-" + db_file.toUpperCase();
        selectPath = Paths.get(System.getProperty("user.dir"), "selectfiles", selectFileName + ".sel");

        // Read the file and get values of condition (WHERE)
        // and ordering (ORDER BY)
        try {
            // Create the file with two lines (if the file does not exist)
            if (!Files.exists(selectPath)) {
                ArrayList<String> lines = new ArrayList<>();
                // The file has initially exactly two lines.
                // The first line contains a semicolon,
                // the second line is empty.
                lines.add(";");
                lines.add("");
                // Create the file from the array list "lines" with two empty lines
                Files.write(selectPath, lines, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            // System.out.println("selectPath: " + selectPath);
            // Open the file
            BufferedReader infileSelect = Files.newBufferedReader(selectPath, Charset.forName("UTF-8"));
            // Read all (two) lines to get values for WHERE and ORDER BY clauses
            StringBuilder sb = new StringBuilder();
            String line = infileSelect.readLine();
            while (line != null) {
                sb.append(line);
                line = infileSelect.readLine();
            }
            // Split the string obtained from the file (has two parts).
            // The two values may be empty.
            String[] arr = (sb.toString()).split(";");
            condition = "";
            ordering = "";
            if (arr.length > 0) {
                condition = arr[0];
            }
            if (arr.length > 1) {
                ordering = arr[1];
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            // message.setText(colNotText);
            message.setText(exc.toString());
        }

        // Get full column list from the metadata of the table
        allColumnList = "";
        normalColumnList = "";
        clobColumnList = "";
        blobColumnList = "";

        try {
            DatabaseMetaData dmd = this.conn.getMetaData();
            ResultSet rs = dmd.getColumns(null, library, db_file, null);
            allNumOfCols = 0;
            allColNames = new ArrayList<>();
            allColTypes = new ArrayList<>();
            allColSizes = new ArrayList<>();
            clobNumOfCols = 0;
            clobColNames = new ArrayList<>();
            clobColTypes = new ArrayList<>();
            clobColSizes = new ArrayList<>();
            blobNumOfCols = 0;
            blobColNames = new ArrayList<>();
            blobColTypes = new ArrayList<>();
            blobColSizes = new ArrayList<>();

            while (rs.next()) {
                String colName = rs.getString(4);
                int colType = rs.getInt(5);
                int colSize = rs.getInt(7);
                allColumnList += ", " + colName;
                allColNames.add(colName);
                allColTypes.add(colType);
                allColSizes.add(colSize);

                allNumOfCols++;
                // Omit advanced column types
                // - they are incapable to render in a cell
                if (colType != java.sql.Types.CLOB && colType != java.sql.Types.NCLOB && colType != java.sql.Types.BLOB
                        && colType != java.sql.Types.ARRAY) // Add column name to the full list without advanced column types
                // - they are incapable to render in a cell
                {
                    normalColumnList += ", " + colName;
                }

                // Advanced columns are added to special lists
                if (colType == java.sql.Types.CLOB || colType == java.sql.Types.NCLOB) {
                    clobNumOfCols++;
                    clobColumnList += ", " + colName;
                    clobColNames.add(colName);
                    clobColTypes.add(colType);
                    clobColSizes.add(colSize);
                }

                if (colType == java.sql.Types.BLOB) {
                    blobNumOfCols++;
                    blobColumnList += ", " + colName;
                    blobColNames.add(colName);
                    blobColTypes.add(colType);
                    blobColSizes.add(colSize);
                }
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String aliasStmtText;
        try {
            stmt = conn.createStatement();
            aliasStmtText = "drop alias " + file_member.toUpperCase();
            stmt.execute(aliasStmtText);
        } catch (Exception exc) {
            //exc.printStackTrace();
        }
        try {
            aliasStmtText = "create alias " + file_member.toUpperCase() + " for " + db_file.toUpperCase() + "(" + file_member + ")";
            stmt.execute(aliasStmtText);
        } catch (Exception exc) {
            //exc.printStackTrace();
        }

        // Initially, the actual column list is the normal column list
        // without columns of "advanced" types (CLOB, BLOB, ARRAY)
        actualColumnList = normalColumnList;

        // Write the actual column list to the .col file for the database file (table)
        columnsPath = Paths.get(System.getProperty("user.dir"), "columnfiles", selectFileName
                + ".col");
        try {
            ArrayList<String> colArr = new ArrayList<>();
            colArr.add(actualColumnList);
            // Write file with columns list
            // Rewrite the existing file or create and write a new file.
            Files.write(columnsPath, colArr, StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            System.out.println("write columns file: " + ioe.getLocalizedMessage());
            //ioe.printStackTrace();
        }
    }

    /**
     * Build the window with the table rows (list)
     *
     * @param condition
     * @param ordering
     */
    protected void buildListWindow(String condition, String ordering) {
        this.condition = condition;
        this.ordering = ordering;

        // Construct WHERE and ORDER BY clauses if applicable
        if (!condition.isEmpty()) {
            whereClause = " WHERE " + condition;
        } else {
            whereClause = "";
        }
        if (!ordering.isEmpty()) {
            orderByClause = " ORDER BY " + ordering;
        } else {
            orderByClause = "";
        }

        // Start building the window
        // -------------------------
        textAreaWhere = new JTextArea(condition);
        textAreaOrder = new JTextArea(ordering);
        textAreaStmt = new JTextArea();

        exitButton = new JButton(exit);
        exitButton.setMinimumSize(new Dimension(70, 35));
        exitButton.setMaximumSize(new Dimension(70, 35));
        exitButton.setPreferredSize(new Dimension(70, 35));

        addButton = new JButton(insert);
        addButton.setMinimumSize(new Dimension(150, 35));
        addButton.setMaximumSize(new Dimension(150, 35));
        addButton.setPreferredSize(new Dimension(150, 35));

        updButton = new JButton(edit_sel);
        updButton.setMinimumSize(new Dimension(130, 35));
        updButton.setMaximumSize(new Dimension(130, 35));
        updButton.setPreferredSize(new Dimension(130, 35));

        refreshButton = new JButton(refresh);
        refreshButton.setMinimumSize(new Dimension(140, 35));
        refreshButton.setMaximumSize(new Dimension(140, 35));
        refreshButton.setPreferredSize(new Dimension(140, 35));

        delButton = new JButton(del_sel);
        delButton.setMinimumSize(new Dimension(130, 35));
        delButton.setMaximumSize(new Dimension(130, 35));
        delButton.setPreferredSize(new Dimension(130, 35));

        columnsButton = new JButton(columns);
        columnsButton.setMinimumSize(new Dimension(140, 35));
        columnsButton.setMaximumSize(new Dimension(140, 35));
        columnsButton.setPreferredSize(new Dimension(140, 35));

        titlePanel = new JPanel();
        listPanel = new JPanel();
        labelWhere = new JLabel();
        labelOrder = new JLabel();

        textAreaStmtPanel = new JPanel();
        listMsgPanel = new JPanel();
        buttonPanel = new JPanel();

        listTitle = new JLabel();
        listPrompt = new JLabel();
        message = new JLabel();

        textAreaStmt = new JTextArea();
        textAreaStmt.setFont(listPrompt.getFont());
        textAreaStmt.setEditable(false);
        textAreaStmt.setBackground(titlePanel.getBackground());
        scrollPaneStmt = new JScrollPane();

        // Localized messages
        noRowUpd = locMessages.getString("NoRowUpd");
        noRowDel = locMessages.getString("NoRowDel");
        noData = locMessages.getString("NoData");
        dataError = locMessages.getString("DataError");
        sqlError = locMessages.getString("SqlError");
        invalidValue = locMessages.getString("InvalidValue");
        invalidCharset = locMessages.getString("InvalidCharset");
        value = locMessages.getString("Value");
        tooLong = locMessages.getString("TooLong");
        contentLoaded = locMessages.getString("ContentLoaded");
        colValueNull = locMessages.getString("ColValueNull");
        connLost = locMessages.getString("ConnLost");

        // Evaluate modifications of the SELECT statement (WHERE, ORDER BY)
        // ----------------------
        evalModifications();

        // Get database table data using SELECT statement
        // -----------------------
        message = getData();
        if (!message.getText().equals("")) {
            message.setForeground(DIM_RED); // red
            listMsgPanel.add(message);
        }

        // Create the graphic table - listPanel - which is part of the window
        // ------------------------
        createTable();

        // Continue building the window using the listPanel just created
        // ----------------------------
        JLabel listTitle = new JLabel();
        BoxLayout boxLayoutY = new BoxLayout(titlePanel, BoxLayout.Y_AXIS);
        titlePanel.setLayout(boxLayoutY);

        // Localized titles
        titEdit = titles.getString("TitEdit") + library.toUpperCase() + "/" + db_file.toUpperCase();

        // If file name differs from member name - add member name in parentheses
        if (!db_file.equalsIgnoreCase(file_member)) {
            titEdit += "(" + file_member + ")";
        }
        listTitle.setText(titEdit);
        listTitle.setFont(new Font("Helvetica", Font.PLAIN, 20));
        listTitle.setMinimumSize(new Dimension(listWidth, 20));
        listTitle.setPreferredSize(new Dimension(listWidth, 20));
        listTitle.setMaximumSize(new Dimension(listWidth, 20));
        listTitle.setAlignmentX(Box.LEFT_ALIGNMENT);
        titlePanel.add(listTitle);
        titlePanel.add(Box.createRigidArea(new Dimension(10, 10)));

        // Align the title panel
        titlePanel.setAlignmentX(Box.CENTER_ALIGNMENT);

        changeCell = titles.getString("ChangeCell");
        listPrompt.setText(changeCell);
        listPrompt.setForeground(DIM_BLUE); // Dim blue
        listPrompt.setAlignmentX(Box.LEFT_ALIGNMENT);
        titlePanel.add(listPrompt);

        // User input for WHERE condition
        where = titles.getString("Where");
        labelWhere.setText(where + refresh + ".");
        BoxLayout labelWhereLayoutX = new BoxLayout(labelWhere, BoxLayout.X_AXIS);
        labelWhere.setLayout(labelWhereLayoutX);
        labelWhere.setMinimumSize(new Dimension(listWidth, 30));
        labelWhere.setPreferredSize(new Dimension(listWidth, 30));
        labelWhere.setMaximumSize(new Dimension(listWidth, 30));
        labelWhere.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        labelWhere.setForeground(DIM_BLUE); // blue
        labelWhere.setAlignmentX(LEFT_ALIGNMENT);

        BoxLayout areaWhereLayoutX = new BoxLayout(textAreaWhere, BoxLayout.X_AXIS);
        textAreaWhere.setFont(listPrompt.getFont());
        textAreaWhere.setLayout(areaWhereLayoutX);
        textAreaWhere.setMinimumSize(new Dimension(listWidth, 50));
        textAreaWhere.setPreferredSize(new Dimension(listWidth, 50));
        textAreaWhere.setMaximumSize(new Dimension(listWidth, 50));

        // User input for ORDER BY ordering
        order = titles.getString("Order");
        labelOrder.setText(order + refresh + ".");
        BoxLayout labelOrderLayoutX = new BoxLayout(labelOrder, BoxLayout.X_AXIS);
        labelOrder.setFont(listPrompt.getFont());
        labelOrder.setLayout(labelOrderLayoutX);
        labelOrder.setMinimumSize(new Dimension(listWidth, 30));
        labelOrder.setPreferredSize(new Dimension(listWidth, 30));
        labelOrder.setMaximumSize(new Dimension(listWidth, 30));
        labelOrder.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        labelOrder.setForeground(DIM_BLUE); // blue
        labelOrder.setAlignmentX(LEFT_ALIGNMENT);

        BoxLayout areaOrderLayoutX = new BoxLayout(textAreaOrder, BoxLayout.X_AXIS);
        textAreaOrder.setFont(listPrompt.getFont());
        textAreaOrder.setLayout(areaOrderLayoutX);
        textAreaOrder.setMinimumSize(new Dimension(listWidth, 20));
        textAreaOrder.setPreferredSize(new Dimension(listWidth, 20));
        textAreaOrder.setMaximumSize(new Dimension(listWidth, 20));

        // Statement panel
        BoxLayout areaStmtLayoutX = new BoxLayout(textAreaStmtPanel, BoxLayout.X_AXIS);
        textAreaStmtPanel.setLayout(areaStmtLayoutX);
        //      textAreaStmtPanel.setMinimumSize(new Dimension(listWidth, 100));
        textAreaStmtPanel.setPreferredSize(new Dimension(listWidth, 100));
        textAreaStmtPanel.setMaximumSize(new Dimension(listWidth, 100));
        textAreaStmtPanel.setAlignmentX(JTextArea.LEFT_ALIGNMENT);

        scrollPaneStmt.setBorder(null);

        scrollPaneStmt.setViewportView(textAreaStmt);

        // Message panel
        BoxLayout msgLayoutX = new BoxLayout(listMsgPanel, BoxLayout.X_AXIS);
        listMsgPanel.setLayout(msgLayoutX);
        listMsgPanel.setMinimumSize(new Dimension(listWidth, 20));
        listMsgPanel.setPreferredSize(new Dimension(listWidth, 20));
        listMsgPanel.setMaximumSize(new Dimension(listWidth, 20));

        // Button panel
        BoxLayout buttonLayoutX = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
        buttonPanel.setLayout(buttonLayoutX);
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(updButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(refreshButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(delButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(columnsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.setMinimumSize(new Dimension(listWidth, 60));
        buttonPanel.setPreferredSize(new Dimension(listWidth, 60));
        buttonPanel.setMaximumSize(new Dimension(listWidth, 60));

        // Global panel contains all other window objects
        globalPanel = new JPanel();

        // Create and register row selection model (for selecting a single row)
        // ---------------------------------------
        rowSelectionModel = jTable.getSelectionModel();
        jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Row selection model registration
        rowSelectionModel.addListSelectionListener(sl -> {
            rowIndexList = (ListSelectionModel) sl.getSource();
            rowIndex = rowIndexList.getLeadSelectionIndex();
            if (!rowIndexList.isSelectionEmpty()) {
                rowIndex = rowIndexList.getLeadSelectionIndex();
            } // No row was selected
            else {
                rowIndex = -1;
            }
        });

        // Set Exit button activity
        // ------------------------
        exitButton.addActionListener(a -> {
            String aliasStmtText;
            try {
                stmt = conn.createStatement();
                aliasStmtText = "drop alias " + file_member.toUpperCase();
                stmt.execute(aliasStmtText);
            } catch (Exception exc) {
                //exc.printStackTrace();
            }
            dispose();
        });

        // Set Add button activity
        // -----------------------
        addButton.addActionListener(a -> {
            addNewRecord = true;
            message.setText("");
            textAreaStmt.setText("");
            msg.setText("");
            dataMsgPanel.add(msg);

            if (rowIndexList != null) { // row index not empty
                if (rowIndex >= 0) {
                    // Remove list window container
                    listContentPane.removeAll();
                    rowIndex = rowIndexList.getLeadSelectionIndex();
                    // Create panel with data fields and buttons
                    buildDataWindow();
                    // Display data scroll pane with focus (to enable page keys)
                    this.add(scrollPaneData);
                    scrollPaneData.requestFocus();
                    setVisible(true);
                    pack();
                    rowIndexList = null;
                } else {
                    message.setText(noRowUpd);
                    message.setForeground(DIM_RED); // red
                    listMsgPanel.add(message);
                    textAreaStmt.setText(stmtText);
                    textAreaStmtPanel.add(scrollPaneStmt);
                    setVisible(true);
                }
            } else {
                rowIndex = -1;
                // System.out.println("rowIndex2: " + rowIndex);
                listContentPane.removeAll();
                // Create panel with data fields and buttons
                buildDataWindow();
                // Display data scroll pane with focus (to enable page keys)
                this.add(scrollPaneData);
                scrollPaneData.requestFocus();
                pack();
                setVisible(true);
            }
        });

        // Set Update button activity
        // --------------------------
        updButton.addActionListener(a -> {
            addNewRecord = false;
            message.setText("");
            textAreaStmt.setText("");
            msg.setText("");
            dataMsgPanel.add(msg);

            if (rowIndexList != null) { // row index not empty
                if (rowIndex >= 0) {
                    // Remove list window container
                    listContentPane.removeAll();
                    rowIndex = rowIndexList.getLeadSelectionIndex();
                    // Create panel with data fields and buttons
                    buildDataWindow();
                    // Display data scroll pane with focus (to enable page keys)
                    this.add(scrollPaneData);
                    scrollPaneData.requestFocus();
                    pack();
                    setVisible(true);
                    rowIndexList = null;
                } else {
                    message.setText(noRowUpd);
                    message.setForeground(DIM_RED); // red
                    listMsgPanel.add(message);
                    textAreaStmt.setText(stmtText);
                    textAreaStmtPanel.add(scrollPaneStmt);
                    setVisible(true);
                }
            } else {
                message.setText(noRowUpd);
                message.setForeground(DIM_RED); // red
                listMsgPanel.add(message);
                textAreaStmt.setText(stmtText);
                textAreaStmtPanel.add(scrollPaneStmt);
                setVisible(true);
            }
        });

        // Set Delete button activity
        // --------------------------
        delButton.addActionListener(a -> {
            message.setText("");
            textAreaStmt.setText("");
            // If filter list is not empty
            if (rowIndexList != null) {
                if (rowIndex >= 0) {
                    // Get sequential number from selected row
                    rowIndex = rowIndexList.getLeadSelectionIndex();
                    System.out.println("rowIndex: " + rowIndex);
                    BigDecimal rrn = (BigDecimal) rows[rowIndex][0];
                    System.out.println("rrn: " + rrn);

                    deleteRow(file_member, rrn);
                    // Evaluate WHERE and ORDER BY modifications
                    evalModifications();
                    getData();
                    refreshTableList();
                    repaint();
                    // Disable last row selection
                    rowIndexList = null;
                } else {
                    message.setText(noRowDel);
                    message.setForeground(DIM_RED); // Dim red
                    listMsgPanel.add(message);
                    textAreaStmt.setText(stmtText);
                    textAreaStmtPanel.add(scrollPaneStmt);
                    setVisible(true);
                }
            } else {
                message.setText(noRowDel);
                message.setForeground(DIM_RED); // Dim red
                listMsgPanel.add(message);
                textAreaStmt.setText(stmtText);
                textAreaStmtPanel.add(scrollPaneStmt);
                setVisible(true);
            }
        });

        // Set Refresh button activity
        // ---------------------------
        refreshButton.addActionListener(a -> {
            refreshTableList();
            message.setForeground(DIM_RED); // Dim red
            listMsgPanel.add(message);
            textAreaStmt.setText(stmtText);
            textAreaStmtPanel.add(scrollPaneStmt);
            setVisible(true);
        });

        // Set Columns button activity
        // ---------------------------
        columnsButton.addActionListener(a -> {
            new U_ColumnsJList(normalColumnList, selectFileName);
            refreshTableList();
        });

        // Finish the window building
        // --------------------------
        textAreaStmt.setText(stmtText);
        textAreaStmtPanel.add(scrollPaneStmt);

        listMsgPanel.add(message);
        listMsgPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Lay out components in the window in groups
        GroupLayout layout = new GroupLayout(globalPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                        .addComponent(titlePanel)
                        .addComponent(scrollPaneList)
                        .addComponent(labelWhere)
                        .addComponent(textAreaWhere)
                        .addComponent(labelOrder)
                        .addComponent(textAreaOrder)
                        .addComponent(textAreaStmtPanel)
                        .addComponent(listMsgPanel)
                        .addComponent(buttonPanel)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(titlePanel)
                        .addComponent(scrollPaneList)
                        .addComponent(labelWhere)
                        .addComponent(textAreaWhere)
                        .addComponent(labelOrder)
                        .addComponent(textAreaOrder)
                        .addComponent(textAreaStmtPanel)
                        .addComponent(listMsgPanel)
                        .addComponent(buttonPanel)));

        // Put the layout to the global panel
        globalPanel.setLayout(layout);

        // Put the global panel to a scroll pane
        JScrollPane scrollPaneTable = new JScrollPane(globalPanel);
        scrollPaneTable.setBorder(null);

        listContentPane = getContentPane(); // Window container
        listContentPane.removeAll(); // Important for resizing the window!

        // Put scroll pane to the window container
        listContentPane.add(scrollPaneTable);

        // Y = set window size for full contents, N = set fixed window size
        if (autoWindowSize.equals("Y")) {
            globalWidth = listWidth + 45;
            globalHeight = listHeight + 485;
            //pack();
        } else {
            globalWidth = windowWidth;
            globalHeight = windowHeight;
        }

        // Make window visible
        setSize(globalWidth, globalHeight);
        setLocation(0, 10);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * ************************************************************************
     * Builds data Window for inserting or updating data
     * *************************************************************************
     */
    Integer dataPanelGlobalWidth = 640;
    Integer minFldWidth = 40;
    JPanel dataGlobalPanel;
    //Container dataContentPane;
    JPanel dataMsgPanel = new JPanel();
    JScrollPane scrollPaneData;

    JTextField[] textFields;
    JLabel[] fldLabels;
    String[] txtFldLengths;

    ArrayList<JLabel> clobLabels;
    ArrayList<JButton> clobButtons;
    ArrayList<JLabel> blobLabels;
    ArrayList<JButton> blobButtons;

    String clobTypes[];

    GridBagLayout gridBagLayout = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    // Multiplication factor for the data field width (in characters)
    // to get field width in pixels
    int dataFieldFactor;

    /**
     * Build the window with data to be entered or rewritten.
     */
    String colName;
    JTextArea resultTextArea;
    int insertColumnNumber;
    PreparedStatement pstmt;
    ArrayList<String> values;
    long colStartPos;
    long colLength;
    Reader reader;
    InputStream stream;

    protected void buildDataWindow() {

        colStartPos = 1;
        colLength = new Integer(fetchFirst);

        // Localized messages
        length = locMessages.getString("Length");
        tooLongForCol = locMessages.getString("TooLongForCol");
        contentNotLoaded = locMessages.getString("ContentNotLoaded");
        colNotText = locMessages.getString("ColNotText");

        // Start building the window
        // -------------------------
        JLabel dataPanelTitle = new JLabel();
        enterData = titles.getString("EnterData");
        String panelText = enterData + library.toUpperCase() + "/" + db_file.toUpperCase();

        // If file name differs from member name - add member name in parentheses
        if (!db_file.equalsIgnoreCase(file_member)) {
            panelText += "(" + file_member + ")";
        }
        dataPanelTitle.setText(panelText);
        dataPanelTitle.setFont(new Font("Helvetica", Font.PLAIN, 20));

        JPanel titleDataPanel = new JPanel();
        titleDataPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        titleDataPanel.add(dataPanelTitle);
        titleDataPanel.setMinimumSize(new Dimension(dataPanelTitle.getPreferredSize().width, 30));
        titleDataPanel.setPreferredSize(new Dimension(dataPanelTitle.getPreferredSize().width, 30));
        titleDataPanel.setMaximumSize(new Dimension(dataPanelTitle.getPreferredSize().width, 30));

        // Create arrays of labels and (empty) text fields
        fldLabels = new JLabel[numOfCols];
        textFields = new JTextField[numOfCols];
        for (int idx = 0; idx < numOfCols; idx++) {
            // Column name, type and size
            if (colTypes[idx].equals("NUMERIC") || colTypes[idx].equals("DECIMAL")) {
                fldLabels[idx] = new JLabel(colNames[idx] + "  " + colTypes[idx] + " (" + colPrecisions[idx] + ", " + colScales[idx] + ")");
            } else {
                fldLabels[idx] = new JLabel(colNames[idx] + "  " + colTypes[idx] + " (" + colSizes[idx] + ")");
            }
            // Empty text field
            textFields[idx] = new JTextField("");
        }

        // Place data fields in grid bag for all columns
        // to input data panel
        // ---------------------------------------------
        JPanel inputDataPanel = new JPanel();
        // Grid bag layout used to lay out components
        inputDataPanel.setLayout(gridBagLayout);

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;

        // Create input text fields of normal table columns
        // ------------------------------------------------
        // RRN field (index 0) is omitted!
        dataFieldFactor = (int) (fontSize * 0.9);

        // Data in fields have default font and its size
        //Font defaultFont = UIManager.getDefaults().getFont("TabbedPane.font");
        // Text fields are proportional to the font size
        //dataFieldFactor = (int) (defaultFont.getSize());
        for (int idx = 1; idx < numOfCols; idx++) {
            int txtFieldLength = colSizes[idx] * dataFieldFactor;
            // Binary and variable binary columns will have twice as many characters (hex)
            if (colTypes[idx].equals("BINARY") || colTypes[idx].equals("VARBINARY")) {
                txtFieldLength *= 2;
            }
            if (txtFieldLength > maxFldWidth) {
                txtFieldLength = maxFldWidth;
            }
            if (txtFieldLength < minFldWidth) {
                txtFieldLength = minFldWidth;
            }
            textFields[idx].setMinimumSize(new Dimension(txtFieldLength, 25));
            textFields[idx].setMaximumSize(new Dimension(txtFieldLength, 25));
            textFields[idx].setPreferredSize(new Dimension(txtFieldLength, 25));
            textFields[idx].setFont(new Font("Monospaced", Font.PLAIN, fontSize));
            // textFields[idx].setFont(new Font("Monospaced", Font.PLAIN, defaultFont.getSize()));

            // For UPDATE display field values from the selected row.
            // Fill the text field with null mark or value from the table cell.

            // For INSERT display field values from the selected row or,
            // if no row is selected, display empty fields. 

            if (rowIndex > -1) { // -1 was set in "addNewRecord" if no row was selected
                if (rows[rowIndex][idx] == null) {
                    textFields[idx].setText(nullMark);
                } else {
                    textFields[idx].setText((rows[rowIndex][idx]).toString());
                }
            }

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.WEST;

            // Add column labels to the input data panel on the left
            inputDataPanel.add(fldLabels[idx], gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;

            // Add text fields to the input data panel on the right
            inputDataPanel.add(textFields[idx], gbc);
        }

        // Create labels and buttons of CLOB columns for UPDATE
        // ----------------------------------------------------
        clobLabels = new ArrayList<>();
        clobButtons = new ArrayList<>();
        clobTypes = new String[allNumOfCols];

        for (int idx = 0; idx < allNumOfCols; idx++) {
            if (allColTypes.get(idx) == java.sql.Types.CLOB || allColTypes.get(idx) == java.sql.Types.NCLOB) {
                if (allColTypes.get(idx) == java.sql.Types.CLOB) {
                    clobTypes[idx] = "CLOB";
                } else if (allColTypes.get(idx) == java.sql.Types.NCLOB) {
                    clobTypes[idx] = "NCLOB";
                }
                // Column name, type and size
                JLabel label = new JLabel(allColNames.get(idx) + "  " + clobTypes[idx] + " (" + allColSizes.get(idx) + ")");
                clobLabels.add(label);

                // Column CLOB Button. Its text is the column name!
                JButton button = new JButton(allColNames.get(idx));
                clobButtons.add(button);

                gbc.gridy++;
                gbc.gridx = 0;
                gbc.anchor = GridBagConstraints.WEST;

                // Add column label to the input data panel on the left
                inputDataPanel.add(label, gbc);

                // CLOB buttons are added for UPDATE only!
                if (!addNewRecord) {
                    gbc.gridx = 1;
                    gbc.anchor = GridBagConstraints.WEST;

                    // Add button to the input data panel on the right
                    inputDataPanel.add(button, gbc);
                }
            }
        }

        // Create labels and buttons of BLOB columns for UPDATE
        // ----------------------------------------------------
        blobLabels = new ArrayList<>();
        blobButtons = new ArrayList<>();

        for (int idx = 0; idx < allNumOfCols; idx++) {
            if (allColTypes.get(idx) == java.sql.Types.BLOB) {

                // Column name, type and size
                JLabel label = new JLabel(allColNames.get(idx) + "  " + "BLOB" + " ("
                        + allColSizes.get(idx) + ")");
                blobLabels.add(label);

                // Column CLOB Button. Its text is the column name!
                JButton button = new JButton(allColNames.get(idx));
                blobButtons.add(button);

                gbc.gridy++;
                gbc.gridx = 0;
                gbc.anchor = GridBagConstraints.WEST;

                // Add column label to the input data panel on the left
                inputDataPanel.add(label, gbc);

                // BLOB buttons are added for UPDATE only!
                if (!addNewRecord) {
                    gbc.gridx = 1;
                    gbc.anchor = GridBagConstraints.WEST;

                    // Add button to the input data panel on the right
                    inputDataPanel.add(button, gbc);
                }
            }
        }

        // Register listeners for all CLOB buttons for UPDATE
        // --------------------------------------------------
        if (!addNewRecord) {
            for (int idx = 0; idx < clobButtons.size(); idx++) {
                int index = idx;
                clobButtons.get(idx).addActionListener(ae -> {
                    // Column name is obtained from the ActionEvent ae (button text)
                    colName = ae.getActionCommand();
                    msg.setText(" ");
                    dataMsgPanel.removeAll();
                    dataMsgPanel.add(msg);
                    updateClob(index);
                });
            }
        }

        // Register listeners for all BLOB buttons for UPDATE
        // --------------------------------------------------
        if (!addNewRecord) {
            for (int idx = 0; idx < blobButtons.size(); idx++) {
                blobButtons.get(idx).addActionListener(ae -> {
                    // Column name is obtained from the ActionEvent ae (button text)
                    colName = ae.getActionCommand();
                    msg.setText(" ");
                    dataMsgPanel.removeAll();
                    dataMsgPanel.add(msg);
                    updateBlob();
                });
            }
        }

        // Build button row panel
        JButton saveButton = new JButton(saveData);
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
        buttonRow.add(Box.createRigidArea(new Dimension(10, 40)));
        buttonRow.add(saveAndReturnButton);
        buttonRow.add(Box.createRigidArea(new Dimension(10, 40)));
        buttonRow.add(dataPanelReturnButton);

        // Message panels
        BoxLayout msgLayoutX = new BoxLayout(dataMsgPanel, BoxLayout.LINE_AXIS);
        dataMsgPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
        dataMsgPanel.setLayout(msgLayoutX);

        msg.setText(" ");
        dataMsgPanel.removeAll();
        dataMsgPanel.add(msg);

        // Lay out components in groups
        dataGlobalPanel = new JPanel();
        GroupLayout layout = new GroupLayout(dataGlobalPanel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(titleDataPanel)
                .addComponent(buttonRow)
                .addComponent(dataMsgPanel)
                .addComponent(inputDataPanel)
        ));
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createSequentialGroup()
                .addComponent(titleDataPanel)
                .addComponent(buttonRow)
                .addComponent(dataMsgPanel)
                .addComponent(inputDataPanel)
        ));

        dataGlobalPanel.setLayout(layout);

        // Put data global panel to the scroll pane
        scrollPaneData = new JScrollPane(dataGlobalPanel);

        // Set Save button activity
        // --------------------------
        saveButton.addActionListener(a -> {
            if (!saveData()) {
                repaint();
                setVisible(true);
            }
        });

        // Set Save and Return button activity
        // -----------------------------------
        saveAndReturnButton.addActionListener(a -> {
            if (saveData()) {
                // Create the list window again
                buildListWindow(this.condition, this.ordering);
            } // Error when inserting or updating data
            else {
                repaint();
                setVisible(true);
            }
        });

        // Set Return button activity
        // --------------------------
        dataPanelReturnButton.addActionListener(a -> {
            listContentPane.removeAll();
            buildListWindow(this.condition, this.ordering);
        });

        // Enable ENTER key to save and return action
        // ------------------------------------------
        dataGlobalPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "saveData");
        dataGlobalPanel.getActionMap().put("saveData", new SaveAction());
    }

    /**
     * Evaluate modifications of the SELECT statement: WHERE condition and ORDER BY ordering.
     */
    protected void evalModifications() {
        condition = textAreaWhere.getText();
        if (!condition.equals("")) {
            whereClause = " WHERE " + condition;
        } else {
            textAreaWhere.setText("");
            whereClause = "";
        }
        ordering = textAreaOrder.getText();
        if (!ordering.equals("")) {
            orderByClause = " ORDER BY " + ordering;
        } else {
            textAreaOrder.setText("");
            orderByClause = "";
        }
    }

    /**
     * Refresh the list of rows in the table according to WHERE and ORDER BY modifications (if applicable).
     */
    protected void refreshTableList() {
        message.setText("");

        condition = textAreaWhere.getText();
        ordering = textAreaOrder.getText();

        // Save the modifications of the SELECT statement
        // obtained from the input text areas to the file.
        ArrayList<String> modifArr = new ArrayList<>();
        // Build an array of 2 items - condition and ordering
        // divided by a semicolon. Both items may contain New Line characters.
        modifArr.add(condition + ";");
        modifArr.add(ordering);
        // Write the array to the file thus preserving the user input
        try {
            // Write file with values of condition (WHERE) and ordering (ORDER BY)
            // Rewrite the existing file or create and write a new file.
            Files.write(selectPath, modifArr, StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            System.out.println("write file: " + ioe.getLocalizedMessage());
            ioe.printStackTrace();
        }

        // Re-create the table from the modified data (by add, update, or delete)
        listContentPane.removeAll();

        buildListWindow(condition, ordering);

        // Disable row selection
        rowIndexList = null;
        rowIndex = -1;
    }

    /**
     * Get data from the database file (table)
     *
     * @return
     */
    public JLabel getData() {
        String columnList;
        String[] columnArray;
        // Read actual select column list from the .col file
        try {
            List<String> items = Files.readAllLines(columnsPath);
            // System.out.println("items.toString(): "+items.toString());
            columnList = items.get(0);
            columnArray = columnList.split(",");
            actualColumnList = "";
            for (int idx = 0; idx < columnArray.length; idx++) {
                // 20 columns in a line, next columns in the next line
                if (idx % 20 != 0) {
                    actualColumnList += "," + columnArray[idx];
                } else {
                    actualColumnList += "\n";
                }
            }
            // System.out.println("actualColumnList:"+actualColumnList);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        ResultSet rs;
        try {
            // Build text of SELECT statement using column names and other variables.
            // ------------------------------
            // The first column will allways be the value of the RRN (Relative Record Number) 
            // of the given row in the original table.

            // Example:
            // select rrn(CENY2) as RRN, CZBOZI, CENAJ, NAZZBO, RAZITKO, DATUM, CAS
            //   from VZTOOL/CENY2
            //   fetch first 1000 rows only
            stmtText = "select rrn(" + file_member.toUpperCase() + ") as RRN" + actualColumnList
                    + "\n from " + library.toUpperCase() + "/" + file_member.toUpperCase();
            if (!whereClause.isEmpty()) {
                stmtText += "\n ";
            }
            stmtText += whereClause;
            if (!orderByClause.isEmpty()) {
                stmtText += "\n ";
            }
            stmtText += orderByClause;
            stmtText += "\n fetch first " + fetchFirst + " rows only";

            // Create statement Scroll insensitive and Concurrent updatable
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //            stmt = conn.createStatement();

            // Execute the SQL statement and obtain the Result Set rs.
            // -------------------------
            rs = stmt.executeQuery(stmtText);
            // An error may result from invalid statement

            // Get information on columns of the result set
            ResultSetMetaData rsmd = rs.getMetaData(); // data about result set
            numOfCols = rsmd.getColumnCount(); // number of columns in result set
            colNames = new String[numOfCols];
            colSizes = new int[numOfCols];
            colPrecisions = new String[numOfCols];
            colScales = new String[numOfCols];
            colTypes = new String[numOfCols];
            // Omit column 0 with RRN???
            listWidth = 0;
            for (int col = 0; col < numOfCols; col++) {
                colNames[col] = rsmd.getColumnName(col + 1);
                colTypes[col] = rsmd.getColumnTypeName(col + 1);
                colSizes[col] = rsmd.getColumnDisplaySize(col + 1);
                int colSize = colSizes[col];
                if (colTypes[col].equals("BINARY") || colTypes[col].equals("VARBINARY")) {
                    colSize *= 2;
                } else if (colTypes[col].equals("NUMERIC") || colTypes[col].equals("DECIMAL")) {
                    System.out.println("Precision: " + rs.getMetaData().getPrecision(col + 1));
                    System.out.println("Scale    : " + rsmd.getScale(col + 1));
                    colPrecisions[col] = String.valueOf(rs.getMetaData().getPrecision(col + 1));
                    colScales[col] = String.valueOf(rs.getMetaData().getScale(col + 1));
                }
                double maxFieldWidth = cellFieldFactor * Math.max(colSize, colNames[col].length());
                if (maxFieldWidth > maxFldWidth) {
                    maxFieldWidth = maxFldWidth;
                }
                // Add column widths to determine width of the window
                listWidth += maxFieldWidth;
            }
            // Reduce window width by part of RRN column width (BigDecimal)
            listWidth -= 100;
            // The width must fit all objects (mainly buttons)
            if (listWidth < 850) {
                listWidth = 850;
            }

            // Fill "rows" array by values from the result set rs
            // --------------------------------------------------
            // Set end of result set
            rs.last();
            // Get number of rows (the number of the last row)
            numOfRows = rs.getRow();
            rs.beforeFirst(); // set pointer before the first row

            // Create the "rows" array now when number of elements is known
            rows = new Object[numOfRows][numOfCols];
            while (rs.next()) {
                // Fields are numbered from 0, database columns from 1
                for (int col = 0; col < numOfCols; col++) {
                    // System.out.println("colTypes[col]: " + colTypes[col]);
                    // System.out.println("rs.getObject(col + 1): " +
                    // rs.getObject(col + 1));
                    // Set cell value
                    if (colTypes[col] == null) {
                        // If column value is null set null mark to the cell                        
                        rows[rs.getRow() - 1][col] = nullMark;
                    } else if (colTypes[col].equals("BINARY") || colTypes[col].equals("VARBINARY")) {
                        // If column type is BINARY or VARBINARY translate bytes in
                        // hexadecimal characters
                        int length = rs.getBytes(col + 1).length;
                        String hexString = "";
                        for (int idx = 0; idx < length; idx++) {
                            hexString += byteToHex(rs.getBytes(col + 1)[idx]);
                            // System.out.println("hexString: "+hexString);
                        }
                        rows[rs.getRow() - 1][col] = hexString;
                    } else {
                        // Otherwise set the cell value as Object
                        // that is automatically converted to the correct type.                        
                        rows[rs.getRow() - 1][col] = rs.getObject(col + 1);
                    }
                }
            }
            rs.close();
            stmt.close();
        } // end try
        catch (Exception exc) {
            exc.printStackTrace();
            System.out.println("Statement:\n" + stmtText);
            System.out.println("getData: " + exc.toString());
            // Connection to the server lost or data error
            message.setText(connLost + " - " + exc.toString() + "\n");
            // Initialize the .sel file that has invalid data (WHERE/ORDER BY).
            // If invalid data remained in the file after exiting the window
            // the window would never been displayed again.
            try {
                selectPath = Paths.get(System.getProperty("user.dir"), "selectfiles", selectFileName + ".sel");
                ArrayList<String> lines = new ArrayList<>();
                // The file has initially exactly two lines.
                // The first line contains a semicolon, the second line is empty.
                lines.add(";");
                lines.add("");
                // Create the file from the array list "lines" with two empty lines
                Files.write(selectPath, lines, Charset.forName("UTF-8"), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Display only 2 columns in 1 row
            // - the first is RRN, the second is an explaining text
            numOfCols = 2; // one column only
            numOfRows = 0; // no rows in table
            colNames = new String[2];
            colNames[0] = "!";
            colNames[1] = noData;
            colSizes = new int[2];
            colSizes[0] = 1;
            colSizes[1] = 1200; // Get enough space for long messages
            // Create a new data array
            rows = new Object[numOfRows][numOfCols];
            // System.out.println("colNames[1]: "+colNames[1]);
            //            listWidth = 1200;
            message.setForeground(DIM_RED); // red local message
        }
        return message;
    }

    /**
     * Create jTable in scrollPane in listPanel
     */
    protected void createTable() {
        // Create a new table with its own data model
        // ------------------------------------------
        tableModel = new TableModel();
        jTable = new JTable(tableModel);

        // Attributes of the table
        jTable.setRowHeight(25); // row height
        // color of grid lines
        jTable.setGridColor(Color.LIGHT_GRAY);
        // no resizing of columns 
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // font in cells
        jTable.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
        // font in header
        jTable.getTableHeader().setFont(new Font("Monospaced", Font.ITALIC, fontSize));
        // header height
        jTable.getTableHeader().setPreferredSize(new Dimension(0, 26));
        // no reordering of headers and columns
        jTable.getTableHeader().setReorderingAllowed(false);

        // Column model for column rendering and editing
        TableColumnModel tcm = jTable.getColumnModel();
        tc = new TableColumn[numOfCols];
        for (int col = 0; col < numOfCols; col++) {
            // Get table column object from the model
            tc[col] = tcm.getColumn(col);
            // Assign the cell editor to the table column
            tc[col].setCellEditor(new CellEditor());
        }
        // Column 0 - RRN - different background and foreground color
        tc[0].setCellRenderer(new ColorColumnRenderer(Color.WHITE, DIM_BLUE));
        tc[0].setHeaderRenderer(new ColorColumnRenderer(Color.WHITE, DIM_BLUE));

        // Column 0 - RRN - width will be adjusted to fit the column NAME.
        tc[0].setPreferredWidth((int) (Math.max(colSizes[0], cellFieldFactor * colNames[0].length()) + 5));

        // Other columns width will be adjusted by multiplication with cellFieldFactor
        for (int col = 1; col < numOfCols; col++) {
            int colSize = colSizes[col];
            if (colTypes != null) {
                // If the member of the file exists - columns may be processed
                if (colTypes[col].equals("BINARY") || colTypes[col].equals("VARBINARY")) {
                    // Binary columns are twice as wide
                    colSize *= 2;
                }

                double maxFieldWidth = cellFieldFactor * Math.max(colSize, colNames[col].length());
                if (maxFieldWidth > maxFldWidth) {
                    maxFieldWidth = maxFldWidth;
                }
                tc[col].setPreferredWidth((int) maxFieldWidth);
                //            tc[col].setMaxWidth((int) maxFieldWidth);
                //            tc[col].setMinWidth((int) maxFieldWidth);
            } else {
                // If no member exists in the file the error message must be visible in the window
                tc[col].setPreferredWidth((int) 200);
                listWidth = 1200;
            }
        }

        // Fixed height of the table within the window
        listHeight = 400;

        scrollPaneList = new JScrollPane(jTable);
        scrollPaneList.setMaximumSize(new Dimension(listWidth, listHeight));
        scrollPaneList.setMinimumSize(new Dimension(listWidth, listHeight));
        scrollPaneList.setPreferredSize(new Dimension(listWidth, listHeight));
        scrollPaneList.setBorder(BorderFactory.createLineBorder(Color.WHITE));
    }

    /**
     * Updating a row of the database table by UPDATE statement. The column value
     * is taken from the "rows" array, row "row", column "col". The value is
     * assigned to the single parameter in position 1 (ONE).
     *
     * @param file_member
     * @param row
     * @param col
     */
    @SuppressWarnings("UseSpecificCatch")
    public void updateRow(String file_member, int row, int col) {
        dataMsgPanel.removeAll();
        message.setText("");
        textAreaStmt.setText("");

        final int ONE = 1;
        // Build UPDATE statement from constants and variables
        stmtText = "update " + library + "/" + file_member + "  SET ";
        stmtText += colNames[col] + " = ?";
        // System.out.println("colTypes[col]: " + colTypes[col]);
        stmtText += " where rrn(" + file_member + ") = " + rows[row][0];
        // System.out.println("UPDATE: " + stmtText);
        try ( // Prepare the UPDATE statement.
                PreparedStatement pstmt = conn.prepareStatement(stmtText)) {
            // Check type of the parameter value and if correct
            // assign it to the parameter in the prepared statement.
            // Otherwise an error message is reported and the statement
            // is not performed.
            switch (colTypes[col]) {
                case "DECIMAL":
                    if (rows[row][col].equals(nullMark)) {
                        pstmt.setNull(ONE, java.sql.Types.DECIMAL);
                    } else {
                        pstmt.setBigDecimal(ONE, new BigDecimal(rows[row][col].toString()));
                    }
                    break;
                case "INTEGER":
                    if (rows[row][col].equals(nullMark)) {
                        pstmt.setNull(ONE, java.sql.Types.INTEGER);
                    } else {
                        pstmt.setInt(ONE, new Integer(rows[row][col].toString()));
                    }
                    break;
                case "DATE":
                    if (rows[row][col].equals(nullMark)) {
                        pstmt.setNull(ONE, java.sql.Types.DATE);
                    } else {
                        pstmt.setDate(ONE, Date.valueOf(rows[row][col].toString()));
                    }
                    break;
                case "TIME":
                    if (rows[row][col].equals(nullMark)) {
                        pstmt.setNull(ONE, java.sql.Types.TIME);
                    } else {
                        pstmt.setTime(ONE, Time.valueOf(rows[row][col].toString()));
                    }
                    break;
                case "TIMESTAMP":
                    if (rows[row][col].equals(nullMark)) {
                        pstmt.setNull(ONE, java.sql.Types.TIMESTAMP);
                    } else {
                        pstmt.setTimestamp(ONE, Timestamp.valueOf(rows[row][col].toString()));
                    }
                    break;
                case "BINARY":
                case "VARBINARY":
                    int length;
                    char[] chars;
                    String[] strings;
                    byte[] bytes;
                    if (rows[row][col].equals(nullMark)) // NULL value for the binary column
                    {
                        pstmt.setNull(col, java.sql.Types.BINARY);
                    } else {
                        // The string of hexadecimal characters entered in the cell
                        // will be converted into byte array of half length.
                        length = rows[row][col].toString().length();
                        // /System.out.println("length/2: "+length/2);
                        // /System.out.println("rows[row][col]: "+rows[row][col]);
                        chars = new char[length];
                        strings = new String[length / 2];
                        bytes = new byte[length / 2];
                        rows[row][col].toString().getChars(0, length, chars, 0);
                        // Pairs of hexadecimal characters of the string entered in the
                        // cell
                        // will be translated to bytes.
                        for (int idx = 0; idx < length / 2; idx++) {
                            strings[idx] = String.valueOf(chars[2 * idx])
                                    + String.valueOf(chars[2 * idx + 1]);
                            bytes[idx] = hexToByte(strings[idx]);
                            // System.out.println("strings[idx]: "+strings[idx]);
                            // System.out.println("bytes[idx] hex: "+byteToHex(bytes[idx]));
                        }
                        // Assign the byte array to the UPDATE statement parameter
                        pstmt.setBytes(ONE, bytes);
                    }
                    break;
                default:
                    // Remaining types - (var)char, (var)graphic etc.
                    // are treated as Object.
                    if (rows[row][col].equals(nullMark)) {
                        pstmt.setNull(ONE, java.sql.Types.OTHER);
                    } // JDBC converts Object to the appropriate SQL type
                    else {
                        pstmt.setObject(ONE, rows[row][col].toString());
                    }
            }
            pstmt.execute();
        } // end try
        catch (Exception exc) {
            exc.printStackTrace();
            System.out.println("exc.getClass(): " + exc.getClass());
            String msgText;
            try {
                if (exc.getClass() == Class.forName("java.lang.NumberFormatException")) {
                    msgText = invalidValue + col + " - " + colNames[col] + ".";
                    message.setText(msgText);
                    System.out.println("message a: " + message.getText());
                } else if (exc.getClass() == Class.forName("java.sql.SQLIntegrityConstraintViolationException")) {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message b: " + message.getText());
                } else if (exc.getClass() == Class.forName("java.lang.IllegalArgumentException")) {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message c: " + message.getText());
                } else if (exc.getClass() == Class.forName("com.ibm.as400.access.AS400JDBCSQLSyntaxErrorException")) {
                    message.setText(sqlError + exc.getLocalizedMessage() + ".");
                    System.out.println("message d: " + message.getText());
                } else if (exc.getClass() == Class.forName("java.sql.SQLException")) {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message e: " + message.getText());
                } else {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message f: " + message.getText());
                }

            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            System.out.println("message: " + message.getText());
            message.setForeground(DIM_RED); // red
            // Put message in message panel
            listMsgPanel.add(message);
            // Display the UPDATE statement in statement panel
            textAreaStmt.setAlignmentX(JTextArea.LEFT_ALIGNMENT);
            textAreaStmt.setText(stmtText);
            textAreaStmtPanel.add(textAreaStmt);
            repaint();
            setVisible(true);
        }
    }

    /**
     * Insert a row to the database file by the statement INSERT INTO
     *
     * @param file_member
     * @return
     */
    public boolean insertRow(String file_member) {
        dataMsgPanel.removeAll();
        msg.setText("");

        // Build the INSERT statement
        // numOfCols is counted with RRN. If numOfCols == 1
        // then there are no normal columns.
        // RRN field (index 0) is omitted from INSERT!
        stmtText = "insert into " + library + "/" + file_member + " (";
        System.out.println("numOfCols: " + numOfCols);

        // Normal columns
        stmtText += normalColumnList.substring(normalColumnList.indexOf(", ") + 1);
        stmtText += clobColumnList;
        stmtText += blobColumnList;
        stmtText += ") values(";
        for (int col = 1; col < numOfCols; col++) {
            if (col > 1) {
                stmtText += ", ";
            }
            stmtText += "?";
        }
        for (int col = numOfCols - 1; col < numOfCols - 1 + clobButtons.size(); col++) {
            if (numOfCols > 1) {
                stmtText += ", ?";
            } else {
                if (col > 0) {
                    stmtText += ", ";
                }
                stmtText += "?";
            }
        }
        for (int col = numOfCols - 1; col < numOfCols - 1 + blobButtons.size(); col++) {
            if (numOfCols > 1) {
                stmtText += ", ?";
            } else {
                if (col > 0) {
                    stmtText += ", ";
                }
                stmtText += "?";
            }
        }

        stmtText += ")";
        System.out.println("INSERT: " + stmtText);
        return performPreparedStatement(stmtText);
    }

    /**
     * Update the row of the database file by UPDATE statement. Column values are
     * taken from text fields of the "textFields" array.
     *
     * @param file_member
     * @param row
     * @return
     */
    public boolean updateWholeRow(String file_member, int row) {
        dataMsgPanel.removeAll();
        msg.setText(" ");
        boolean result = false;
        if (numOfCols > 1) {
            stmtText = "update " + library + "/" + file_member + "  SET ";
            // Column 0 - (RRN) is not updated.
            for (int col = 1; col < numOfCols; col++) {
                if (col > 1) {
                    stmtText += ", ";
                }
                stmtText += colNames[col];
                stmtText += " = ?";
            }
            stmtText += " where rrn(" + file_member + ") = " + rows[row][0];
            // System.out.println("UPDATE WHOLE ROW: " + stmtText);
            result = performPreparedStatement(stmtText);
        }
        return result;
    }

    JLabel msg = new JLabel();
    //   JLabel msg2 = new JLabel();

    /**
     * Perform the prepared statement for InsertRow() and UpdateWholeRow()
     * methods
     *
     * @param stmtText
     * @return
     */
    @SuppressWarnings("UseSpecificCatch")
    protected boolean performPreparedStatement(String stmtText) {
        dataMsgPanel.removeAll();
        int col = 0;
        // Check input fields for SQL type conformance
        try {
            pstmt = conn.prepareStatement(stmtText);
            for (col = 1; col < numOfCols; col++) {
                // System.out.println("colTypes[col]: " + colTypes[col]);
                // System.out.println("textFields[col].getText(): " +
                // textFields[col].getText());

                switch (colTypes[col]) {
                    case "DECIMAL":
                        if (textFields[col].getText().equals(nullMark)) {
                            pstmt.setNull(col, java.sql.Types.DECIMAL);
                        } else {
                            pstmt.setBigDecimal(col, new BigDecimal(textFields[col].getText()));
                        }
                        break;
                    case "INTEGER":
                        if (textFields[col].getText().equals(nullMark)) {
                            pstmt.setNull(col, java.sql.Types.INTEGER);
                        } else {
                            pstmt.setInt(col, new Integer(textFields[col].getText()));
                        }
                        break;
                    case "DATE":
                        if (textFields[col].getText().equals(nullMark)) {
                            pstmt.setNull(col, java.sql.Types.DATE);
                        } else {
                            pstmt.setDate(col, Date.valueOf(textFields[col].getText()));
                        }
                        break;
                    case "TIME":
                        if (textFields[col].getText().equals(nullMark)) {
                            pstmt.setNull(col, java.sql.Types.TIME);
                        } else {
                            pstmt.setTime(col, Time.valueOf(textFields[col].getText()));
                        }
                        break;
                    case "TIMESTAMP":
                        if (textFields[col].getText().equals(nullMark)) {
                            pstmt.setNull(col, java.sql.Types.TIMESTAMP);
                        } else {
                            pstmt.setTimestamp(col, Timestamp.valueOf(textFields[col].getText()));
                        }
                        break;
                    case "BINARY":
                    case "VARBINARY":
                        // Binary values are represented by hexadecimal characters.
                        // Field length is therefore twice as long
                        int length;
                        char[] chars;
                        String[] strings;
                        byte[] bytes;
                        if (textFields[col].getText().equals(nullMark)) {
                            pstmt.setNull(col, java.sql.Types.BINARY);
                        } else {
                            // Field length for BINARY and VARBINARY is obtained the same
                            // way.
                            length = textFields[col].getText().length();
                            chars = new char[length];
                            strings = new String[length / 2];
                            bytes = new byte[length / 2];
                            textFields[col].getText().getChars(0, length, chars, 0);
                            // Pairs of hexa characters are transformed into single bytes
                            for (int idx = 0; idx < length / 2; idx++) {
                                // Two hexa characters are transformed into one byte binary
                                // value
                                strings[idx] = String.valueOf(chars[2 * idx])
                                        + String.valueOf(chars[2 * idx + 1]);
                                bytes[idx] = hexToByte(strings[idx]);
                            }
                            pstmt.setBytes(col, bytes);
                        }
                        break;
                    // Remaining types - (var)char, (var)graphic etc.
                    default:
                        if (textFields[col].getText().equals(nullMark)) {
                            pstmt.setNull(col, java.sql.Types.OTHER);
                        } // JDBC converts Object to the appropriate SQL type
                        else {
                            pstmt.setObject(col, textFields[col].getText());
                        }
                }
            }

            // For INSERT CLOB the user supplies a text file for the column
            if (addNewRecord) {
                int descriptorIdx;
                // Reader reader = null;
                for (col = 1; col < clobButtons.size() + 1; col++) {
                    if (numOfCols == 1) // If no normal columns are visible, the CLOB columns are
                    // numbered
                    {
                        descriptorIdx = col;
                    } else // If some normal columns are visible, normal and CLOB columns
                    // are numbered
                    {
                        descriptorIdx = numOfCols - 1 + col;
                    }

                    /*
                * // Invoke file chooser dialog to choose a file for the CLOB //
                * column U_GetFileReader getFileReader = new U_GetFileReader();
                * // The dialog delivers a Reader which contains data of the
                * file reader = getFileReader.getFileReader();
                * System.out.println("readerInsert: " + reader); // If the
                * reader is null (the user canceled the dialog) if (reader ==
                * null) // Set null to the column pstmt.setString(descriptorIdx,
                * null); // If the reader is non-null (user chose a file) else
                * // Set text from the reader to the column
                * pstmt.setClob(descriptorIdx, reader);
                     */
                    pstmt.setString(descriptorIdx, null);

                }
            }

            // For INSERT BLOB the user supplies a binary file for the column
            if (addNewRecord) {
                int descriptorIdx;
                // byte[] bytes = null;
                for (col = 1; col < blobButtons.size() + 1; col++) {
                    if (numOfCols == 1) // If no normal columns are visible, the BLOB columns are
                    // numbered
                    {
                        descriptorIdx = col + blobButtons.size();
                    } else // If some normal columns are visible, normal and BLOB columns
                    // are numbered
                    {
                        descriptorIdx = numOfCols - 1 + blobButtons.size() + col;
                    }

                    /*
                * // Invoke file chooser dialog to choose a file for the BLOB //
                * column U_GetFile getFile = new U_GetFile(); // The dialog
                * delivers a Reader which contains data of the file file =
                * getFile.getFile(file); // If the file is null (the user
                * canceled the dialog) if (file == null) // Set null to the
                * column pstmt.setBytes(descriptorIdx, null); // If the file is
                * non-null (user chose a file) else // Get byte array from the
                * file and set it to the BLOB bytes =
                * Files.readAllBytes(file.toPath());
                * pstmt.setBytes(descriptorIdx, bytes);
                     */
                    pstmt.setBytes(descriptorIdx, null);
                }
            }

            // Perform the statement (UPDATE or INSERT)
            pstmt.execute();
            pstmt.close();
            return true;
        } // end try
        catch (Exception exc) {
            exc.printStackTrace();
            System.out.println("exc.getClass(): " + exc.getClass());
            String msgText;
            try {
                if (exc.getClass() == Class.forName("java.lang.NumberFormatException")) {
                    msgText = invalidValue + col + " - " + colNames[col] + ".";
                    message.setText(msgText);
                    System.out.println("message a: " + message.getText());
                } else if (exc.getClass() == Class.forName("java.sql.SQLIntegrityConstraintViolationException")) {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message b: " + message.getText());
                } else if (exc.getClass() == Class.forName("java.lang.IllegalArgumentException")) {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message c: " + message.getText());
                } else if (exc.getClass() == Class.forName("com.ibm.as400.access.AS400JDBCSQLSyntaxErrorException")) {
                    message.setText(sqlError + exc.getLocalizedMessage());
                    System.out.println("message d: " + message.getText());
                } else if (exc.getClass() == Class.forName("java.sql.SQLException")) {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message e: " + message.getText());
                } else {
                    msgText = sqlError + exc.getLocalizedMessage() + ". ";
                    message.setText(msgText);
                    System.out.println("message f: " + message.getText());
                }
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
            System.out.println("message: " + message.getText());
            message.setForeground(DIM_RED); // red
            // Put message in data message panel
            dataMsgPanel.add(message);
            repaint();
            setVisible(true);
            return false;
        }
    }

    /**
     * Delete the row by DELETE statement using the relative record number
     *
     * @param file_member
     * @param rrn
     */
    protected void deleteRow(String file_member, BigDecimal rrn) {
        stmtText = "delete from " + file_member + " where rrn(" + file_member + ") = ";
        stmtText += rrn;
        System.out.println("stmtText deleteRow: " + stmtText);

        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(stmtText);
            stmt.close();
        } // end try
        catch (SQLException sqle) {
            // System.out.println("DELETE: " + stmtText);
            // System.out.println("deleteRow: " + sqle.getLocalizedMessage());
            sqle.printStackTrace();
            message.setText(sqle.getLocalizedMessage());
            message.setForeground(DIM_RED); // red
            // Put message in message panel
            listMsgPanel.add(message);
            // Put DELETE statement in statement panel
            textAreaStmt.setText(stmtText);
            textAreaStmtPanel.add(scrollPaneStmt);
            repaint();
            //            setVisible(true);
        }
    }

    /**
     * Update CLOB
     */
    @SuppressWarnings("UseSpecificCatch")
    protected void updateClob(int col) {
        colLength = 0;
        String clobType = "";
        // Get column capacity for column name just processed
        for (int in = 0; in < allColNames.size(); in++) {
            if (allColNames.get(in).contains(colName)) {
                System.out.println("ColSize: " + allColSizes.get(in));
                colCapacity = allColSizes.get(in);
                clobType = clobTypes[in];
            }
        }
        try {
            // UPDATE the CLOB column
            // ----------------------
            // Build SELECT statement for current CLOB column
            // (by colName)
            stmtText = "select " + colName + "\n from " + library.toUpperCase() + "/"
                    + file_member.toUpperCase();
            stmtText += " where rrn(" + file_member + ") = " + rows[rowIndex][0];
            ResultSet rs;

            // Statement is scrollable, updatable
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            System.out.println("SELECT: \n" + stmtText);

            // Execute the SELECT statement and obtain the ResulSet rs.
            rs = stmt.executeQuery(stmtText);
            rs.next();

            if (clobType.equals("CLOB")) {
                clob = (com.ibm.as400.access.AS400JDBCClob) rs.getClob(colName);
            } else if (clobType.equals("NCLOB")) {
                clob = (com.ibm.as400.access.AS400JDBCClobLocator) rs.getClob(colName);
            }
            // Build UPDATE statement for current CLOB column (by colName)
            stmtText = "update " + library + "/" + file_member + "  SET " + colName;
            stmtText += " = ? where rrn(" + file_member + ") = " + rows[rowIndex][0];
            //System.out.println("UPDATE: \n" + stmtText);

            // Prepare the UPDATE statement for the column
            pstmt = conn.prepareStatement(stmtText);

            // If clob reference is not null
            if (clob != null) {
                colLength = clob.length();

                // Update CLOB value between start position and length
                U_ClobUpdate clobUpdate = new U_ClobUpdate(this);
                clobUpdate.createWindow(colName, clob, colStartPos, colLength);

                // Get results of changed text area
                retClobValues = clobUpdate.getReturnedValues();
                // CLOB object returned
                clob = (com.ibm.as400.access.AS400JDBCClob) retClobValues.getClob();
                // Start position returned
                colStartPos = retClobValues.getStartPos();
                // Length returned
                colLength = retClobValues.getLength();
                // Message returned
                msg = retClobValues.getMsg();

                if (clob != null) {
                    msg = retClobValues.getMsg();
                    msg.setForeground(DIM_BLUE); // Dim blue
                } else {
                    colLength = 0;
                    msg.setText(colValueNull);
                    msg.setForeground(DIM_BLUE); // Dim blue
                }
                // Set the only prepared parameter in UPDATE statement
                // by the CLOB column value
                pstmt.setClob(1, clob);
            } // If the CLOB reference is null
            else {
                // Invoke file chooser dialog to choose a file for the CLOB column
                U_GetFile getFile = new U_GetFile();
                file = getFile.getFile(file);
                // If the user chose a file
                if (file != null) {
                    colLength = Files.size(file.toPath());
                    try {
                        reader = Files.newBufferedReader(file.toPath(), Charset.forName(charCode));
                    } catch (UnsupportedCharsetException ucse) {
                        msg.setForeground(DIM_RED); // Dim red
                        msg.setText(invalidCharset + " " + charCode + ".");
                        System.out.println("DataTable UnsupportedCharsetException: " + msg.getText());
                        ucse.printStackTrace();
                        dataMsgPanel.add(msg);
                        pack();
                        repaint();
                        return;
                    }
                    if (reader != null) {
                        // If the reader is non-null (user chose a file)
                        // Set text from the reader to the column
                        pstmt.setClob(1, reader);
                        msg.setText(contentLoaded + " " + colLength);
                        msg.setForeground(DIM_BLUE); // Dim blue
                        // System.out.println("DataTable file not null Open: " + msg.getText());
                    } else {
                        // If the reader is null (the user canceled the dialog)
                        colLength = 0;
                        // Set null to the column
                        pstmt.setNull(1, java.sql.Types.CHAR);
                        msg.setText(colValueNull);
                        msg.setForeground(DIM_BLUE); // Dim blue
                        // System.out.println("DataTable file not null Cancel 1: " + msg.getText());
                    }
                } else {
                    // If the reader is null (the user canceled the dialog)
                    colLength = 0;
                    // Set null to the column
                    pstmt.setNull(1, java.sql.Types.CHAR);
                    msg.setText(colValueNull);
                    msg.setForeground(DIM_BLUE); // Dim blue
                    // System.out.println("DataTable file null Cancel 2: " + msg.getText());
                }
            }
            // Perform the UPDATE statement for the column
            pstmt.executeUpdate();
            pstmt.close();
            stmt.close();
            dataMsgPanel.add(msg);
            pack();
            repaint();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("DataTable IOException: " + colLength);
            if (colLength > colCapacity) {
                msg.setForeground(DIM_RED); // Dim red
                msg.setText(length + colLength + tooLongForCol + colName + "." + contentNotLoaded + ", " + ioe.getLocalizedMessage());
                System.out.println("DataTable IOException too long: " + msg.getText());
            } else if (colLength == 0) {
                msg.setForeground(DIM_BLUE); // Dim blue
                msg.setText(colValueNull + ", " + ioe.getLocalizedMessage());
                System.out.println("DataTable IOException col value null: " + msg.getText());
            } else if (colLength < 0) {
                msg.setForeground(DIM_RED); // Dim red
                msg.setText(colNotText + ", " + ioe.getLocalizedMessage());
                System.out.println("DataTable IOException col value not text: " + msg.getText());
            }
        } catch (SQLException sqle) {
            msg.setForeground(DIM_RED); // Dim red
            msg.setText(invalidValue + colName + "." + ", " + sqle.getLocalizedMessage());
            System.out.println("DataTable SQLException: " + msg.getText());
            sqle.printStackTrace();
        }

        dataMsgPanel.add(msg);
        pack();
        repaint();
        //    setVisible(true);
    }

    /**
     * Update BLOB
     */
    @SuppressWarnings("UseSpecificCatch")
    protected void updateBlob() {
        msg.setText(" ");
        try {
            // UPDATE the BLOB column
            // ----------------------
            // Build SELECT statement for current BLOB column
            // (by colName)
            stmtText = "select " + colName + "\n from " + library.toUpperCase() + "/"
                    + file_member.toUpperCase();
            stmtText += " where rrn(" + file_member + ") = " + rows[rowIndex][0];
            System.out.println("SELECT: \n" + stmtText);

            // Statement is scrollable, updatable
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            // Execute the SELECT statement and obtain the ResulSet rs.
            ResultSet rs = stmt.executeQuery(stmtText);
            // Read the only row
            rs.next();
            // Get the contents of the BLOB column
            blob = rs.getBlob(colName);

            // Build UPDATE statement for current CLOB column
            // (by colName)
            stmtText = "update " + library + "/" + file_member + "  set " + colName;
            stmtText += " = ? where rrn(" + file_member + ") = " + rows[rowIndex][0];
            System.out.println("UPDATE: \n" + stmtText);

            // If blob reference is not null - content window is displayed
            if (blob != null) {
                blobLength = blob.length();

                // Call class with the window to update the BLOB value 
                // --------------------------
                U_BlobUpdate blobUpdate = new U_BlobUpdate(this);
                blobUpdate.createWindow(colName, blob, blobLength);
                U_BlobReturnedValues retBlobValues = blobUpdate.getReturnedValues();

                // Get returned values
                blobReturned = retBlobValues.getBlob();
                blobLength = retBlobValues.getLength();
                msg = retBlobValues.getMsg();
                msg.setForeground(DIM_BLUE); // Dim blue
                if (blobReturned != null) {
                    blobLength = blobReturned.length();
                } else {
                    blobLength = 0;
                    msg.setText(colValueNull);
                }

                // Update the column only if a file was loaded and put into the BLOB.
                // The following special message was sent from U_BlobUpdate if not.
                if (!msg.getText().equals("BLOB was not changed")
                        && !msg.getText().equals("BLOB nebyl změněn")) {
                    // Prepare the UPDATE statement for the column
                    pstmt = conn.prepareStatement(stmtText);
                    // Set the only prepared parameter in UPDATE statement
                    // by the BLOB column value
                    pstmt.setBlob(1, blobReturned);
                    // Perform the UPDATE statement for the column
                    pstmt.executeUpdate();
                }
            } else {
                // If blob reference is NULL - no content window is displayed.
                // Only the file chooser is called to get another file for the column
                // -------------------------------       

                // Prepare the UPDATE statement for the column
                pstmt = conn.prepareStatement(stmtText);

                U_GetFile getFile = new U_GetFile();
                file = getFile.getFile(file);
                if (file != null) {
                    blobLength = Files.size(file.toPath());
                    // Invoke file chooser dialog to choose a file for the
                    // BLOB column
                    stream = Files.newInputStream(file.toPath());
                    // If the stream is null (the user canceled the dialog)
                    if (stream == null) {
                        // Set null to the column
                        pstmt.setNull(1, java.sql.Types.BLOB);
                        // Perform the UPDATE statement for the column
                        // inserting the NULL value
                        pstmt.executeUpdate();
                        msg.setText(colValueNull);
                    } // If the stream is non-null (user chose a file)
                    else {
                        // Set content from the stream to the column
                        pstmt.setBlob(1, stream);
                        // Perform the UPDATE statement for the column
                        pstmt.executeUpdate();
                        msg.setText(contentLoaded + blobLength);
                    }
                } else {
                    blobLength = 0;
                    pstmt.setNull(1, java.sql.Types.BLOB);
                    pstmt.executeUpdate();
                    msg.setText(colValueNull);
                }
            }
            stmt.close();
            msg.setForeground(DIM_BLUE); // Dim blue
            dataMsgPanel.add(msg);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (blobLength > 0) {
                    msg.setForeground(DIM_RED); // Dim red
                    msg.setText(length + blobLength + tooLongForCol + colName + "." + contentNotLoaded);
                } else {
                    // If blob length is <= 0
                    msg.setText(e.getLocalizedMessage());
                    msg.setForeground(DIM_RED); // Dim red
                }
                dataMsgPanel.add(msg);
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }
        pack();
        repaint();
        setVisible(true);

    }

    /**
     * Data model provides methods to fill data from the source to table cells
     * for display. It is applied every time when any change in data source
     * occurs.
     */
    class TableModel extends AbstractTableModel {

        static final long serialVersionUID = 1L;

        // Returns number of columns
        @Override
        public int getColumnCount() {
            return numOfCols;
        }

        // Returns number of rows
        @Override
        public int getRowCount() {
            return numOfRows;
        }

        // Sets number of rows
        public void setRowCount(int rowCount) {
            numOfRows = rowCount;
        }

        // Returns column name
        @Override
        public String getColumnName(int col) {
            return colNames[col];
        }

        // Data transfer from the source to a cell for display. It is applied
        // automatically at any change of data source but also when ENTER or TAB
        // key
        // is pressed or when clicked by a mouse. Double click or pressing a data
        // key
        // invokes the cell editor method - getTableCellEditorComponent().
        // The method is called at least as many times as is the number of cells
        // in the table.
        @Override
        public Object getValueAt(int row, int col) {
            // System.out.println("getValueAt: (" + row + "," + col + "): " +
            // rows[row][col]);
            // Return the value for display in the table
            if (rows[row][col] == null) {
                return nullMark; // Sloupec s hodnotou NULL
            } else {
                return rows[row][col].toString(); // Ostatní sloupce
            }
        }

        // Write input data from the cell back to the data source for
        // display in the table. A change in the data source invokes method
        // getValueAt().
        // The method is called after the cell editor ends its activity.
        @Override
        public void setValueAt(Object obj, int row, int col) {
            // Assign the value from the cell to the data source.
            if (obj == null) {
                rows[row][col] = null;
            }
            rows[row][col] = obj;
            // Update also the corresponding row of the database file
            updateRow(file_member, row, col);
            // System.out.println("setValueAt: (" + row + "," + col + "): " +
            // rows[row][col]);
        }

        // Get class of the column value - it is important for the cell editor
        // could be invoked and could determine e.g. the way of aligning of the
        // text in the cell.
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public Class getColumnClass(int col) {
            return getValueAt(0, col).getClass();
        }

        // Determine whicn cells are editable or not
        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return false; // column 0 - RRN - cannot be changed
            } else {
                return true;
            }
        }
    }

    /**
     * Cell editor as an extension of the class DefaultCellEditor. It is applied
     * before keyboard data input and after that.
     */
    class CellEditor extends DefaultCellEditor {

        static final long serialVersionUID = 1L;
        JTextField tf; // copy of the component for the editor
        int row;
        int col;
        String sOrig; // original text of the cell

        public CellEditor() {
            super(new JTextField());
            // Get a copy of the component from the cell for the editor
            this.tf = (JTextField) super.getComponent();
        }

        // Delivering a copy of the component for editing on the screen.
        // This method is applied before entering data from the keyboard.
        // The editor has its own copy of the component available.
        // Here the input text is saved into the component copy and
        // the copy is returned.
        @Override
        public Component getTableCellEditorComponent(JTable table, Object obj, boolean isSelected, int row, int col) {
            // Invoke the same method of the superclass DefaultCellEditor
            tf = (JTextField) super.getTableCellEditorComponent(table, obj, isSelected, row, col);
            // Set font for the component copy
            tf.setFont(new Font("Monospaced", Font.PLAIN, fontSize));
            // Copy the cell content to data source
            this.row = row;
            this.col = col;
            // Save the original text (correct number) to return it eventually
            sOrig = tf.getText();
            // Save the unchanged entered text to the component copy
            tf.setText(obj.toString());
            // System.out.print("getTableCellEditorComponent: (");
            // System.out.println(row + "," + col + "): " + tf.getText());
            // Return the "pre-edited" component copy for editing on the screen
            return tf;
        }

        // Delivering the component value after editing into the cell for display.
        // This method is applied after ending data input from the keyboard,
        // i.e. after leaving the cell by ENTER or TAB keys, or mouse click.
        @Override
        @SuppressWarnings("ConvertToStringSwitch")
        public Object getCellEditorValue() {
            // Get component copy from the edited cell
            tf = (JTextField) super.getComponent();
            Object obj;
            if (tf.getText().equals(nullMark)) {
                return nullMark; // return NULL mark for the cell
            }
            // Get cell value and determine its SQL type,
            // create an object of that type
            if (colTypes[col].equals("DECIMAL")) {
                obj = new java.math.BigDecimal(tf.getText());
            } else if (colTypes[col].equals("INTEGER")) {
                obj = new Integer(tf.getText());
            } else if (colTypes[col].equals("DATE")) {
                obj = Date.valueOf(tf.getText());
            } else if (colTypes[col].equals("TIME")) {
                obj = Time.valueOf(tf.getText());
            } else if (colTypes[col].equals("TIMESTAMP")) {
                obj = Timestamp.valueOf(tf.getText());
            } else // other types are treated as String
            {
                obj = (String) tf.getText();
            }
            // and return the object value for display
            return obj;
        }

        // Check data after leaving entering data from the keyboard to the cell.
        // The input is stopped or is continued.
        @Override
        public boolean stopCellEditing() {
            tf = (JTextField) super.getComponent();
            message.setForeground(Color.BLACK);
            tf.setForeground(Color.BLACK);
            try {
                if (tf.getText().equals(nullMark)) {
                } else {
                    // Try to create a value of the SQL type of the current column
                    // as a check of correctness.
                    if (colTypes[col].equals("DECIMAL")) {
                        new java.math.BigDecimal(tf.getText());
                    }
                    if (colTypes[col].equals("INTEGER")) {
                        Integer.parseInt(tf.getText());
                    }
                    if (colTypes[col].equals("DATE")) {
                        Date.valueOf(tf.getText());
                    }
                    if (colTypes[col].equals("TIME")) {
                        Time.valueOf(tf.getText());
                    }
                    if (colTypes[col].equals("TIMESTAMP")) {
                        Timestamp.valueOf(tf.getText());
                    }
                    // Check the value length against the defined length of the
                    // column
                    if (colTypes[col].equals("BINARY") || colTypes[col].equals("VARBINARY")) {
                        // The value in binary column may be twice as long because it
                        // is entered
                        // as pairs of hexadecimal characters.
                        if (tf.getText().length() > colSizes[col] * 2) {
                            message.setText(value + tooLong);
                            message.setForeground(DIM_RED); // Dim red
                            tf.setForeground(DIM_RED); // Dim red
                            repaint();
                            setVisible(true);
                            return false;
                        }
                    } else // Ordinary field
                    if (tf.getText().length() > colSizes[col]) {
                        message.setText(value + tooLong);
                        message.setForeground(DIM_RED); // Dim red
                        tf.setForeground(DIM_RED); // Dim red
                        repaint();
                        setVisible(true);
                        return false;
                    }
                }
                // Write the text into data source ("rows" array)
                rows[row][col] = tf.getText();
                // Try to update the column in the row of the database table
                updateRow(file_member, row, col);
                if (!message.getText().isEmpty()) {
                    // Any error is reported in the message line
                    message.setForeground(DIM_RED); // Dim red
                    tf.setForeground(DIM_RED); // Dim red
                    repaint();
                    setVisible(true);
                    return false;
                }
                // If no error - end entry and return true
                return super.stopCellEditing();
            } catch (Exception e) {
                e.printStackTrace();
                message.setText(e.getLocalizedMessage());
                message.setForeground(DIM_RED); // Dim red
                tf.setForeground(DIM_RED); // Dim red
                // Set invalid value to the cell and try to update the row in
                // database
                rows[row][col] = tf.getText();
                updateRow(file_member, row, col);
                return false;
            }
        }
    }

    /**
     * Determine background and foreground color in the column
     */
    class ColorColumnRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        Color backgroundColor, foregroundColor;

        public ColorColumnRenderer(Color backgroundColor, Color foregroundColor) {
            super();
            this.backgroundColor = backgroundColor;
            this.foregroundColor = foregroundColor;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            cell.setBackground(backgroundColor);
            cell.setForeground(foregroundColor);
            return cell;
        }
    }

    /**
     * Determint alignment of data in the column according to the data type.
     * BigDecimal and Integer are aligned to the right, the others are aligned
     * left.
     */
    class AdjustColumnRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 1L;
        String cls;

        public AdjustColumnRenderer(String cls) {
            this.cls = cls;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (cls.equals("java.math.BigDecimal") || cls.equals("java.lang.Integer")) {
                renderedLabel.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
            }
            return renderedLabel;
        }
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

    /**
     * Save data of the inserted or updated row
     *
     * @return boolean true (if no error)
     */
    protected boolean saveData() {
        message.setText("");
        boolean OK = true;
        if (addNewRecord) {
            // Insert row
            if (checkInputFields()) {
                OK = false;
            } else if (!insertRow(file_member)) {
                OK = false;
            }
        } else // Update row
        {
            if (checkInputFields()) {
                OK = false;
            } else if (!updateWholeRow(file_member, rowIndex)) {
                OK = false;
            }
        }
        dataMsgPanel.add(msg);
        setVisible(true);
        return OK;
    }

    String valueInMsg = "";

    /**
     * Checks input fields - whether their values are longer than field lengths.
     * As soon as the first of the checked fields is invalid, returns with
     * "true".
     *
     * @return true if error
     */
    protected boolean checkInputFields() {
        message.setForeground(Color.BLACK);
        // Set black color of field values as if all are correct
        for (int i = 0; i < numOfCols; i++) {
            textFields[i].setForeground(Color.BLACK);
        }
        // Check all field values for length (not greater than prescribed)
        boolean error = false;
        for (int idx = 0; idx < numOfCols; idx++) {
            int colSize = colSizes[idx];
            if (colTypes[idx].equals("BINARY") || colTypes[idx].equals("VARBINARY")) {
                colSize *= 2;
            }
            if (textFields[idx].getText().length() > colSize) {
                valueInMsg = textFields[idx].getText();
                // If field value is greater - set red color
                textFields[idx].setForeground(DIM_RED); // Dim red
                error = true;
                break;
            }
        }
        // If the field value is longer than 50 characters shorten the value
        // and append ellipsis (three dots).
        if (valueInMsg.length() > 50) {
            valueInMsg = valueInMsg.substring(0, 50) + "...";
        }
        if (error == true) {
            // In case of error - send a message to the window.
            System.out.println("valueInMsg: " + valueInMsg);
            msg.setForeground(DIM_RED); // Dim red
            msg.setText(value + valueInMsg + tooLong);
        }
        return error;
    }

    /**
     * Clears data fields
     */
    protected void clearDataFields() {
        for (int idx = 0; idx < numOfCols; idx++) {
            textFields[idx].setText("");
        }
    }

    /**
     * Translate single byte to hexadecimal character
     *
     * @param singleByte
     * @return
     */
    static String byteToHex(byte singleByte) {
        int bin = (singleByte < 0) ? (256 + singleByte) : singleByte;
        int bin0 = bin >>> 4; // higher half-byte
        int bin1 = bin % 16; // lowe half-byte
        String hex = Integer.toHexString(bin0) + Integer.toHexString(bin1);
        return hex;
    }

    /**
     * Translate a string of two hexadecimal characters to a single byte
     *
     * @param hexChar
     * @return
     */
    static byte hexToByte(String hexChar) {
        // Translation tables
        String args = "0123456789abcdef";
        int[] funcs = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
        // Two characters from the String traslated in lower case
        char charHigh = hexChar.toLowerCase().charAt(0);
        char charLow = hexChar.toLowerCase().charAt(1);
        int highHalf = 0, lowHalf = 0;
        // Find character in argument table
        if (args.indexOf(charHigh) > -1) // If found get corresponding function (int value)
        // If not found - result is 0
        {
            highHalf = funcs[args.indexOf(charHigh)];
        }
        if (args.indexOf(charLow) > -1) {
            lowHalf = funcs[args.indexOf(charLow)];
        }
        // Assemble high and low half-bytes in single byte
        int singleByte = (highHalf << 4) + lowHalf;
        return (byte) singleByte;
    }
}
