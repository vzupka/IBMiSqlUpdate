package update;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.util.ArrayList;
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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout.Alignment;

/**
 * Update BLOB contents - display, update or print
 *
 * @author Vladimír Župka 2016
 *
 */
public class U_BlobUpdate extends JDialog {

   static final long serialVersionUID = 1L;

   U_DataTable dataTable;
   String colName;
   JLabel imageLabel;
   byte[] bytes;
   Image img;

   Locale locale;
   ResourceBundle titles;
   String titView, updateColumn;
   ResourceBundle buttons;
   String _return, getFile, putFile, pagset, print;
   ResourceBundle messages;
   String notRendered, blobCapacity, colLengthIs, contentLoaded, colValueNull, blobNotChanged;

   Properties properties;
   BufferedReader infile;
   Path parPath = Paths.get(System.getProperty("user.dir"), "paramfiles", "Parameters.txt");
   String encoding = System.getProperty("file.encoding");
   String language;
   int windowHeight;
   int windowWidth;
   String autoWindowSize;
   int fontSize;
   Font font;

   // Measuring units millimeters for MediaPrintableArea
   private static final int MM = 1000;

   PageFormat pageFormat;
   PrintRenderer printRenderer;

   long blobLength;
   Blob blob;
   InputStream stream;
   U_BlobReturnedValues retValues;
   boolean blobChanged;

   JLabel title;

   JButton returnButton;
   JButton getButton;
   JButton putButton;
   JButton previewButton;
   JButton printButton;
   JPanel buttonPanel;

   JLabel msg = new JLabel();
   JPanel msgPanel;

   JScrollPane scrollPane = new JScrollPane();
   JPanel globalPanel = new JPanel();
   Container container;
   GroupLayout layout = new GroupLayout(globalPanel);

   // Lines from the print file
   ArrayList<String> lineList = new ArrayList<>();

   // Printer job
   PrinterJob printerJob;
   PrintRequestAttributeSet attr_set;

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

   final Color DIM_BLUE = new Color(50, 60, 160);
   final Color DIM_RED = new Color(190, 60, 50);

   /**
    * Constructor
    *
    * @param dataTable
    */
   public U_BlobUpdate(U_DataTable dataTable) {
      super();
      super.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
      this.dataTable = dataTable;
      // Application properties
      properties = new Properties();
      try {
         BufferedReader infile = Files.newBufferedReader(parPath, Charset.forName(encoding));
         properties.load(infile);
         infile.close();
      } catch (IOException ioe) {
         ioe.printStackTrace();
      }
      
      // Create and register a window listener
      WindowListener windowListener = new MainWindowAdapter();
      this.addWindowListener(windowListener);
      
      retValues = new U_BlobReturnedValues();
      retValues.setMsg(msg);
   }

