package net.contargo.print.pdf;

import org.apache.pdfbox.cos.COSArray;
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
import java.io.UnsupportedEncodingException;

import java.nio.file.Path;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


/**
 * A PDF renderer implementation using the Apache PDFBox project.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public class PDFBoxRenderer implements PDFRenderer {

    // http://partners.adobe.com/public/developer/en/pdf/PDFReference.pdf
    private static final String SHOW_STRING_OP = "Tj";
    private static final String SHOW_MORE_STRINGS_OP = "TJ";

    @SuppressWarnings("unchecked")
    @Override
    public byte[] searchAndReplaceText(Path path, Map<String, String> texts) {

        ByteArrayOutputStream documentOut = new ByteArrayOutputStream();

        try(PDDocument doc = PDDocument.load(path.toFile())) {
            PDDocumentCatalog documentCatalog = doc.getDocumentCatalog();
            List<PDPage> pages = documentCatalog.getAllPages();

            for (PDPage page : pages) {
                PDFStreamParser parser = new PDFStreamParser(page.getContents());
                parser.parse();

                List<?> tokens = parser.getTokens();

                Object previousToken = null;

                for (Object current : tokens) {
                    if (current instanceof PDFOperator) {
                        PDFOperator op = (PDFOperator) current;
                        String operation = op.getOperation();

                        if (SHOW_STRING_OP.equals(operation) && previousToken != null) {
                            searchAndReplaceCOSString(texts, (COSString) previousToken);
                        } else if (SHOW_MORE_STRINGS_OP.equals(operation) && previousToken != null) {
                            searchAndReplaceCOSArray(texts, (COSArray) previousToken);
                        }
                    }

                    previousToken = current;
                }

                PDStream updatedStream = new PDStream(doc);
                OutputStream out = updatedStream.createOutputStream();
                ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
                tokenWriter.writeTokens(tokens);
                page.setContents(updatedStream);
            }

            doc.save(documentOut);
        } catch (IOException | COSVisitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return documentOut.toByteArray();
    }


    private void searchAndReplaceCOSArray(Map<String, String> texts, COSArray cosArray) throws IOException,
        UnsupportedEncodingException {

        String text = StreamSupport.stream(cosArray.spliterator(), false).filter(e -> e instanceof COSString).map(s ->
                    ((COSString) s).getString()).collect(Collectors.joining());

        COSString cosString = new COSString(text);
        searchAndReplaceCOSString(texts, cosString);
        cosArray.clear();
        cosArray.add(cosString);
    }


    private void searchAndReplaceCOSString(Map<String, String> texts, COSString cosString) throws IOException,
        UnsupportedEncodingException {

        String string = cosString.getString();

        for (Entry<String, String> e : texts.entrySet()) {
            string = string.replaceAll(e.getKey(), e.getValue());
        }

        cosString.reset();
        cosString.append(string.getBytes("ISO-8859-1"));
    }
}
