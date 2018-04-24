package update;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

/**
 * Select table columns to a (reduced) resulting column list and save it to the
 * .col file
 *
 * @author Vladimír Župka 2016
 *
 */
public class U_ColumnsJList extends JDialog {

    protected static final long serialVersionUID = 1L;

    // Application parameters
    Properties properties;
    BufferedReader infile;
    Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
    String encoding = System.getProperty("file.encoding");

    String language;

    // Localized text objects
    Locale locale;
    ResourceBundle titles;
    ResourceBundle buttons;
    String titleCol, prompt1Col, prompt2Col, prompt3Col;
    String copyCol, deleteCol, clearAll, saveExit;

    // Empty left vector for list of all columns
    Vector<String> vectorLeft = new Vector<>();

    // Left ist containing the vector
    JList<String> listLeft = new JList<>(vectorLeft);

    // Right list has a list model
    JList<String> listRight;
    // List model for right list
    DefaultListModel<String> listRightModel = new DefaultListModel<>();
    // Drag and drop Transfer handler for right list
    ListLeftRightTransfHdlr listLeftRightTransfHdlr = new ListLeftRightTransfHdlr();

    JLabel message = new JLabel("");

    JScrollPane scrollPaneLeft = new JScrollPane(listLeft);
    JScrollPane scrollPaneRight;

    int scrollPaneWidth = 170;
    int scrollPaneHeight = 120;

    JPanel leftPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    JPanel globalPanel = new JPanel();

    GroupLayout layout = new GroupLayout(globalPanel);

    private String returnedColumnList = ""; // Empty column list to return

    // Path to the file containing column list for SELECT statement
    Path columnsPath;

    final Color DIM_BLUE = new Color(50, 60, 160);
    final Color DIM_RED = new Color(190, 60, 50);
    Color VERY_LIGHT_BLUE = Color.getHSBColor(0.60f, 0.05f, 0.98f);

