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


    // Multi-line text replacement -------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureMultiLineReplacementThrowsIfTextIsTooLong() throws RenderException {

        // 60 characters
        String text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed";

        // but only 2 lines à 20 characters
        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, 20, "replace0", "replace1")
            .build();
    }


    @Test
    public void ensurePDFIsBuiltWithCorrectMultiLineReplacementFittingText() throws RenderException {

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
    public void ensurePDFIsBuiltWithCorrectMultiLineReplacementWhenTextContainsNumbers() throws RenderException {

        // 36 characters
        String text = "Kto 100 255 700 700, BLZ 480 800 00";

        new BuildablePDF(mockedPath, mockedPDFBuilder).withMultiLineReplacement(text, 20, "replace0", "replace1")
            .build();

        Mockito.verify(mockedPDFBuilder)
            .renderSearchAndReplaceText(Matchers.any(byte[].class), replacementsCaptor.capture());

        Map<String, String> replacements = replacementsCaptor.getValue();
        Assert.assertEquals("Wrong amount of replacements", 2, replacements.size());

        Assert.assertEquals("Wrong replacement for first line", "Kto 100 255 700 700,", replacements.get("replace0"));
        Assert.assertEquals("Wrong replacement for second line", "BLZ 480 800 00", replacements.get("replace1"));
    }
}
