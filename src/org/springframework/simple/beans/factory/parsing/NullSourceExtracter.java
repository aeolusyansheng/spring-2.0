package org.springframework.simple.beans.factory.parsing;

import org.springframework.core.io.Resource;

public class NullSourceExtracter implements SourceExtracter {

	@Override
	public Object extracterSource(Object sourceCandidate, Resource definingResource) {
		return null;
	}

}
