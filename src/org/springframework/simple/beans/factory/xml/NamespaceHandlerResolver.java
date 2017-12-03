package org.springframework.simple.beans.factory.xml;

public interface NamespaceHandlerResolver {

	NamespaceHandler resolve(String namespaceUrl);
}
