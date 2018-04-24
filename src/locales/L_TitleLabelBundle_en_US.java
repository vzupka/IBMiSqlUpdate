package locales;

import java.util.ListResourceBundle;

/**
 * 
 * @author Vladimír Župka 2016
 */
public class L_TitleLabelBundle_en_US extends ListResourceBundle {
   @Override
   public Object[][] getContents() {
      return contents;
   }

   private final Object[][] contents = { 
         // U_Menu
         { "SelRun", "Run table update" },
         { "ParApp", "Edit parameters for the application" },
         { "TitMenu", "\nMaintenance of database files\n" },
         { "TitEdit", "Table " }, 

         // U_DataTable list
         { "Where", "Enter condition WHERE for row selection and press "}, 
         { "Order", "Enter condition ORDER BY for row ordering and press "},   
         { "ChangeCell", "Change a cell value: Double click, rewrite the cell value and press ENTER (or click TAB or another cell)."},       

         // U_DataTable data
         { "EnterData", "Enter data for the table " }, 

         // ParametersEdit
         { "DefParApp", "\nSet application parameters and run\n" },
         { "AdrSvr", "Server address" },
         { "UsrName", "User name" },
         { "Library", "Library with database files" },
         { "File", "Database file" },
         { "Member", "File member" },
         { "FileSelect", "Select database file" },
         { "Char_set", "Character set for CLOB" },
         { "CharSelect", "Select character set for CLOB" },
         { "AutWin", "Automatic window size" },
         { "WinWidth", "Window width" },
         { "WinHeight", "Window height" },
         { "NullMark", "Mark for null field values" },
         { "FontSize", "Size of the font for data displayed in fields" },
         { "MaxFldLen", "Limit for length of displayed data field" },
         { "PrintFontSize", "Size of the font for printing CLOB in print points" },
         { "FetchFirst", "Maximum number of records to display" },         
         { "OrEnter", "or press ENTER    " },  
         { "NoTable", "No table in schema." },
         { "InvalSchema", "Invalid schema name." },  
         { "NoCharset", "No character set" },  

         // U_ColumnJList
         { "TitleCol", "Define list of fields to display" }, 
         { "Prompt1Col", "Select names on the left and drag them to the right or press button  \">>>\"." }, 
         { "Prompt2Col", "Remove selected names on the right by pressing button  \"X<<\"." }, 
         { "Prompt3Col", "Remove all names on the right by pressing button  \"Clear all\"." }, 

         // SQL properties
         { "DecSeparator", "." },
         { "SortLanguage", "ENU" }, 
         
         // U_ClobUpdate
         { "UpdateColumn", "Column " },
         { "StartOfText", "Start of text:" },
         { "LengthOfText", "Length of text:" },
         { "Find", "Find text:" },

         // U_ClobPrintSetting
         { "PageSetTitle", "Page Setup" },
         { "PaperSiz", "Paper Size (A4, A3, LETTER)" },
         { "FontSiz", "Font Size" },
         { "Orient", "Orientation (PORTRAIT/P, LANDSCAPE/L)" },
         { "LeftMar", "Left Margin" },
         { "RightMar", "Right Margin" },
         { "TopMar", "Top Margin" },
         { "BottomMar", "Bottom Margin" },         
   };
}
