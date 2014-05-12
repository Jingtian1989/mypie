package org.my.pie.interpreter;

import org.my.pie.symbol.StructSymbol;

public class StructSpace extends MemorySpace {

	private StructSymbol structSymbol;

	public StructSpace(StructSymbol struct) {
		super(struct.getName());
		this.structSymbol = struct;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (String fieldName : structSymbol.getMembers().keySet()) {
			Object fieldValue = members.get(fieldName);
			if (!first) {
				sb.append(",");
			} else {
				first = false;
			}
			sb.append(fieldName);
			sb.append("=");
			sb.append(fieldValue);
		}
		sb.append("}");
		return sb.toString();
	}

	public StructSymbol getStructSymbol() {
		return structSymbol;
	}

}
