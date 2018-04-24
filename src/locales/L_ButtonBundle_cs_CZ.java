package locales;

import java.util.ListResourceBundle;

/**
 * 
 * @author Vladimír Župka 2016
 */
public class L_ButtonBundle_cs_CZ extends ListResourceBundle{
   @Override
   public Object[][] getContents() {
      return contents;
    }

    final private Object[][] contents = {
       // U_Menu
       {"Run", "Spustit"},
       {"Param", "Parametry"},
 
       // U_ParametersEdit
       {"Sav", "Uložit data"},  
       {"SelectFile", "Vyberte soubor"},  
       {"SelectCharset", "Vyberte charset"},  

       // U_DataTable list
       {"Exit", "Končit"},   
       {"Edit", "Editace"},
       {"Insert", "Vložit nový řádek"},       
       {"Edit_sel", "Upravit vybraný"},       
       {"Del_sel", "Zrušit vybraný"}, 
       {"Refresh", "Obnovit zobrazení"},         
       {"Columns", "Výběr sloupců"},       
       
       // U_DataTable data
       {"SaveData", "Uložit data"},       
       {"SaveReturn", "Uložit a návrat"},       
       {"Return", "Návrat"},       

       // U_ColumnJList
       {"CopyCol", ">>>"},       
       {"DeleteCol", "X<<"},       
       {"ClearAll", "Vymazat vše"},       
       {"SaveExit", "Uložit + návrat"},       

       // U_ClobUpdate + U_BlobUpdate
       {"Save", "Uložit data"},       
       {"GetFile", "Číst soubor"}, 
       {"PutFile", "Uložit soubor"},       
       {"Pagset", "Vzhled stránky"},       
       {"Print", "Tisk"},       
    };
}