   /**
    * Create and show window
    *
    * @param colName
    * @param blob
    * @param blobLength
    */
   @SuppressWarnings({ "ConvertToStringSwitch", "UseSpecificCatch" })
   public void createWindow(String colName, Blob blob, long blobLength) {
      this.blob = blob;
      this.colName = colName;
      this.blobLength = blobLength;

      // Set flag "blob not changed"
      blobChanged = false;

      // Get application propertieserties
      windowHeight = new Integer(properties.getProperty("RESULT_WINDOW_HEIGHT"));
      windowWidth = new Integer(properties.getProperty("RESULT_WINDOW_WIDTH"));
      autoWindowSize = properties.getProperty("AUTO_WINDOW_SIZE");

      language = properties.getProperty("LANGUAGE");
      Locale currentLocale = Locale.forLanguageTag(language);

      // Get resource bundle classes
      titles = ResourceBundle.getBundle("locales.L_TitleLabelBundle", currentLocale);
      buttons = ResourceBundle.getBundle("locales.L_ButtonBundle", currentLocale);
      messages = ResourceBundle.getBundle("locales.L_MessageBundle", currentLocale);

      // Localized titles
      updateColumn = titles.getString("UpdateColumn");

      // Localized button labels
      _return = buttons.getString("Return");
      getFile = buttons.getString("GetFile");
      putFile = buttons.getString("PutFile");
      pagset = buttons.getString("Pagset");
      print = buttons.getString("Print");

      // Localized messages
      notRendered = messages.getString("NotRendered");
      blobCapacity = messages.getString("BlobCapacity");
      colLengthIs = messages.getString("ColLengthIs");
      contentLoaded = messages.getString("ContentLoaded");
      colValueNull = messages.getString("ColValueNull");
      blobNotChanged = messages.getString("BlobNotChanged");

      title = new JLabel(updateColumn + colName);
      Font titleFont = new Font("Helvetica", Font.PLAIN, 20);
      title.setFont(titleFont);

      returnButton = new JButton(_return);
      returnButton.setMinimumSize(new Dimension(95, 35));
      returnButton.setMaximumSize(new Dimension(95, 35));
      returnButton.setPreferredSize(new Dimension(95, 35));

      getButton = new JButton(getFile);
      getButton.setMinimumSize(new Dimension(110, 35));
      getButton.setMaximumSize(new Dimension(110, 35));
      getButton.setPreferredSize(new Dimension(110, 35));

      putButton = new JButton(putFile);
      putButton.setMinimumSize(new Dimension(110, 35));
      putButton.setMaximumSize(new Dimension(110, 35));
      putButton.setPreferredSize(new Dimension(110, 35));

      previewButton = new JButton(pagset);
      previewButton.setMinimumSize(new Dimension(150, 35));
      previewButton.setMaximumSize(new Dimension(150, 35));
      previewButton.setPreferredSize(new Dimension(150, 35));

      printButton = new JButton(print);
      printButton.setMinimumSize(new Dimension(120, 35));
      printButton.setMaximumSize(new Dimension(120, 35));
      printButton.setPreferredSize(new Dimension(120, 35));

      // Build button row
      buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
      buttonPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
      buttonPanel.add(returnButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      buttonPanel.add(getButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      buttonPanel.add(putButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      buttonPanel.add(previewButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      buttonPanel.add(printButton);
      buttonPanel.add(Box.createRigidArea(new Dimension(0, 60)));

      // Message panels
      msgPanel = new JPanel();
      BoxLayout msgLayoutX = new BoxLayout(msgPanel, BoxLayout.Y_AXIS);
      msgPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
      msgPanel.setLayout(msgLayoutX);

      // Build scroll pane with BLOB image or message.
      // Get a label with an icon of the BLOB contents
      imageLabel = renderImage();
      msg.setText(colLengthIs + this.blobLength);
      msg.setForeground(DIM_BLUE); // Dim blue
      // If the contents cannot be rendered (dimensions are not positive numbers)
      if (imageLabel == null) {
         // Display a negative message - not rendered
         imageLabel = new JLabel();
         imageLabel.setText("");
         msg.setForeground(DIM_BLUE); // Dim blue
         msg.setText(notRendered + colLengthIs + this.blobLength);
      }
      msgPanel.add(msg);

      // Set scroll pane with an image label of the BLOB or a message
      // or a default image with the negative explaining message.
      scrollPane.setViewportView(imageLabel);
      // Maximum heigth - this big number ensures visibility of all the window.
      // If no max. size is specified the bottom of the window is
      // invisible under the screen.
      int screenWeight = Toolkit.getDefaultToolkit().getScreenSize().width;
      int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
      scrollPane.setMaximumSize(new Dimension(screenWeight - 25, screenHeight - 180));

      // Lay out components in the window
      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);
      layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(Alignment.LEADING)
            .addComponent(title)
            .addComponent(scrollPane)
            .addComponent(buttonPanel)
            .addComponent(msgPanel)));
      layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createSequentialGroup()
            .addComponent(title)
            .addComponent(scrollPane)
            .addComponent(buttonPanel)
            .addComponent(msgPanel)));
      globalPanel.setLayout(layout);
      globalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      // Set "Return" button activity
      // ----------------------------
      returnButton.addActionListener(a -> {
         // Save the updated BLOB object and blob length
         // and message label into U_BlobReturnedValues object "retValues"
         // for the user (caller) of this class to get these values for further processing
         retValues.setBlob(this.blob);
         retValues.setLength(this.blobLength);
         // A special case when the blob was not changed.
         if (blobChanged)
            retValues.setMsg(msg);
         else
            retValues.setMsg(new JLabel(blobNotChanged));
         // Close the window
         dispose();
      });

