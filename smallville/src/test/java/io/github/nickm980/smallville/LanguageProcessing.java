package io.github.nickm980.smallville;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import io.github.nickm980.smallville.math.SentenceTokenizer;

public class LanguageProcessing {

    @Disabled
    @Test
    public void testNamedEntities() {
	SentenceTokenizer comparison = new SentenceTokenizer();
	String s = comparison.extractName("John Lin went to the mall with John Smith which is inside the Barn Yard");
	System.out.println(s);
    }
}
