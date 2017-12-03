package org.springframework.simple.beans.factory.parsing;

import org.springframework.core.io.Resource;

public interface SourceExtracter {

	Object extracterSource(Object sourceCandidate, Resource definingResource);
}