      // Set Get file button activity
      // ----------------------------
      getButton.addActionListener((ActionEvent al) -> {

         long fileLength;
         bytes = null;
         String[] suffixes = { ".JPEG", ".JPG", ".GIF", ".PNG", ".BMP", ".WBMP" };
         msg.setText(" ");
         msg.setForeground(DIM_BLUE); // Dim blue

         // Invoke file chooser dialog to choose a file for the CLOB column
         U_GetFile getFile = new U_GetFile();
         // The dialog delivers a file
         this.dataTable.file = getFile.getFile(this.dataTable.file);

         // If the FILE IS NULL (the user canceled the dialog)
         if (this.dataTable.file == null) {
            this.blob = null;
            this.blobLength = 0;
            msg.setText(colValueNull);
            // Save the updated BLOB object and blob length
            // and message label into U_BlobReturnedValues object "retValues"
            // for the user (caller) of this class to get these values for further processing
            retValues.setBlob(this.blob);
            retValues.setLength(this.blobLength);
            retValues.setMsg(msg);
            dispose();
         } else {
            // If the FILE is NOT NULL (the user chose a file)
            fileLength = this.dataTable.file.length();
            int colCapacity = 0;
            try {
               this.blob.truncate(0);
               // Read all bytes from the file and put to a byte array
               bytes = Files.readAllBytes(this.dataTable.file.toPath());
               // Put the byte array into the BLOB
               this.blobLength = this.blob.setBytes(1, bytes);

               // Find column capacity
               for (int idx = 0; idx < this.dataTable.blobColNames.size(); idx++) {
                  if (colName.equals(this.dataTable.blobColNames.get(idx))) {
                     colCapacity = this.dataTable.blobColSizes.get(idx);
                     break;
                  }
               }
               if (fileLength > colCapacity) {
                  msg.setForeground(DIM_RED); // red
                  msg.setText(blobCapacity + this.blobLength + ".");
               } else {
                  // Check if the file is an image capable to render
                  String path = this.dataTable.file.toPath().toString().toUpperCase();
                  boolean match = false;
                  for (String suffixe : suffixes) {
                     // If the file suffix matches one from the list
                     // note the match and break the loop.
                     if (path.endsWith(suffixe)) {
                        match = true;
                        break;
                     }
                  }
                  if (match) {
                     // Create an image and an icon from the bytes.
                     img = Toolkit.getDefaultToolkit().createImage(bytes);
                     ImageIcon icon = new ImageIcon(img);
                     // Image width or heigth must be positive to be rendered
                     if (img.getWidth(this) > 0) {
                        // If the width is non-zero - Set the icon to the
                        // JLabel
                        imageLabel = new JLabel();
                        imageLabel.setIcon(icon);
                        msg.setText(contentLoaded + this.blobLength);
                        msg.setForeground(DIM_BLUE);
                        scrollPane.setViewportView(imageLabel);
                     } else {
                        // If the width is 0 or -1 set empty message
                        // to the scroll pane
                        imageLabel = new JLabel();
                        imageLabel.setText("");
                        msg.setText(notRendered + colLengthIs + this.blobLength);
                        msg.setForeground(DIM_BLUE);
                        scrollPane.setViewportView(imageLabel);
                     }
                  } else {
                     // If the file suffix does not conform - set empty message to the scroll pane
                     imageLabel = new JLabel();
                     imageLabel.setText("");
                     msg.setForeground(DIM_BLUE); // Dim blue
                     msg.setText(notRendered + colLengthIs + this.blobLength);
                     scrollPane.setViewportView(imageLabel);
                  }
               }
               // No try-catch message (but blob capacity exceeded)
               blobChanged = true;
               //                    msgPanel.removeAll();
               //                    msgPanel.add(msg);

            } catch (Exception e) {
               System.out.println("e.getLocalizedMessage(): " + e.getLocalizedMessage());
               e.printStackTrace();
               msg.setForeground(DIM_RED); // red
               msg.setText(blobCapacity);
               msgPanel.add(msg);
            } finally {
               retValues.setBlob(this.blob);
               retValues.setLength(this.blobLength);
               retValues.setMsg(msg);
            }
            msgPanel.removeAll();
            msgPanel.add(msg);

            // Show the window
            pack();
            repaint();
            setVisible(true);
         }
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
               byte[] bytes;
               bytes = blob.getBytes(1, (int) blob.length());
               Files.write(this.dataTable.file.toPath(), bytes);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      });

      // Set Preview button activity
      // ---------------------------
      previewButton.addActionListener(a -> {
         U_ClobPrintSetting printSetting = new U_ClobPrintSetting();
         printSetting.buildDataWindow();
      });

      // Set Print button activity
      // --------------------------
      printButton.addActionListener(a -> {
         // Print contents of the work area
         printResult();
      });

      container = getContentPane();
      container.add(globalPanel);

