package update;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Choosing and opening a text file
 * 
 * @author Vladimír Župka 2016
 */
public class U_GetFileReader extends JFrame {
   private static final long serialVersionUID = 1L;

   File inFile;
   FileFilter textFilter;
   BufferedReader reader;
   
   /**
    * File dialog
     * @return 
    */
   public Reader getFileReader() {
      reader = null;
      inFile = null;
      textFilter = new FileNameExtensionFilter("Text files", "txt");

      // Pop up a file dialog
      JFileChooser fileChooser = new JFileChooser(".");
      fileChooser.addChoosableFileFilter(textFilter);
      
      int result = fileChooser.showOpenDialog(U_GetFileReader.this);
      System.out.println("result: "+result);
      if (result == 0) {         
         inFile = fileChooser.getSelectedFile();
         if (inFile != null) {
            try {
               reader = Files.newBufferedReader(inFile.toPath(), Charset.forName("UTF-8"));
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
         else 
            reader = null;
      }
      return reader;
   }

   public static void main(String...strings ) {
      U_GetFileReader gf = new U_GetFileReader();
      gf.getFileReader();
   }
}