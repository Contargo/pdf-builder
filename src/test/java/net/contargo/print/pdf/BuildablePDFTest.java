package net.contargo.print.pdf;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.*;

import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@RunWith(MockitoJUnitRunner.class)
public class BuildablePDFTest {

    @Mock
    private PDFBuilder mockedPDFBuilder;

    @Mock
    private Path mockedPath;

    @Captor
    private ArgumentCaptor<Map<String, String>> replacementsCaptor;

    @Before
    public void setUp() throws Exception {

        byte[] bytes = "Test".getBytes();
        Mockito.when(mockedPDFBuilder.renderFromTemplate(Mockito.eq(mockedPath))).thenReturn(bytes);
        Mockito.when(mockedPDFBuilder.renderSearchAndReplaceText(Mockito.any(byte[].class),
                    Mockito.anyMapOf(String.class, String.class)))
            .thenReturn(bytes);
        Mockito.when(mockedPDFBuilder.renderQRCodes(Mockito.any(byte[].class), Mockito.anyListOf(QRSpec.class)))
            .thenReturn(bytes);
    }


    // Text replacement ------------------------------------------------------------------------------------------------

    @Test
    public void ensureThrowsIfReplacementKeyIsNullOrEmpty() {

        assertFailsForReplacementKey(null);
        assertFailsForReplacementKey("");
    }


    private void assertFailsForReplacementKey(String key) {

        try {
            new BuildablePDF(mockedPath, mockedPDFBuilder).withReplacement(key, "foo");
            Assert.fail(String.format("Should fail for search value `%s`", key));
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }


    @Test
    public void ensureThrowsIfReplacementKeyContainsLigature() {

        assertFailsForReplacementKey("fi");
        assertFailsForReplacementKey("truckIdentifier");
        // TODO: add more ligatures here!
    }


    @Test
    public void ensurePDFIsBuiltWithCorrectReplacement() throws RenderException {

        new BuildablePDF(mockedPath, mockedPDFBuilder).withReplacement("foo", "bar").build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 1, replacements.size());
        Assert.assertEquals("Wrong replacement", "bar", replacements.get("foo"));
    }


