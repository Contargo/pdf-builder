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
     * @see  PDFRenderer#renderFromTemplate(Path)
     */
    protected byte[] renderFromTemplate(Path template) throws IOException {

        ASSERT_NOT_NULL.accept("template", template);

        return pdfRenderer.renderFromTemplate(template);
    }


    /**
     * Delegates to the PDF renderer.
     *
     * @see  PDFRenderer#renderSearchAndReplaceText(byte[], Map)
     */
    protected byte[] renderSearchAndReplaceText(byte[] pdf, Map<String, String> texts) {

        ASSERT_NOT_NULL.accept("pdf", pdf);
        ASSERT_NOT_NULL.accept("texts", texts);

        return pdfRenderer.renderSearchAndReplaceText(pdf, texts);
    }


    /**
     * Delegates to QR-code and PDF renderers.
     */
    protected byte[] renderQRCodes(byte[] pdf, List<QRSpec> specs) {

        ASSERT_NOT_NULL.accept("pdf", pdf);
        ASSERT_NOT_NULL.accept("specs", specs);

        List<QRCode> codes = specs.stream().map(s -> s.render(qrRenderer)).collect(Collectors.toList());

        return pdfRenderer.renderQRCodes(pdf, codes);
    }

    protected static final class QRCode {

        private final byte[] data;
        private final int x;
        private final int y;

        public QRCode(byte[] data, int x, int y) {

            this.data = data;
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

        public PDFDocument build() throws IOException {

            byte[] pdf;

            pdf = builder.renderFromTemplate(template);
            pdf = builder.renderSearchAndReplaceText(pdf, replacements);
            pdf = builder.renderQRCodes(pdf, qrCodes);

            return new PDFDocument(pdf);
        }


        public BuildablePDF withReplacement(String search, String replace) {

            replacements.put(search, replace);

            return this;
        }


        public BuildablePDF withReplacements(Map<String, String> replacements) {

            this.replacements.putAll(replacements);

            return this;
        }


        public BuildablePDF withQRCode(QRSpec qrSpec) {

            this.qrCodes.add(qrSpec);

            return this;
        }
    }

    public static final class QRSpec {

        private final String code;

        private int x;
        private int y;
        private int size;

        private QRSpec(String code) {

            this.code = code;
            this.x = 0;
            this.y = 0;
            this.size = 125;
        }

        public static QRSpec fromCode(String code) {

            return new QRSpec(code);
        }


        private QRCode render(QRCodeRenderer renderer) {

            byte[] qrCode = renderer.render(code, size);

            return new QRCode(qrCode, x, y);
        }


        public QRSpec withPositionY(int y) {

            this.y = y;

            return this;
        }


        public QRSpec withPositionX(int x) {

            this.x = x;

            return this;
        }


        public QRSpec withSize(int size) {

            this.size = size;

            return this;
        }


        public QRSpec withPosition(int x, int y) {

            this.x = x;
            this.y = y;

            return this;
        }
    }

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
