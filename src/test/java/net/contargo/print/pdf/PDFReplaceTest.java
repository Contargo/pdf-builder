package net.contargo.print.pdf;

import org.junit.Assert;
import org.junit.Test;

import org.mockito.Mockito;

import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;


/**
 * @author  Olle Törnström - toernstroem@synyx.de
 */
public class PDFReplaceTest {

    @Test
    public void ensureHasPublicStaticApiForSimpleSearchAndReplace() {

        Path nonNullPath = Mockito.mock(Path.class);
        Map<String, String> nonNullTextMap = new HashMap<>();

        byte[] result = PDFReplace.searchAndReplaceText(nonNullPath, nonNullTextMap);

        Assert.assertNotNull("Must not be null", result);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureSearchAndReplaceTextRequiresPathParameter() {

        PDFReplace.searchAndReplaceText(null, new HashMap<>());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureSearchAndReplaceTextRequiresTextsParameter() {

        PDFReplace.searchAndReplaceText(Mockito.mock(Path.class), null);
    }
}
