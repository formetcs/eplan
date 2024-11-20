package java.lang;

public class StringBuffer extends java.lang.Object implements java.io.Serializable
						    //, CharSequence 
{
    public StringBuffer();
    public StringBuffer(int n);
    public StringBuffer(java.lang.String s);

    public java.lang.StringBuffer	append(boolean b); 
    public java.lang.StringBuffer	append(char c); 
    //    public StringBuffer	append(char[] str); 
    //    public StringBuffer	append(char[] str, int offset, int len); 
    //    public StringBuffer	append(double d); 
    //    public StringBuffer	append(float f); 
    public java.lang.StringBuffer	append(int i); 
    public java.lang.StringBuffer	append(long l); 
    public java.lang.StringBuffer	append(java.lang.Object obj); 
    public java.lang.StringBuffer	append(java.lang.StringBuffer sb); 

    public char charAt(int index) ;
    public int length();
    //    public CharSequence subSequence(int start, int end);
    java.lang.String toString();
 }
