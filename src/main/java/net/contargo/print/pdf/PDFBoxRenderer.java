package net.contargo.print.pdf;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFOperator;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Path;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;


/**
 * A PDF renderer implementation using the Apache PDFBox project.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @author  Slaven Travar - slaven.travar@pta.de
 * @since  0.1
 */
public class PDFBoxRenderer implements PDFRenderer {

    private static final String ISO_8859_1 = "ISO-8859-1";

    // http://partners.adobe.com/public/developer/en/pdf/PDFReference.pdf
    private static final String SHOW_STRING_OP = "Tj";
    private static final String SHOW_MORE_STRINGS_OP = "TJ";

    @Override
    public byte[] renderFromTemplate(Path template) throws RenderException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try(PDDocument doc = PDDocument.load(template.toFile())) {
            doc.save(out);
        } catch (COSVisitorException | IOException e) {
            throw new RenderException("Parsing the template failed.", e);
        }

        return out.toByteArray();
    }


    @Override
    public byte[] renderFromTemplate(InputStream template) throws RenderException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try(PDDocument doc = PDDocument.load(template)) {
            doc.save(out);
        } catch (COSVisitorException | IOException e) {
            throw new RenderException("Parsing the template failed.", e);
        }

        return out.toByteArray();
    }


    @SuppressWarnings("unchecked")
    @Override
    public byte[] renderSearchAndReplaceText(byte[] pdf, Map<String, String> texts) throws RenderException {

        ByteArrayInputStream documentIn = new ByteArrayInputStream(pdf);
        ByteArrayOutputStream documentOut = new ByteArrayOutputStream();

        try(PDDocument doc = PDDocument.load(documentIn)) {
            List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
            parsePages(texts, doc, pages);
            doc.save(documentOut);
        } catch (IOException | COSVisitorException e) {
            throw new RenderException("Search and replace PDF text failed.", e);
        }

        return documentOut.toByteArray();
    }


    private void parsePages(Map<String, String> texts, PDDocument doc, List<PDPage> pages) throws IOException {

        for (PDPage page : pages) {
            parsePage(texts, doc, page);
        }
    }


    private void parsePage(Map<String, String> texts, PDDocument doc, PDPage page) throws IOException {

        PDFStreamParser parser = new PDFStreamParser(page.getContents());
        parser.parse();

        List<?> tokens = parser.getTokens();
        updateTokens(texts, tokens);

        PDStream updatedStream = new PDStream(doc);

        try(OutputStream out = updatedStream.createOutputStream()) {
            ContentStreamWriter tokenWriter = new ContentStreamWriter(out);
            tokenWriter.writeTokens(tokens);
            page.setContents(updatedStream);
        }
    }


    private void updateTokens(Map<String, String> texts, List<?> tokens) throws IOException {

        Object previous = null;

        for (Object current : tokens) {
            if (current instanceof PDFOperator) {
                updateToken(texts, previous, (PDFOperator) current);
            }

            previous = current;
        }
    }


    private void updateToken(Map<String, String> texts, Object args, PDFOperator operator) throws IOException {

        if (args == null) {
            return;
        }

        String operation = operator.getOperation();

        if (SHOW_STRING_OP.equals(operation)) {
            searchAndReplaceInCOSString(texts, (COSString) args);
        } else if (SHOW_MORE_STRINGS_OP.equals(operation)) {
            searchAndReplaceInCOSArray(texts, (COSArray) args);
        }
    }


    private void searchAndReplaceInCOSString(Map<String, String> texts, COSString cosString) throws IOException {

        String string = cosString.getString();
        String result = searchAndReplace(texts, string);
        cosString.reset();
        cosString.append(result.getBytes(ISO_8859_1));
    }


    private String searchAndReplace(Map<String, String> texts, String orig) {

        String result = orig;

        for (Entry<String, String> e : texts.entrySet()) {
            // escape any rouge backslashes in value, since the replacement fails on any unknown/missing escaped
            // characters following the slash - that way slashes are replaced literally - see bug #13987
            String value = e.getValue() == null ? null : e.getValue().replace("\\", "\\\\");
            result = result.replaceAll(e.getKey(), value);
        }

        return result;
    }


    private void searchAndReplaceInCOSArray(Map<String, String> texts, COSArray cosArray) throws IOException {

        String string = StreamSupport.stream(cosArray.spliterator(), false)
            .filter(e -> e instanceof COSString)
            .map(s -> ((COSString) s).getString())
            .collect(Collectors.joining());

        String result = searchAndReplace(texts, string);
        COSString cosString = new COSString(result.getBytes(ISO_8859_1));
        cosArray.clear();
        cosArray.add(cosString);
    }


    @Override
    public byte[] renderQRCodes(byte[] pdf, List<QRCode> codes) throws RenderException {

        ByteArrayInputStream documentIn = new ByteArrayInputStream(pdf);
        ByteArrayOutputStream documentOut = new ByteArrayOutputStream();

        try(PDDocument document = PDDocument.load(documentIn)) {
            PDDocumentCatalog documentCatalog = document.getDocumentCatalog();
            @SuppressWarnings("unchecked")
            List<PDPage> pages = documentCatalog.getAllPages();

            if (pages.size() > 1) {
                throw new IllegalStateException("Cannot add QR code to document with more pages than 1.");
            }

            PDPage page = pages.iterator().next();
            PDRectangle rectangle = page.getMediaBox();

            try(PDPageContentStream contentStream = new PDPageContentStream(document, page, true, false)) {
                for (QRCode qr : codes) {
                    addQRCode(document, rectangle, contentStream, qr);
                }
            }

            document.save(documentOut);
        } catch (IOException | COSVisitorException e) {
            throw new RenderException("Rendering QR-codes in PDF failed.", e);
        }

        return documentOut.toByteArray();
    }


    private void addQRCode(PDDocument document, PDRectangle rectangle, PDPageContentStream contentStream, QRCode qrCode)
        throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        qrCode.save(out);

        InputStream in = new ByteArrayInputStream(out.toByteArray());
        BufferedImage imageBuffer = ImageIO.read(in);
        PDXObjectImage image = new PDPixelMap(document, imageBuffer);

        float x = calculateCoordinate(qrCode.getX(), rectangle.getWidth(), image.getWidth());
        float y = calculateCoordinate(qrCode.getY(), rectangle.getHeight(), image.getHeight());

        contentStream.drawXObject(image, x, y, image.getWidth(), image.getHeight());
    }


    private float calculateCoordinate(int position, float pageBounds, int imageSize) {

        // Negative positioning means flipped offset from other side of page
        if (position < 0) {
            return pageBounds - (Math.abs(position) + imageSize);
        }

        return position;
    }
}
