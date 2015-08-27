package net.contargo.print.pdf;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


/**
 * A utility tool for PDF creation and manipulation.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
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
     * @param  template  path to base the builder of, never {@code null}
     *
     * @return  a new builder instance
     */
    public static BuildablePDF fromTemplate(Path template) {

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
     * @throws  IOException  in case rendering fails
     *
     * @see  PDFRenderer#renderFromTemplate(Path)
     */
    protected byte[] renderFromTemplate(Path template) throws IOException {

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
     * @see  PDFRenderer#renderSearchAndReplaceText(byte[], Map)
     */
    protected byte[] renderSearchAndReplaceText(byte[] pdf, Map<String, String> text) {

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
     * @see  QRCodeRenderer#render(String, int)
     * @see  PDFRenderer#renderQRCodes(byte[], List)
     */
    protected byte[] renderQRCodes(byte[] pdf, List<QRSpec> specs) {

        ASSERT_NOT_NULL.accept("pdf", pdf);
        ASSERT_NOT_NULL.accept("specs", specs);

        List<QRCode> codes = specs.stream().map(s -> s.render(qrRenderer)).collect(Collectors.toList());

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
        private final Path template;
        private final Map<String, String> replacements;
        private final List<QRSpec> qrCodes;

        public BuildablePDF(Path template, PDFBuilder builder) {

            this.builder = builder;
            this.template = template;
            this.replacements = new HashMap<>();
            this.qrCodes = new ArrayList<>();
        }

        /**
         * Builds a PDF from this builder.
         *
         * @return  the built PDF document
         *
         * @throws  IOException  in case building failed
         */
        public PDFDocument build() throws IOException {

            byte[] pdf;

            pdf = builder.renderFromTemplate(template);
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

            replacements.put(search, replace);

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


        private QRCode render(QRCodeRenderer renderer) {

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
