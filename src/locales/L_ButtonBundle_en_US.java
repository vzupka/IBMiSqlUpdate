package locales;

import java.util.ListResourceBundle;
/**
 * 
 * @author Vladimír Župka 2016
 */
public class L_ButtonBundle_en_US extends ListResourceBundle{
   @Override
   public Object[][] getContents() {
      return contents;
    }

    final private Object[][] contents = {
       // U_Menu
       {"Run", "Run"},
       {"Param", "Parameters"},

       // U_ParametersEdit
       {"Sav", "Save data"}, 
       {"SelectFile", "Select file"},  
       {"SelectCharset", "Select charset"},  

       // U_DataTable list
       {"Exit", "Exit"},    
       {"Insert", "Insert new row"},
       {"Edit", "Edit selected"},
       {"Edit_sel", "Edit selected"},       
       {"Del_sel", "Delete selected"},    
       {"Refresh", "Refresh"},  
       {"Columns", "Select columns"},       

       
       // U_DataTable data
       {"SaveData", "Save data"},       
       {"SaveReturn", "Save and return"},       
       {"Return", "Return"},       

       // U_ColumnJList
       {"CopyCol", ">>>"},       
       {"DeleteCol", "XXX"},       
       {"ClearAll", "Clear all"},       
       {"SaveExit", "Save + return"},       

       // U_ClobUpdate + U_BlobUpdate
       {"Save", "Save data"},  
       {"GetFile", "Get File"},       
       {"PutFile", "Put File"},       
       {"Pagset", "Page Setting"},       
       {"Print", "Print"},       

    };
}
