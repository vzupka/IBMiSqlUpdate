package update;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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

   // Empty array list with elements of type String
   ArrayList<String> arrListLeft = new ArrayList<>();
   ArrayList<String> arrListRight = new ArrayList<>();
   // List containing the arrList
   JList<String> listLeft = new JList<>();
   JList<String> listRight = new JList<>();

   JLabel message = new JLabel("");

   JScrollPane scrollPaneLeft = new JScrollPane(listLeft);
   JScrollPane scrollPaneRight = new JScrollPane(listRight);
   int scrollPaneWidth = 150;
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
   @SuppressWarnings("OverridableMethodCallInConstructor")
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
         arrListLeft.add(col.trim());
      }
      String[] data = new String[arrListLeft.size()];
      arrListLeft.toArray(data);
      listLeft.setListData(data);

      // Fill the Right list (resulting column selection) initially 
      // with the values from the .col file
      columnsPath = Paths
            .get(System.getProperty("user.dir"), "columnfiles", selectedFileName + ".col");
      try {
         List<String> items = Files.readAllLines(columnsPath);
         items.get(0);
         cols = items.get(0).split(",");
         for (int idx = 1; idx < cols.length; idx++) {
            arrListRight.add(cols[idx].trim());
         }
         data = new String[arrListRight.size()];
         arrListRight.toArray(data);
         listRight.setListData(data);

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

      scrollPaneLeft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      //scrollPaneLeft.setMaximumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
      scrollPaneLeft.setMinimumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
      scrollPaneLeft.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
      scrollPaneLeft.setBackground(scrollPaneLeft.getBackground());
      scrollPaneLeft.setBorder(BorderFactory.createLineBorder(Color.WHITE));

      scrollPaneRight.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      //scrollPaneRight.setMaximumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
      scrollPaneRight.setMinimumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
      scrollPaneRight.setPreferredSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
      scrollPaneRight.setBackground(scrollPaneLeft.getBackground());
      scrollPaneRight.setBorder(BorderFactory.createLineBorder(Color.WHITE));

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

      // Set Copy button activity
      // ------------------------
      copyButton.addActionListener(a -> {
         // Copy selected items from the left box to the right box
         List<String> itemsLeft = listLeft.getSelectedValuesList();
         // System.out.println("items: "+itemsLeft);
         if (!arrListRight.isEmpty()) {
            // Add left list after non-empty right list 
            String lastRightString = arrListRight.get(arrListRight.size() - 1);
            int lastRightIndex = arrListRight.indexOf(lastRightString);
            //System.out.println("lastRightString: "+lastRightString);
            //System.out.println("lastRightIndex: "+lastRightIndex);
            for (int idx = 0; idx < itemsLeft.size(); idx++) {
               boolean foundInRight = false;
               // Find out if the left item matches any right item
               for (int jdx = 0; jdx < lastRightIndex + 1; jdx++) {
                  if (itemsLeft.get(idx).equals(arrListRight.get(jdx))) {
                     //System.out.print("itemsLeft.get(idx): "+itemsLeft.get(idx));
                     //System.out.println(", arrListRight.get(jdx): "+arrListRight.get(jdx));
                     foundInRight = true;
                  }
               }
               // If the left item does not match any item in the right box vector
               // add the item at the end of the vector items in the right box.
               if (!foundInRight) {
                  arrListRight.add(itemsLeft.get(idx));
                  arrListLeft.remove(idx);
               }
            }
            // Add (put) left list in the empty right list
         } else {
            arrListRight.addAll(itemsLeft);
         }
         // Fill the right list with the items of the resulting vector
         String[] values = new String[arrListRight.size()];
         arrListRight.toArray(values);
         listRight.setListData(values);
      });

      // Set Delete button activity
      // --------------------------
      deleteButton.addActionListener(a -> {
         // Delete all selected items from the right box
         List<String> itemsRight = listRight.getSelectedValuesList();
         //System.out.println("itemsRight: " + itemsRight);

         // If both lists are not empty remove selected items in the right list
         if (!itemsRight.isEmpty() && !arrListRight.isEmpty()) {
            // System.out.println("items: "+itemsRight);
            int lastIndex = arrListRight.indexOf(itemsRight.get(itemsRight.size() - 1));
            int firstIndex = arrListRight.indexOf(itemsRight.get(0));
            //System.out.println("lastIndex: " + lastIndex);
            //System.out.println("firstIndex: " + firstIndex);
            // Remove selected items backwards (from END to START of the selection interval)
            for (int idx = lastIndex; idx >= firstIndex; idx--) {
               //System.out.println("arrListRight.get(" + idx + "): " + arrListRight.get(idx));
               boolean itemMatches = false;
               // Find out if the right selected item matches the corresponding 
               // vector item in the right box
               for (int jdx = 0; jdx < itemsRight.size(); jdx++) {
                  //System.out.println("itemsRight.get(" + jdx + "): " + itemsRight.get(jdx));
                  if (arrListRight.get(idx).equals(itemsRight.get(jdx))) {
                     itemMatches = true;
                  }
               }
               // If the two items match, remove the vector item
               if (itemMatches) {
                  arrListRight.remove(idx);
               }
            }
         }
         // Clear selection in the right list
         String[] values = new String[arrListRight.size()];
         arrListRight.toArray(values);
         listRight.setListData(values);
      });

      // Set Clear button activity
      // -------------------------
      clearButton.addActionListener(a -> {
         arrListRight.clear();
         String[] values = new String[arrListRight.size()];
         arrListRight.toArray(values);
         listRight.setListData(values);
      });

      // Set Save + Exit button activity
      // -------------------------------
      exitButton.addActionListener(a -> {
         returnedColumnList = "";
         // Build column list as a string (comma separated column names)
         for (String str : arrListRight) {
            returnedColumnList += ", ";
            returnedColumnList += str;
         }
         // System.out.println("returnedColumnList: "+returnedColumnList);

         // Write resulting column list to the file
         columnsPath = Paths
               .get(System.getProperty("user.dir"), "columnfiles", selectedFileName + ".col");
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
      @SuppressWarnings("OverridableMethodCallInConstructor")
      Container cont = getContentPane();
      cont.add(globalPanel);

      // Make window visible 
      setSize(500, 350);
      setLocation(300, 320);
      setVisible(true);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
   }
}
