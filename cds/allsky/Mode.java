package cds.allsky;

public enum Mode {

   // La m�thode utilis� pour coadder agit au niveau des pixels
   KEEP,          // Si la valeur existante du pixel est != BLANK, alors le garde
   OVERWRITE,     // si la nouvelle valeur du pixel est != BLANK, alors remplace la valeur existante
   AVERAGE,       // Effectue la moyenne de la valeur existante avec la nouvelle valeur
   ADD,           // Effectue la somme de la valeur existante avec la nouvelle valeur

   // La m�thode utilis� pour coadder agit au niveau des tuiles HEALPix
   REPLACETILE,    // Recalcule toutes les tuiles (de niveau le plus bas)
   KEEPTILE,       // Conserve en l'�tat toutes les tuiles (de niveau le plus bas) d�j� calcul�es

   // Dans le cas de la cr�ation d'un cube composite
   LINK,          // Les tuiles du cube composite seront en fait des liens symboliques
   COPY;          // Les tuiles du cube composite seront des copies des tuiles originales

   public static Mode getDefault() {
      return OVERWRITE;
   }

   public static String getExplanation(Mode m) {
      if( m==KEEP )        return m+": "+"Add pixel values only for pixels not yet computed or BLANK";
      if( m==OVERWRITE )   return m+": "+"Replace existing pixel values if the new value is not BLANK";
      if( m==AVERAGE )     return m+": "+"Compute the weighted average value based on the new pixel value and the existing one";
      if( m==ADD )         return m+": "+"Compute the sum value of new new pixel value and the existing one";
      if( m==REPLACETILE ) return m+": "+"Add new tiles, and if necessary, replace existing tiles (low level tiles)";
      if( m==KEEPTILE )    return m+": "+"Add new tiles but only for those not yet computed (low level tiles)";
      if( m==LINK )        return m+": "+"Composite cube tiles based on symbolic links on original tiles";
      if( m==COPY )        return m+": "+"Composite cube tiles are copies of original tiles";
      return "";
   }
}