    /**
     * Constructor
     *
     * @param fullColumnList
     * @param selectedFileName
     */
    public U_ColumnsJList(String fullColumnList, String selectedFileName) {
        this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        // Application properties
        properties = new Properties();
        try {
            BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
            properties.load(infile);
            infile.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        language = properties.getProperty("LANGUAGE"); // local language

        // Localization classes
        Locale currentLocale = Locale.forLanguageTag(language);
        titles = ResourceBundle.getBundle("locales.L_TitleLabelBundle", currentLocale);
        buttons = ResourceBundle.getBundle("locales.L_ButtonBundle", currentLocale);

        // Localized titles
        titleCol = titles.getString("TitleCol");
        prompt1Col = titles.getString("Prompt1Col");
        prompt2Col = titles.getString("Prompt2Col");
        prompt3Col = titles.getString("Prompt3Col");
        JLabel title = new JLabel(titleCol);
        JLabel prompt1 = new JLabel(prompt1Col);
        JLabel prompt2 = new JLabel(prompt2Col);
        JLabel prompt3 = new JLabel(prompt3Col);

        // Localized button labels
        copyCol = buttons.getString("CopyCol");
        deleteCol = buttons.getString("DeleteCol");
        clearAll = buttons.getString("ClearAll");
        saveExit = buttons.getString("SaveExit");
        JButton copyButton = new JButton(copyCol);
        JButton deleteButton = new JButton(deleteCol);
        JButton clearButton = new JButton(clearAll);
        JButton exitButton = new JButton(saveExit);

        // Fill the Left list with the full column list from the parameter
        String[] cols = fullColumnList.split(",");
        for (String col : cols) {
            vectorLeft.addElement(col.trim());
        }
        listLeft.setListData(vectorLeft);
        // Set the left list as enabled for dragging from.
        listLeft.setDragEnabled(true);

        // Fill the Right list (resulting column selection) initially with the values from the .col file
        columnsPath = Paths.get(System.getProperty("user.dir"), "columnfiles", selectedFileName + ".col");
        try {
            List<String> items = Files.readAllLines(columnsPath);
            items.get(0);
            cols = items.get(0).split(",");
            for (int idx = 1; idx < cols.length; idx++) {
                listRightModel.addElement(cols[idx].trim());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Start window construction
        Font titleFont = new Font("Helvetica", Font.PLAIN, 20);
        title.setFont(titleFont);
        prompt1.setForeground(DIM_BLUE); // Dim blue
        prompt2.setForeground(DIM_BLUE); // Dim blue
        prompt3.setForeground(DIM_BLUE); // Dim blue

        message.setText("");

        // Create right list (user library list) using the DefaultListModel 
        listRight = new JList(listRightModel);
        listRight.setDragEnabled(true);
        listRight.setDropMode(DropMode.INSERT);
        listRight.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        scrollPaneLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollPaneLeft.setMaximumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneLeft.setMinimumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneLeft.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneLeft.setBackground(scrollPaneLeft.getBackground());
        scrollPaneLeft.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        scrollPaneRight = new JScrollPane(listRight);
        scrollPaneRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //scrollPaneRight.setMaximumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneRight.setMinimumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneRight.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        scrollPaneRight.setBackground(scrollPaneLeft.getBackground());
        scrollPaneRight.setBorder(BorderFactory.createEmptyBorder());

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(title)
                .addComponent(prompt2)
                .addComponent(prompt1)
                .addComponent(prompt3)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollPaneLeft)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(deleteButton)
                                .addComponent(copyButton)
                                .addComponent(clearButton)
                                .addComponent(exitButton))
                        .addComponent(scrollPaneRight))
                .addComponent(message));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(title)
                .addComponent(prompt2)
                .addComponent(prompt1)
                .addComponent(prompt3)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(scrollPaneLeft)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(deleteButton)
                                .addComponent(copyButton)
                                .addComponent(clearButton)
                                .addComponent(exitButton))
                        .addComponent(scrollPaneRight))
                .addComponent(message));

        listRight.setTransferHandler(listLeftRightTransfHdlr);

        // Set Copy button activity
        // ------------------------
        copyButton.addActionListener(a -> {
            copyLeftToRight(null);
        });

        // Delete button listener (remove selected)
        // ----------------------
        deleteButton.addActionListener(a -> {
            List<String> itemsRight = listRight.getSelectedValuesList();
            if (!itemsRight.isEmpty() && !listRightModel.isEmpty()) {
                for (Object item : itemsRight) {
                    listRightModel.removeElement(item);
                }
            }
        });

        // Set Clear button listener (remove all items)
        // -------------------------
        clearButton.addActionListener(a -> {
            listRightModel.removeAllElements();
        });

        // Set Save + Exit button activity
        // -------------------------------
        exitButton.addActionListener(a -> {
            returnedColumnList = "";
            // Build column list as a string (comma separated column names)
            for (int idx = 0; idx < listRightModel.getSize(); idx++) {
                String str = listRightModel.getElementAt(idx);
                returnedColumnList += ", ";
                returnedColumnList += str;
            }

            // Write resulting column list to the file
            columnsPath = Paths.get(System.getProperty("user.dir"), "columnfiles", selectedFileName + ".col");
            try {
                ArrayList<String> colArr = new ArrayList<>();
                colArr.add(returnedColumnList);
                // Rewrite the existing file or create and write a new file.
                Files.write(columnsPath, colArr, StandardCharsets.UTF_8);
            } catch (IOException ioe) {
                System.out.println("write columns file: " + ioe.getLocalizedMessage());
                ioe.printStackTrace();
            }
            dispose();
        });

        // Complete window construction
        globalPanel.setLayout(layout);
        globalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Container cont = getContentPane();
        cont.add(globalPanel);

        // Make window visible 
        setSize(520, 370);
        setLocation(300, 320);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * @param index
     */
    protected void copyLeftToRight(Integer index) {
        // Copy selected items from the left box to the right box
        List<String> itemsLeft = listLeft.getSelectedValuesList();
        if (!listRightModel.isEmpty()) {
            // Add selected items from the left list after non-empty right list 
            int lastRightIndex = listRightModel.size() - 1;
            for (int idx = itemsLeft.size() - 1; idx >= 0; idx--) {
                boolean foundInRight = false;
                // Find out if the left item matches any right item
                for (int jdx = 0; jdx < lastRightIndex + 1; jdx++) {
                    if (itemsLeft.get(idx).equals(listRightModel.get(jdx))) {
                        foundInRight = true;
                    }
                }
                // If the left item does not match any item in the right box vector
                // add the item at the end of the vector items in the right box.
                if (!foundInRight) {
                    if (index == null) {
                        listRightModel.addElement(itemsLeft.get(idx));
                    } else {
                        listRightModel.insertElementAt(itemsLeft.get(idx), index);
                    }
                }
            }
        } else {
            // Add selected items from the whole left list in the empty right list
            for (int idx = 0; idx < itemsLeft.size(); idx++) {
                listRightModel.add(idx, itemsLeft.get(idx));
            }
        }
        // Clear selection in the left list
        listLeft.clearSelection();
        repaint();
    }

    /**
     *
     */
    class ListLeftRightTransfHdlr extends TransferHandler {

        @Override
        public boolean canImport(TransferHandler.TransferSupport info) {
            // Check for String flavor
            if (!info.isDrop()) {
                return false;
            }
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection("");
        }

        @Override
        public int getSourceActions(JComponent c) {
            return TransferHandler.MOVE;
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            if (!info.isDrop()) {
                return false;
            }

            JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
            int index = dl.getIndex();
            boolean insert = dl.isInsert();
            // Perform the actual import.  
            if (insert) {
                copyLeftToRight(index);
            }
            return true;
        }
    }
}
