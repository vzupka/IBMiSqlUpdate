package locales;

import java.util.ListResourceBundle;

/**
 * 
 * @author Vladimír Župka 2016
 */
public class L_MessageBundle_en_US extends ListResourceBundle {
   @Override
   public Object[][] getContents() {
      return contents;
   }

   final private Object[][] contents = {
         // U_Main
         { "CurDir", "Application directory = " },
         { "NoConnection", "NO CONNECTION TO THE DATABASE SERVER!" },
         { "InvalSchema", "Invalid library name" },
         { "CorrectPar", "Correct application parameters or the library name." },
         { "ConnExit", "Connection to the database was interrupted on Exit button. Get connection again." },
         
         { "ParSaved", "Parameters have been saved to the file: " },
         { "InvalFile", "Invalid file name!" },
         { "InvalCharset", "Invalid charset code!" },
         { "ConnLost", "LOST CONNECTION TO THE SERVER  " },
         { "CloseStart", "Close and start the application again." },

         // U_ConnectDB
         { "Driver", "JDBC driver not found for the host " },
         { "ConnErr", "Connection error: " },
         
         // U_DataTable
         { "NoRowUpd", "No row was selected. Select one to edit." },
         { "NoRowDel", "No row was selected. Select one to delete." },

         // U_DataTable, getData()
         { "SqlError", "SQL error: " },
         { "InvalidValue", "Unacceptable value for column " },
         { "InvalidCharset", "Invalid character set" },
         { "DataError", "Input data error: " },
         { "NoDataMember", "No data." },
         { "NoData", "No data. Correct values for WHERE/ORDER BY "
               + "or correct the table name in Parameters." },
         // U_DataTable, checkInputFields()
         { "Value", "Value " },
         { "TooLong", " of the field is too long." },
         { "Length", "Length " },
         
         // U_DataTable, updateClob()
         { "TooLongForCol", " is too big for the column " },
         { "ContentLoaded", "Content of the file or part of it was loaded into the column. Length is " },
         { "ColValueNull", "Column value is NULL." },
         { "ColNotText", "Content type is not correct text. Choose another charset." },               
         
         // U_DataTable, updateBlob()
         { "ContentNotLoaded", " Content was not loaded." },
         { "BlobNotChanged", "BLOB was not changed" },

         // U_ClobUpdate
         { "ColUndef", "Column content is undefined." },   
         { "InvalCharSet", "The file does not conform to character set " },
         { "ColUnchg", ". Column remains unchanged." },
         { "ColLenZero", ". Column will be text of lentgth 0." },
         { "ClobCapacity", " Column capacity was exceeded.  File length is " },
         { "NotFound", "The searched text was not found." },
         { "ColLengthIs", " Column length is " },
         
         // U_ClobPrintSetting
         { "InvalPaperSize", "Invalid paper size. May be A4, A3, LETTER." },
         { "InvalOrient", "Invalid orientation. May be PORTRAIT/P, LANDSCAPE/L." },
         { "InvalFontSiz", "Font size is not a whole number." },
         { "InvalLeftMar", "Left margin is not a whole number." },
         { "InvalRightMar", "Right margin is not a whole number." },
         { "InvalTopMar", "Top margin is not a whole number." },
         { "InvalBottomMar", "Bottom margin is not a whole number." },
         
         // U_BlobUpdate
         { "NotRendered", " Content of the column is not being rendered." },
         { "BlobCapacity", " Column capacity was exceeded. File length is " },
   };
}
