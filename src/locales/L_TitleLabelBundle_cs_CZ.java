package locales;

import java.util.ListResourceBundle;

/**
 * 
 * @author Vladimír Župka 2016
 */
public class L_TitleLabelBundle_cs_CZ extends ListResourceBundle {
   @Override
   public Object[][] getContents() {
      return contents;
   }

   private final Object[][] contents = {
         // U_Menu
         { "SelRun", "Spustit aktualizaci tabulky" },
         { "ParApp", "Upravit parametry aplikace" },
         { "TitMenu", "\nÚdržba databázových souborů\n" }, 
         { "TitEdit", "Tabulka " }, 

         // U_DataTable list
         { "Where", "Zapište podmínku WHERE pro výběr řádků a stiskněte "},       
         { "Order", "Zapište podmínku ORDER BY pro seřazení řádků a stiskněte "},       
         { "ChangeCell", "Změna hodnoty buňky: Poklepejte buňku, přepište její hodnotu a stiskněte ENTER (nebo TAB, nebo jinou buňku)."},       

         // U_DataTable data
         { "EnterData", "Zadejte data pro tabulku " }, 

         // U_ParametersEdit
         { "DefParApp", "\nParametry aplikace a spuštění\n" },
         { "AdrSvr", "Adresa serveru" },
         { "UsrName", "Jméno uživatele" },
         { "Library", "Knihovna s databázovými soubory" },
         { "File", "Databázový soubor" },
         { "Member", "Člen souboru" },
         { "FileSelect", "Výběr databázového souboru" },
         { "Char_set", "Znaková sada pro CLOB" },
         { "CharSelect", "Výběr znakové sady pro CLOB" },
         { "AutWin", "Automatická velikost okna" },
         { "WinWidth", "Šířka okna" },
         { "WinHeight", "Výška okna" },
         { "NullMark", "Značka pro prázdné hodnoty polí" },
         { "FontSize", "Výška písma pro zobrazení dat v počtu tiskových bodů" },
         { "MaxFldLen", "Limit délky zobrazeného datového pole" },
         { "PrintFontSize", "Výška písma pro tisk CLOB v počtu tiskových bodů" },
         { "FetchFirst", "Maximální počet zobrazených záznamů" },         
         { "OrEnter", "nebo stisknout ENTER    " }, 
         { "NoTable", "Žádná tabulka ve schematu" },  
         { "InvalSchema", "Chybné jméno schematu" },  
         { "NoCharset", "Chybí znaková sada" },  

         // U_ColumnJList
         { "TitleCol", "Určete seznam polí k zobrazení" }, 
         { "Prompt1Col", "Vyberte jména vlevo a stiskněte >>> k zařazení." }, 
         { "Prompt2Col", "Vyberte jména vpravo a stiskněte XXX k odstranění." }, 
         { "Prompt3Col", "Stiskněte Vymazat vše k odstranění všech jmen vpravo." }, 

         // SQL properties
         { "DecSeparator", "," }, 
         { "SortLanguage", "CSY" }, 
         
         // U_ClobUpdate
         { "UpdateColumn", "Sloupec " },
         { "StartOfText", "Začátek textu:" },
         { "LengthOfText", "Délka textu:" },
         { "Find", "Hledat text:" },
         
         // U_ClobPrintSetting
         { "PageSetTitle", "Vzhled stránky" },
         { "PaperSiz", "Velikost papíru (A4, A3, LETTER)" },
         { "FontSiz", "Velikost písma pro CLOB" },
         { "Orient", "Orientace (PORTRAIT/P, LANDSCAPE/L)" },
         { "LeftMar", "Levý okraj" },
         { "RightMar", "Pravý okraj" },
         { "TopMar", "Horní okraj" },
         { "BottomMar", "Spodní okraj" },         
   };
}
