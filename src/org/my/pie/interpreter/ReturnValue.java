package org.my.pie.interpreter;

public class ReturnValue extends Error {

	private Object value;

	public ReturnValue() {
		super("");
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}
	
	public String toString() {
		return value.toString();
	}
}
