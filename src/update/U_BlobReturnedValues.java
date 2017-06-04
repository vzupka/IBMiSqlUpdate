package update;

import java.sql.Blob;

import javax.swing.JLabel;

/**
 * Return values for Blob parameeters
 * 
 * @author Vladimír Župka 2016
 */
public class U_BlobReturnedValues {

   private Blob blob;
   private long length;
   private JLabel msg;

   public void setBlob (Blob blob) {
      this.blob = blob;
   }
   public Blob getBlob () {
      return this.blob;
   }
   public void setLength (long length) {
      this.length = length;
   }
   public long getLength () {
      return this.length;
   }
   public void setMsg (JLabel msg) {
      this.msg = msg;
   }
   public JLabel getMsg () {
      return this.msg;
   }
}
