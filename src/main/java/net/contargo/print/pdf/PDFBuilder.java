package net.contargo.print.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
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

    /**
     * A rendered QR code, providing it's byte array and position information.
     */
    protected static final class QRCode {

        private final byte[] data;
        private final int x;
        private final int y;

        public QRCode(byte[] data, int x, int y) {

            this.data = data.clone();
            this.x = x;
            this.y = y;
        }

        public int getX() {

            return x;
        }


        public int getY() {

            return y;
        }


        public void save(OutputStream output) throws IOException {

            output.write(data);
        }
    }

    /**
     * A chaining API for specifying a PDF to build.
     */
    public static final class BuildablePDF {

        private final PDFBuilder builder;
        private Path templateAsPath;
        private InputStream templateAsStream;
        private final Map<String, String> replacements;
        private final List<QRSpec> qrCodes;

        public BuildablePDF(Path template, PDFBuilder builder) {

            this.builder = builder;
            this.templateAsPath = template;
            this.replacements = new HashMap<>();
            this.qrCodes = new ArrayList<>();
        }


        public BuildablePDF(InputStream template, PDFBuilder builder) {

            this.builder = builder;
            this.templateAsStream = template;
            this.replacements = new HashMap<>();
            this.qrCodes = new ArrayList<>();
        }

        /**
         * Builds a PDF from this builder.
         *
         * @return  the built PDF document
         *
         * @throws  RenderException  in case rendering fails, describing the originating cause of failure
         */
        public PDFDocument build() throws RenderException {

            byte[] pdf;

            pdf = templateAsPath != null ? builder.renderFromTemplate(templateAsPath)
                                         : builder.renderFromTemplate(templateAsStream);

            pdf = builder.renderSearchAndReplaceText(pdf, replacements);
            pdf = builder.renderQRCodes(pdf, qrCodes);

            return new PDFDocument(pdf);
        }


        /**
         * Add a search-replace pair to this builder.
         *
         * @param  search  string to search for
         * @param  replace  string to replace with
         *
         * @return  this builder for chaining
         */
        public BuildablePDF withReplacement(String search, String replace) {

            this.replacements.put(search, replace);

            return this;
        }


        /**
         * Adds a map of search-replace pairs to this builder.
         *
         * @param  replacements  map of string pairs to search and replace with
         *
         * @return  this builder for chaining
         */
        public BuildablePDF withReplacements(Map<String, String> replacements) {

            this.replacements.putAll(replacements);

            return this;
        }


        /**
         * Add a QR code specification to this builder.
         *
         * @param  qrSpec  describing the QR code to add
         *
         * @return  this builder for chaining
         */
        public BuildablePDF withQRCode(QRSpec qrSpec) {

            this.qrCodes.add(qrSpec);

            return this;
        }


        public BuildablePDF withMultiLineReplacement(String text, int maxCharactersPerLine, String... replace) {

            for (String replaceValue : replace) {
                this.replacements.put(replaceValue, text);
            }

            return this;
        }
    }

    /**
     * Describing a QR code to render.
     */
    public static final class QRSpec {

        private final String code;

        private int x;
        private int y;
        private int size;

        private QRSpec(String code) {

            this.code = code;
            this.x = 0;
            this.y = 0;
            this.size = 125; // NOSONAR
        }

        /**
         * Creates a new default specification with the given code.
         *
         * @param  code  for the QR-graphics
         *
         * @return  this specification for chaining
         */
        public static QRSpec fromCode(String code) {

            return new QRSpec(code);
        }


        private QRCode render(QRCodeRenderer renderer) throws RenderException {

            byte[] qrCode = renderer.render(code, size);

            return new QRCode(qrCode, x, y);
        }


        /**
         * Set the y position for this specification.
         *
         * @param  y  position
         *
         * @return  this specification for chaining
         */
        public QRSpec withPositionY(int y) {

            this.y = y;

            return this;
        }


        /**
         * Set the x position for this specification.
         *
         * @param  x  position
         *
         * @return  this specification for chaining
         */
        public QRSpec withPositionX(int x) {

            this.x = x;

            return this;
        }


        /**
         * Set the x and y position of this specification.
         *
         * @param  x  position
         * @param  y  position
         *
         * @return  this specification for chaining
         */
        public QRSpec withPosition(int x, int y) {

            this.x = x;
            this.y = y;

            return this;
        }


        /**
         * Set the size of this specification.
         *
         * @param  size  to set
         *
         * @return  this specification for chaining
         */
        public QRSpec withSize(int size) {

            this.size = size;

            return this;
        }
    }

    /**
     * A rendered PDF document.
     */
    public static final class PDFDocument {

        private final byte[] data;

        public PDFDocument(byte[] data) {

            this.data = data.clone();
        }

        public void save(Path target) throws IOException {

            Files.write(target, data);
        }


        public void save(OutputStream output) throws IOException {

            output.write(data);
        }
    }
}
