package org.my.pie.interpreter;

import java.util.HashMap;
import java.util.Map;

public class MemorySpace {

	String name;
	Map<String, Object> members = new HashMap<String, Object>();

	public MemorySpace(String name) {
		this.name = name;
	}

	public Object get(String id) {
		return members.get(id);
	}

	public void put(String id, Object value) {
		members.put(id, value);
	}

	public String toString() {
		return name + ":" + members;
	}

}