      // Y = pack the window to actual contents, N = set fixed size
      if (autoWindowSize.equals("Y")) {
         pack();
      } else {
         setSize(windowWidth, windowHeight);
      }
      setLocation(0, 0);
      // Show the window
      pack();
      setVisible(true);
   } // End of createWindow()

   /**
    * Create a label with an icon (or null) for the beginning of building the
    * window. The label is put in the scroll pane to show the image in the
    * window.
    *
    * @return
    */
   protected JLabel renderImage() {
      JLabel imageLabel = new JLabel();
      try {
         // Get byte array from the blob
         bytes = blob.getBytes(1, (int) blob.length());
         // Create image and icon from the bytes
         img = Toolkit.getDefaultToolkit().createImage(bytes);
         ImageIcon icon = new ImageIcon(img);
         // Image width or heigth must be positive to be rendered
         if (img.getWidth(this) > 0) {
            // If the width is non-zero - Set the icon to the JLabel
            imageLabel.setIcon(icon);
         } else {
            // If the width is zero or -1 Set null to the JLabel
            imageLabel = null;
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return imageLabel;
   }

   /**
    * Returns values saved in U_ClobReturnedValues class
    *
    * @return
    */
   public U_BlobReturnedValues getReturnedValues() {
      return retValues;
   }

   /**
    * Prints contents of the BLOB
    */
   @SuppressWarnings("ConvertToStringSwitch")
   protected void printResult() {
      fontSize = new Integer(properties.getProperty("FONT_SIZE"));
      paperSize = properties.getProperty("MEDIA_SIZE");
      orientation = properties.getProperty("ORIENTATION");
      if (orientation.equals("L") || orientation.equals("LANDSCAPE")) {
         orientation = "LANDSCAPE";
      }
      if (orientation.equals("P") || orientation.equals("PORTRAIT")) {
         orientation = "PORTRAIT";
      }
      leftMargin = new Integer(properties.getProperty("LEFT_MARGIN"));
      rightMargin = new Integer(properties.getProperty("RIGHT_MARGIN"));
      topMargin = new Integer(properties.getProperty("TOP_MARGIN"));
      bottomMargin = new Integer(properties.getProperty("BOTTOM_MARGIN"));

      // Page renderer is an object which prepares and prints a page
      printRenderer = new PrintRenderer(pageFormat);
      JScrollPane jsp = new JScrollPane(printRenderer);
      scrollPane.removeAll();
      scrollPane.add(jsp);
      scrollPane.setViewportView(jsp);
      validate();

      // Set parameters for page format
      // ------------------------------      
      // Set print request attributes
      attr_set = new HashPrintRequestAttributeSet();

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
      printerJob.setPrintable(printRenderer, pageFormat);

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

      /*
       * // ???? // Printing wihout dialog try { printerJob.print(attr_set); }
       * catch (Exception e) { e.printStackTrace(); }
       */ // ????			
   }

   /**
    * Class to render contents of the file for printing
    */
   class PrintRenderer extends JComponent implements Printable {

      private static final long serialVersionUID = 1L;
      BufferedImage bufImage;

      /**
       * Constructor
       *
       * @param pageFormat
       */
      public PrintRenderer(PageFormat pageFormat) {
         if (orientation.equals("LANDSCAPE")) {
            // Heigth and Width are reversed
            imWidth = mmHeight * pointsInMM;
            imHeight = mmWidth * pointsInMM - 8f * pointsInMM;
            pageLengthInPoints = imHeight - (2 * bottomMargin + 5) * pointsInMM;
            pageLength = pageLengthInPoints / fontSize;
            pageWidthInPoints = imWidth;
            pageWidth = pageWidthInPoints / fontSize;
         } else {
            // Orientation Portrait
            imWidth = mmWidth * pointsInMM;
            imHeight = mmHeight * pointsInMM - 8f * pointsInMM;
            pageLengthInPoints = imHeight - (2 * bottomMargin + 5) * pointsInMM;
            pageLength = pageLengthInPoints / fontSize;
            pageWidthInPoints = imWidth;
            pageWidth = pageWidthInPoints / fontSize;
         }
      }

      /**
       * Prints the graphical picture of the image
       */
      @Override
      public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
         if (pageIndex == 0) {
            Graphics2D g2d = (Graphics2D) g;
            // Translate the image to the upper left corner of the page.
            g2d.translate(pageFormat.getImageableX() + leftMargin * pointsInMM, pageFormat.getImageableY() + topMargin * pointsInMM);
            // Scale the image proportionally to page width
            double rate = (pageFormat.getImageableWidth() - (leftMargin + rightMargin) * pointsInMM) / img.getWidth(this);
            g2d.scale(rate, rate);
            // Draw the image (no affine transformaion - null)
            g2d.drawImage(img, null, this);
            return Printable.PAGE_EXISTS;
         } else {
            return Printable.NO_SUCH_PAGE;
         }
      }
   }
   
    /**
     * Window adapter setting current coordinates of the window to properties.
     */
    class MainWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent we) {
         // Save the updated BLOB object and blob length
         // and message label into U_BlobReturnedValues object "retValues"
         // for the user (caller) of this class to get these values for further processing
         retValues.setBlob(blob);
         retValues.setLength(blobLength);
         // A special case when the blob was not changed.
         if (blobChanged)
            retValues.setMsg(msg);
         else
            retValues.setMsg(new JLabel(blobNotChanged));
         // Close the window 
         dispose();
        }
    }
}
