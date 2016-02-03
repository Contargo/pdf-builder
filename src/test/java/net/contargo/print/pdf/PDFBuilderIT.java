package net.contargo.print.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URISyntaxException;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.util.function.Function;


/**
 * @author  Olle Törnström - toernstroem@synyx.de
 * @author  Slaven Travar - slaven.travar@pta.de
 */
public class PDFBuilderIT {

    private static final Path RESOURCES = FileSystems.getDefault().getPath("src/test/resources");

    @Test
    public void ensureReplacesTextInPDFUsingPathAsTemplate() throws URISyntaxException, IOException, RenderException {

        Path source = RESOURCES.resolve("foo.pdf");
        Assert.assertTrue("Missing " + source, source.toFile().exists());

        PDFTextStripper textStripper = new PDFTextStripper();

        try(PDDocument sourcePdDocument = PDDocument.load(source.toFile())) {
            String textOnlyBefore = textStripper.getText(sourcePdDocument);
            Assert.assertTrue("Search value `foo` is missing", textOnlyBefore.contains("foo"));
            Assert.assertFalse("Replace value `bar` present before", textOnlyBefore.contains("bar"));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PDFBuilder.fromTemplate(source).withReplacement("foo", "bar").build().save(out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        try(PDDocument targetPdDocument = PDDocument.load(in)) {
            String textOnlyAfter = textStripper.getText(targetPdDocument);

            Assert.assertFalse("Search value `foo` exists after", textOnlyAfter.contains("foo"));
            Assert.assertTrue("Replace value `bar` is missing", textOnlyAfter.contains("bar"));
        }
    }


    @Test
    public void ensureReplacesTextInPDFUsingInputStreamAsTemplate() throws URISyntaxException, IOException,
        RenderException {

        try(InputStream source = this.getClass().getResourceAsStream("/foo.pdf")) {
            Assert.assertTrue("Missing " + source, source != null);

            PDFTextStripper textStripper = new PDFTextStripper();

            try(PDDocument sourcePdDocument = PDDocument.load(source)) {
                String textOnlyBefore = textStripper.getText(sourcePdDocument);
                Assert.assertTrue("Search value `foo` is missing", textOnlyBefore.contains("foo"));
                Assert.assertFalse("Replace value `bar` present before", textOnlyBefore.contains("bar"));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // reload stream, since PDDocument.load() has closed it
            try(InputStream sourceTemplate = this.getClass().getResourceAsStream("/foo.pdf")) {
                PDFBuilder.fromTemplate(sourceTemplate).withReplacement("foo", "bar").build().save(out);

                ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

                try(PDDocument targetPdDocument = PDDocument.load(in)) {
                    String textOnlyAfter = textStripper.getText(targetPdDocument);

                    Assert.assertFalse("Search value `foo` exists after", textOnlyAfter.contains("foo"));
                    Assert.assertTrue("Replace value `bar` is missing", textOnlyAfter.contains("bar"));
                }
            }
        }
    }


    /**
     * Ensure fix for Bug #13987.
     */
    @Test
    public void ensureReplacesTextWithRougeBackslash() throws URISyntaxException, IOException, RenderException {

        final String backslash = new String(new byte[] { 0x5C });

        Path source = RESOURCES.resolve("foo.pdf");
        Assert.assertTrue("Missing " + source, source.toFile().exists());

        PDFTextStripper textStripper = new PDFTextStripper();

        try(PDDocument sourcePdDocument = PDDocument.load(source.toFile())) {
            String textOnlyBefore = textStripper.getText(sourcePdDocument);
            Assert.assertTrue("Search value `foo` is missing", textOnlyBefore.contains("foo"));
            Assert.assertFalse("Replace value `bar\\` present before", textOnlyBefore.contains("bar" + backslash));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PDFBuilder.fromTemplate(source).withReplacement("foo", "bar" + backslash).build().save(out);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        try(PDDocument targetPdDocument = PDDocument.load(in)) {
            String textOnlyAfter = textStripper.getText(targetPdDocument);

            Assert.assertFalse("Search value `foo` exists after", textOnlyAfter.contains("foo"));
            Assert.assertTrue("Replace value `bar` is missing", textOnlyAfter.contains("bar" + backslash));
        }
    }


    /**
     * Allow multi-line replacing, see #14181.
     */
    @Test
    public void ensureMultiLineReplacementWithVeryLongTextReplacesAllThePlaceholders() throws IOException,
        RenderException {

        // Ensure the PDF to execute test with exists
        Path source = RESOURCES.resolve("footer.pdf");
        Assert.assertTrue("Missing " + source, source.toFile().exists());

        // Create the placeholders for the four footer lines
        String[] searchValues = new String[4];

        for (int i = 0; i < 4; i++) {
            searchValues[i] = "FOOTER" + i;
        }

        // Ensure all of the placeholders exist in the PDF
        PDFTextStripper textStripper = new PDFTextStripper();

        try(PDDocument sourcePdDocument = PDDocument.load(source.toFile())) {
            String textOnlyBefore = textStripper.getText(sourcePdDocument);

            for (String searchValue : searchValues) {
                Assert.assertTrue(String.format("Search value `%s` is missing", searchValue),
                    textOnlyBefore.contains(searchValue));
            }
        }

        // Escape the special characters of the placeholders
        Function<String, String> placeholder = (s) -> String.format("\\$\\{%s\\}", s);
        String[] escapedSearchValues = new String[4];

        for (int i = 0; i < 4; i++) {
            escapedSearchValues[i] = placeholder.apply(searchValues[i]);
        }

        // Build a PDF with replaced placeholders
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String text =
            "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut";

        PDFBuilder.fromTemplate(source).withMultiLineReplacement(text, 20, escapedSearchValues).build().save(out);

        // Ensure the placeholders have been replaced successfully
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        try(PDDocument targetPdDocument = PDDocument.load(in)) {
            String textOnlyAfter = textStripper.getText(targetPdDocument);

            Assert.assertTrue("Replace value is missing", textOnlyAfter.contains(text));

            for (String searchValue : searchValues) {
                Assert.assertFalse(String.format("Search value `%s` exists after", searchValue),
                    textOnlyAfter.contains(searchValue));
            }
        }
    }
}
