package update;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Choosing and opening a text file
 *
 * @author Vladimír Župka 2016
 */
public class U_GetFile extends JFrame {

    private static final long serialVersionUID = 1L;
    File resultFile = null;

    /**
     * File dialog
     *
     * @param currentFile
     * @return
     */
    public File getFile(File currentFile) {

        // Pop up a file dialog
        JFileChooser fileChooser = new JFileChooser(currentFile);

        int result = fileChooser.showOpenDialog(U_GetFile.this);
        if (result == 0) {
            resultFile = fileChooser.getSelectedFile();
        } else {
            resultFile = null;
        }
        return resultFile;
    }
}
