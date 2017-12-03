package org.springframework.simple.beans.factory.parsing;

public interface ProblemReporter {

	void error(Problem problem);

	void warning(Problem problem);
}
