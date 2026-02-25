package locales;

import java.util.ListResourceBundle;

/**
 * 
 * @author Vladimír Župka 2016
 */
public class L_MessageBundle_cs_CZ extends ListResourceBundle {
   @Override
   public Object[][] getContents() {
      return contents;
   }

   final private Object[][] contents = {
         // U_Main
         { "CurDir", "Aplikační adresář = " },
         { "NoConnection", "NENÍ SPOJENÍ S DATABÁZOVÝM SERVEREM!" },
         { "InvalSchema", "Chybné jméno knihovny" },
         { "CorrectPar", "Opravte aplikační parametry nebo jméno knihovny." },
         { "ConnExit", "Spojení s databází bylo přerušeno tlačítkem Končit. Připojte se znovu." },

         { "ParSaved", "Parametry byly uloženy do souboru: " },
         { "InvalFile", "Chybné jméno souboru!" },
         { "InvalCharset", "Chybný kód znakové sady!" },
         { "ConnLost", "ZTRACENO SPOJENÍ SE SERVEREM  " },
         { "CloseStart", "Zavřete a spusťte aplikaci znovu." },

         // U_ConnectDB
         { "Driver", "Nebyl nalezen ovladač JDBC k serveru " },
         { "ConnErr", "Chyba spojení: " },
         
         // U_DataTable
         { "NoRowUpd", "Není vybrán žádný řádek. Vyberte jeden k úpravě." },
         { "NoRowDel", "Není vybrán žádný řádek. Vyberte jeden ke zrušení." },

         // U_DataTable, getData()
         { "SqlError", "Chyba v SQL příkazu: " },
         { "InvalidValue", "Nepřijatelná hodnota pro sloupec " },
         { "InvalidCharset", "Chybná znaková sada" },
         { "DataError", "Chyba vstupních dat: " },
         { "NoDataMember", "Chybí data." },
         { "NoData", "Chybí data. Opravte hodnoty pro WHERE/ORDER BY "
               + "nebo opravte jméno tabulky v Parametrech." },
         // U_DataTable, checkInputFields()
         { "Value", "Hodnota " },
         { "TooLong", " pole je příliš dlouhá." },
         { "Length", "Délka " },
         
         // U_DataTable, updateClob()
         { "TooLongForCol", " je příliš velká pro sloupec " },
         { "ContentLoaded", "Do sloupce byl uložen obsah souboru nebo jeho část. Délka souboru je " },
         { "ColValueNull", "Hodnota sloupce je NULL." },
         { "ColNotText", "Typ obsahu není správný text. Zvolte jinou znakovou sadu." },               

         // U_DataTable, updateBlob()
         { "ContentNotLoaded", " Obsah nebyl uložen." },
         { "BlobNotChanged", "BLOB nebyl změněn" },

         // U_ClobUpdate
         { "ColUndef", "Obsah sloupce není definován." },    
         { "InvalCharSet", "Soubor nevyhovuje znakové sadě " },
         { "ColUnchg", ". Sloupec zůstává nezměněn." },
         { "ColLenZero", ". Sloupec bude text délky 0." },
         { "ClobCapacity", " Kapacita sloupce byla překročena. Délka souboru je " },               
         { "NotFound", "Hledaný text nebyl nalezen." },
         { "ColLengthIs", " Délka sloupce je " },
         
         // U_ClobPrintSetting
         { "InvalPaperSize", "Nesprávná velikost papíru. Může být A4, A3, LETTER." },
         { "InvalOrient", "Nesprávná orientace. Může být PORTRAIT/P, LANDSCAPE/L." },
         { "InvalFontSiz", "Velikost písma není celé číslo." },
         { "InvalLeftMar", "Levý okraj není celé číslo." },
         { "InvalRightMar", "Pravý okraj není celé číslo." },
         { "InvalTopMar", "Horní okraj není celé číslo." },
         { "InvalBottomMar", "Spodní okraj není celé číslo." },

         // U_BlobUpdate
         { "NotRendered", " Obsah sloupce se nezobrazuje." },
         { "BlobCapacity", " Kapacita sloupce byla překročena. Délka souboru je " },

   };
}
