package org.springframework.simple.beans.factory.parsing;

import org.springframework.core.io.Resource;

public class ReaderContext {

	private final Resource resource;
	private final ProblemReporter problemReporter;
	private final SourceExtracter sourceExtractor;
	private final ReaderEventListener readerEventListener;

	public ReaderContext(Resource resource, ProblemReporter problemReporter, SourceExtracter sourceExtractor,
			ReaderEventListener readerEventListener) {
		this.resource = resource;
		this.problemReporter = problemReporter;
		this.sourceExtractor = sourceExtractor;
		this.readerEventListener = readerEventListener;
	}

	public Resource getResource() {
		return this.resource;
	}

	public SourceExtracter getSourceExtracter() {
		return this.sourceExtractor;
	}

	public Object extractSource(Object sourceCandidate) {
		return this.sourceExtractor.extracterSource(sourceCandidate, this.resource);
	}

	public void error(String message, Object source) {

		this.error(message, source, null, null);
	}

	public void error(String message, Object source, ParseState parseState) {

		this.error(message, source, parseState, null);
	}

	public void error(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.error(new Problem(message, location, parseState, cause));
	}

	public void warn(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.warning(new Problem(message, location, parseState, cause));
	}

	public void warn(String message, Object source, ParseState parseState) {

		this.warn(message, source, parseState, null);
	}

	public void warn(String message, Object source) {

		this.warn(message, source, null, null);
	}

	public void fireComponentRegistered(ComponentDefinition componentDefinition) {
		this.readerEventListener.componentRegistered(componentDefinition);
	}

	public void fireAliasRegistered(String beanName, String alias, Object source) {
		this.readerEventListener.aliasRegistered(new AliasDefinition(beanName, alias, source));
	}

	public void fireImportProcessed(String importedResource, Object source) {
		this.readerEventListener.importRegistered(new ImportDefiniton(importedResource, source));
	}
}
