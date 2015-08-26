package net.contargo.print.pdf.pdfbox;

import net.contargo.print.pdf.PDFEngine;

import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Path;

import java.util.List;
import java.util.Map;


/**
 * An engine implementation using the Apache PDFBox project.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public class PDFBoxEngine implements PDFEngine {

    @SuppressWarnings("unchecked")
    @Override
    public byte[] searchAndReplaceText(Path path, Map<String, String> texts) {

        byte[] result = new byte[0];

        try(PDDocument doc = PDDocument.load(path.toFile())) {
            PDDocumentCatalog documentCatalog = doc.getDocumentCatalog();
            List<PDPage> pages = documentCatalog.getAllPages();

            for (PDPage page : pages) {
                PDFStreamParser parser = new PDFStreamParser(page.getContents());
                parser.parse();

                List<?> tokens = parser.getTokens();
                Object previousToken = null;

                for (Object currentToken : tokens) {
                    if (currentToken instanceof PDFOperator) {
                        PDFOperator op = (PDFOperator) currentToken;

                        if ("Tj".equals(op.getOperation()) && previousToken != null) {
                            COSString cosString = ((COSString) previousToken);
                            String text = cosString.getString();

                            for (Map.Entry<String, String> placeholderValue : texts.entrySet()) {
                                text = text.replaceAll(placeholderValue.getKey(), placeholderValue.getValue());
                            }

                            cosString.reset();
                            cosString.append(text.getBytes("ISO-8859-1"));
                        }
                    }

                    previousToken = currentToken;
                }

                PDStream updatedStream = new PDStream(doc);
                OutputStream out = updatedStream.createOutputStream();
                ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
                tokenWriter.writeTokens(tokens);
                page.setContents(updatedStream);
            }

            ByteArrayOutputStream documentOut = new ByteArrayOutputStream();
            doc.save(documentOut);

            result = documentOut.toByteArray();
        } catch (IOException | COSVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result.clone();
    }


    /**
     * Replace all occurrences of placeholders in the given text by values defined in the given placeholder map.
     * Placeholders contained in the text for which no corresponding value is mapped are left alone.
     *
     * @param  text
     * @param  placeholderValues
     *
     * @return
     */
    private String replaceAll(String text, Map<String, String> placeholderValues) {

        String result = text;

        for (Map.Entry<String, String> placeholderValue : placeholderValues.entrySet()) {
            result = result.replaceAll(placeholderValue.getKey(), placeholderValue.getValue());
        }

        return result;
    }
}
