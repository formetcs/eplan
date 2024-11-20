package java.util;

public interface Iterator
{
   public boolean hasNext();
   public /*@nullable@*/ java.lang.Object next();
   public void remove();
}