    @Test
    public void ensurePDFIsBuiltWithCorrectReplacements() throws RenderException {

        new BuildablePDF(mockedPath, mockedPDFBuilder).withReplacement("foo", "bar")
            .withReplacement("search", "replace")
            .build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 2, replacements.size());
        Assert.assertEquals("Wrong replacement", "bar", replacements.get("foo"));
        Assert.assertEquals("Wrong replacement", "replace", replacements.get("search"));
    }


    @Test
    public void ensurePDFIsBuiltWithCorrectReplacementMap() throws RenderException {

        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("foo", "bar");
        replacementMap.put("search", "replace");

        new BuildablePDF(mockedPath, mockedPDFBuilder).withReplacements(replacementMap).build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 2, replacements.size());
        Assert.assertEquals("Wrong replacement", "bar", replacements.get("foo"));
        Assert.assertEquals("Wrong replacement", "replace", replacements.get("search"));
    }


    @Test
    public void ensureThrowsIfReplacementMapContainsKeyThatIsNullOrEmpty() {

        assertFailsForMapWithReplacementKey(null);
        assertFailsForMapWithReplacementKey("");
    }


    private void assertFailsForMapWithReplacementKey(String key) {

        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put(key, "foo");

        try {
            new BuildablePDF(mockedPath, mockedPDFBuilder).withReplacements(replacementMap);

            Assert.fail(String.format("Should fail for map containing search value `%s`", key));
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }


    @Test
    public void ensureThrowsIfReplacementMapContainsKeyWithLigature() {

        assertFailsForMapWithReplacementKey("fi");
        assertFailsForMapWithReplacementKey("truckIdentifier");
        // TODO: add more ligatures here!
    }


    // Multi-line text replacement, see #14181 -------------------------------------------------------------------------

    @Test
    public void ensureMultiLineReplacementThrowsIfTextIsTooLong() {

        Consumer<String> assertFailsForTooLongText = (text) -> {
            try {
                int maxCharsPerLine = 20;

                new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, maxCharsPerLine,
                    "replace0", "replace1");
                Assert.fail(String.format(
                        "Should fail for text with %d characters, but only %d lines with maximum %d characters each",
                        text.length(), maxCharsPerLine, 2));
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        // 60 characters
        assertFailsForTooLongText.accept("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed");

        // 51 characters
        assertFailsForTooLongText.accept("Loremipsumdolorsitametconsetetursadipscingelitrsed");
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureMultiLineReplacementThrowsIfNoSearchValueGiven() {

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement("Lorem ipsum", 10);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureMultiLineReplacementThrowsIfOnlyOneSearchValueGiven() {

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement("Lorem ipsum", 10, "replace0");
    }


    @Test
    public void ensureMultiLineReplacementThrowsForNotPositiveMaximumCharactersNumber() {

        Consumer<Integer> assertFailsForInvalidMaximumCharacters = (max) -> {
            try {
                new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement("Lorem ipsum", max, "replace0",
                    "replace1");

                Assert.fail("Should fail for maximum characters per line: " + max);
            } catch (IllegalArgumentException ex) {
                // Expected
            }
        };

        assertFailsForInvalidMaximumCharacters.accept(-1);
        assertFailsForInvalidMaximumCharacters.accept(0);
    }


    @Test
    public void ensureMultiLineReplacementWorksWithNullText() throws RenderException {

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(null, 20, "replace0", "replace1")
            .build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 2, replacements.size());

        Assert.assertEquals("Wrong replacement for first line", "", replacements.get("replace0"));
        Assert.assertEquals("Wrong replacement for second line", "", replacements.get("replace1"));
    }


    @Test
    public void ensureMultiLineReplacementWorksWithEmptyText() throws RenderException {

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement("", 20, "replace0", "replace1").build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 2, replacements.size());

        Assert.assertEquals("Wrong replacement for first line", "", replacements.get("replace0"));
        Assert.assertEquals("Wrong replacement for second line", "", replacements.get("replace1"));
    }


    @Test
    public void ensureMultiLineReplacementFillsAllPlaceholdersIfTextFitsCompletely() throws RenderException {

        // 28 characters
        String text = "Lucy in the sky with diamonds";

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, 20, "replace0", "replace1")
            .build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 2, replacements.size());

        Assert.assertEquals("Wrong replacement for first line", "Lucy in the sky with", replacements.get("replace0"));
        Assert.assertEquals("Wrong replacement for second line", "diamonds", replacements.get("replace1"));
    }


    @Test
    public void ensureMultiLineReplacementFillsNotAllPlaceholdersIfTextIsShortEnoughFillingFromTopPerDefault()
        throws RenderException {

        // 28 characters
        String text = "Lucy in the sky with diamonds";

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, 20, "replace0", "replace1",
            "replace2", "replace3")
            .build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 4, replacements.size());

        Assert.assertEquals("Wrong replacement for first line", "Lucy in the sky with", replacements.get("replace0"));
        Assert.assertEquals("Wrong replacement for second line", "diamonds", replacements.get("replace1"));
        Assert.assertEquals("Wrong replacement for third line", "", replacements.get("replace2"));
        Assert.assertEquals("Wrong replacement for fourth line", "", replacements.get("replace3"));
    }


    @Test
    public void ensureMultiLineReplacementFillsNotAllPlaceholdersIfTextIsShortEnoughFillingFromTop()
        throws RenderException {

        // 28 characters
        String text = "Lucy in the sky with diamonds";

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, 20,
            BuildablePDF.MultiLineTextFillMode.TOP, "replace0", "replace1", "replace2", "replace3")
            .build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 4, replacements.size());

        Assert.assertEquals("Wrong replacement for first line", "Lucy in the sky with", replacements.get("replace0"));
        Assert.assertEquals("Wrong replacement for second line", "diamonds", replacements.get("replace1"));
        Assert.assertEquals("Wrong replacement for third line", "", replacements.get("replace2"));
        Assert.assertEquals("Wrong replacement for fourth line", "", replacements.get("replace3"));
    }


    @Test
    public void ensureMultiLineReplacementFillsNotAllPlaceholdersIfTextIsShortEnoughFillingFromBottom()
        throws RenderException {

        // 28 characters
        String text = "Lucy in the sky with diamonds";

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, 12,
            BuildablePDF.MultiLineTextFillMode.BOTTOM, "replace0", "replace1", "replace2", "replace3")
            .build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 4, replacements.size());

        Assert.assertEquals("Wrong replacement for first line", "", replacements.get("replace0"));
        Assert.assertEquals("Wrong replacement for second line", "Lucy in the", replacements.get("replace1"));
        Assert.assertEquals("Wrong replacement for third line", "sky with", replacements.get("replace2"));
        Assert.assertEquals("Wrong replacement for fourth line", "diamonds", replacements.get("replace3"));
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureMultiReplacementThrowsIfTextDoesNotFitDueToLengthOfWords() {

        // 60 characters - is fitting in theory to 3 placeholder à max. 20 characters
        String text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed";
        // Would be result in following:
        // 1st line: `Lorem ipsum dolor`
        // 2nd line: `sit amet, consetetur`
        // 3rd line: `sadipscing elitr,`
        // --> last word `sed` would be missing, so ensure that exception is thrown

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, 20, "replace0", "replace1",
            "replace2");
    }
}
