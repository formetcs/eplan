package java.util;

public final class LinkedList implements java.util.List {
	
	/*@ public normal_behavior
	  @ ensures seq == param0.seq;
      @*/
	public /*@pure@*/ LinkedList(java.util.Collection param0);
	
	/*@ public normal_behavior
      @ requires true;
      @*/
	public /*@pure@*/ java.lang.String toString();
}
