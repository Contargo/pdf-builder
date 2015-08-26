package net.contargo.print.pdf;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import java.net.URISyntaxException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * @author  Olle Törnström - toernstroem@synyx.de
 */
public class PDFToolIT {

    /*
     * This is, sadly, a very brittle test that does but little to actually prove that
     * the engine implementation does what it is expected to. Let it be said, that it
     * is all done in the context of "PDFBox will be replaced later". Sorry.
     */
    @Test
    public void ensureReplacesTextInPDF() throws URISyntaxException, IOException {

        Path path = FileSystems.getDefault().getPath("src/test/resources", "foo.pdf");
        Assert.assertTrue("Missing " + path, path.toFile().exists());

        byte[] before = Files.readAllBytes(path);

        String barAsChars = "40, 98, 97, 114, 41, 32, 84, 106";
        Assert.assertFalse("Replace value `(bar) Tj` present before", Arrays.toString(before).contains(barAsChars));

        Map<String, String> texts = new HashMap<>();
        texts.put("foo", "bar");

        byte[] result = PDFTool.newInstance().searchAndReplaceText(path, texts);

        Assert.assertNotNull("No results", result);
        Assert.assertTrue("Empty results: " + Arrays.toString(result), result.length > 0);

        Assert.assertTrue("Replace value `(bar) Tj` is missing", Arrays.toString(result).contains(barAsChars));
    }
}
