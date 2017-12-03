package org.springframework.simple.beans.factory.parsing;

import java.util.EventListener;

public interface ReaderEventListener extends EventListener {

	void componentRegistered(ComponentDefinition componentDefinition);

	void aliasRegistered(AliasDefinition aliasDefinition);

	void importRegistered(ImportDefiniton importDefiniton);
}
