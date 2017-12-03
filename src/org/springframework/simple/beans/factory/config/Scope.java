package org.springframework.simple.beans.factory.config;

import org.springframework.simple.beans.factory.ObjectFactory;

public interface Scope {
	Object get(String name, ObjectFactory objectFactory);

	String getConversationId();

	void registerDestructionCallBack(String name, Runnable callback);

	Object remove(String name);
}
