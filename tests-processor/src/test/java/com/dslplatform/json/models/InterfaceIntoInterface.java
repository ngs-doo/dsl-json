package com.dslplatform.json.models;

import com.dslplatform.json.CompiledJson;

@CompiledJson(deserializeAs = InterfaceIntoInterface.Iface.class)
public interface InterfaceIntoInterface {
	long getY();
	interface Iface extends InterfaceIntoInterface {
		int getZ();
	}
}
