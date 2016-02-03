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
}
