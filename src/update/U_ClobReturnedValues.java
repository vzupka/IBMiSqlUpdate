package update;

import java.sql.Clob;

import javax.swing.JLabel;

/**
 * Return values of Clob text parameters
 * 
 * @author Vladimír Župka 2016
 */
public class U_ClobReturnedValues {

   private Clob clob;
   private long startPos;
   private int length;
   private JLabel msg;

   public void setClob (Clob clob) {
      this.clob = clob;
   }
   public Clob getClob () {
      return this.clob;
   }
   public void setStartPos (long startPos) {
      this.startPos = startPos;
   }
   public long getStartPos () {
      return this.startPos;
   }
   public void setLength (int length) {
      this.length = length;
   }
   public int getLength () {
      return this.length;
   }
   public void setMsg (JLabel msg) {
      this.msg = msg;
   }
   public JLabel getMsg () {
      return this.msg;
   }
}
