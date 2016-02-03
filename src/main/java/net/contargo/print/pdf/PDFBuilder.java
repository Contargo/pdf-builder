package net.contargo.print.pdf;

import java.io.InputStream;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;


/**
 * A PDF utility with a simple and intuitive public API for PDF document generation.
 *
 * <p>This builder aims to provide a high-level practical syntax, for quick and easy PDF document generation.</p>
 *
 * <p>Below is a simple usage example:</p>
 *
 * <pre>
   Path docs = FileSystems.getDefault().getPath("documents");
   Path template = docs.resolve("template.pdf");
   Path result = docs.resolve("result.pdf");

   PDFBuilder.fromTemplate(template)
       .withReplacement("@name@", name)
       .withReplacement("@email@", email)
       .withQRCode(QRSpec.fromCode(code).withPosition(20, 50).withSize(145))
       .build()
       .save(result);
 * </pre>
 *
 * <p>Instead of {@link Path} also {@link InputStream} can be used as input.</p>
 *
 * <pre>
   Path template = this.getClass().getResourceAsStream("/documents/template.pdf");
 * </pre>
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @author  Slaven Travar - slaven.travar@pta.de
 * @since  0.1
 */
public final class PDFBuilder {

    private static final BiConsumer<String, Object> ASSERT_NOT_NULL = (String n, Object o) -> {
        if (o == null) {
            throw new IllegalArgumentException(String.format("The %s must not be null", n));
        }
    };

    private final PDFRenderer pdfRenderer;
    private final QRCodeRenderer qrRenderer;

    protected PDFBuilder(PDFRenderer pdfRenderer, QRCodeRenderer qrRenderer) {

        this.pdfRenderer = pdfRenderer;
        this.qrRenderer = qrRenderer;
    }

    /**
     * Returns a builder for PDF documents, based on a given template path.
     *
     * @param  template  path to base the builder on, never {@code null}
     *
     * @return  a new builder instance
     */
    public static BuildablePDF fromTemplate(Path template) {

        ASSERT_NOT_NULL.accept("template", template);

        PDFBuilder builder = new PDFBuilder(new PDFBoxRenderer(), new QRGenRenderer());

        return new BuildablePDF(template, builder);
    }


    /**
     * Returns a builder for PDF documents, based on a given template input stream.
     *
     * @param  template  input stream to base the builder on, never {@code null}
     *
     * @return  a new builder instance
     *
     * @since  0.2
     */
    public static BuildablePDF fromTemplate(InputStream template) {

        ASSERT_NOT_NULL.accept("template", template);

        PDFBuilder builder = new PDFBuilder(new PDFBoxRenderer(), new QRGenRenderer());

        return new BuildablePDF(template, builder);
    }


    /**
     * Delegates to the PDF renderer.
     *
     * @param  template  to render from
     *
     * @return  the rendered PDF document as a byte array
     *
     * @throws  RenderException  in case rendering fails
     *
     * @see  PDFRenderer#renderFromTemplate(Path)
     */
    protected byte[] renderFromTemplate(Path template) throws RenderException {

        ASSERT_NOT_NULL.accept("template", template);

        return pdfRenderer.renderFromTemplate(template);
    }


    /**
     * Delegates to the PDF renderer.
     *
     * @param  template  to render from
     *
     * @return  the rendered PDF document as a byte array
     *
     * @throws  RenderException  in case rendering fails
     *
     * @see  PDFRenderer#renderFromTemplate(InputStream)
     */
    protected byte[] renderFromTemplate(InputStream template) throws RenderException {

        ASSERT_NOT_NULL.accept("template", template);

        return pdfRenderer.renderFromTemplate(template);
    }


    /**
     * Delegates to the PDF renderer.
     *
     * @param  pdf  document as byte array
     * @param  text  map of search-replace pairs
     *
     * @return  the PDF document as a byte array
     *
     * @throws  RenderException  in case rendering fails
     *
     * @see  PDFRenderer#renderSearchAndReplaceText(byte[], Map)
     */
    protected byte[] renderSearchAndReplaceText(byte[] pdf, Map<String, String> text) throws RenderException {

        ASSERT_NOT_NULL.accept("pdf", pdf);
        ASSERT_NOT_NULL.accept("text", text);

        return pdfRenderer.renderSearchAndReplaceText(pdf, text);
    }


    /**
     * Delegates to QR-code and PDF renderers.
     *
     * @param  pdf  document as byte array
     * @param  specs  list of QR-code specifications to render and add
     *
     * @return  the PDF document as a byte array
     *
     * @throws  RenderException  in case rendering fails
     *
     * @see  QRCodeRenderer#render(String, int)
     * @see  PDFRenderer#renderQRCodes(byte[], List)
     */
    protected byte[] renderQRCodes(byte[] pdf, List<QRSpec> specs) throws RenderException {

        ASSERT_NOT_NULL.accept("pdf", pdf);
        ASSERT_NOT_NULL.accept("specs", specs);

        List<QRCode> codes = new ArrayList<>();

        // No stream operation, the QRRenderer throws a checked exception.
        for (QRSpec spec : specs) {
            codes.add(spec.render(qrRenderer));
        }

        return pdfRenderer.renderQRCodes(pdf, codes);
    }
}
