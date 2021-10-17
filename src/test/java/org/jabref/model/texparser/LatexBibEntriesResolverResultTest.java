package org.jabref.model.texparser;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class LatexBibEntriesResolverResultTest {
    LatexBibEntriesResolverResult latex1;

    @BeforeEach
    public void init() {
        latex1 = new LatexBibEntriesResolverResult(new LatexParserResult());
    }

    @Test
    public void equalsObjectsTest() {
        LatexBibEntriesResolverResult latex2 = new LatexBibEntriesResolverResult(new LatexParserResult());
        boolean isEquals = latex2.equals(latex2);
        assertTrue(isEquals);
    }

    @Test
    public void sameObjectTest() {
        LatexBibEntriesResolverResult latex2 = new LatexBibEntriesResolverResult(new LatexParserResult());
        boolean isEquals = latex2.equals(latex1);
        assertTrue(isEquals);
    }

    @Test
    public void notSameClassTest() {
        LatexBibEntriesResolverResult latex2 = new LatexBibEntriesResolverResult(new LatexParserResult());
        boolean isEquals = latex2.equals(latex1.toString());
        assertFalse(isEquals);
    }

    @Test
    public void nullObjectTest() {
        LatexBibEntriesResolverResult latex2 = new LatexBibEntriesResolverResult(new LatexParserResult());
        boolean isEquals = latex2.equals(null);
        assertFalse(isEquals);
    }

}
