package net.contargo.print.pdf;

import net.contargo.print.pdf.pdfbox.PDFBoxEngine;

import java.nio.file.Path;

import java.util.Map;
import java.util.function.BiConsumer;


/**
 * Main class for the PDF replacement tool, providing static utility methods to users.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public final class PDFReplace {

    private final PDFEngine engine;

    /**
     * Creates a new instance, with the given engine.
     *
     * @param  engine  implementation to delegate work to
     */
    private PDFReplace(PDFEngine engine) {

        this.engine = engine;
    }

    /**
     * Constructs a PDF replace instance, with a default, pre-defined PDF engine (currently Apache PDFBox).
     *
     * @return  a new instance
     */
    public static PDFReplace newInstance() {

        return newInstanceWithEngine(new PDFBoxEngine());
    }


    /**
     * Constructs a new PDF replace instance, using the given PDF engine.
     *
     * @param  engine  implementation to use
     *
     * @return  a new instance
     */
    public static PDFReplace newInstanceWithEngine(PDFEngine engine) {

        return new PDFReplace(engine);
    }


    /**
     * Searches and replaces texts in the given file based on the provided map of search-replace pairs.
     *
     * @param  path  path of the file to interpolate the given texts with
     * @param  texts  map of search-replace pairs, to interpolate
     *
     * @return  the interpolated result as a byte array, never {@code null}
     */
    public byte[] searchAndReplaceText(Path path, Map<String, String> texts) {

        BiConsumer<String, Object> assertNotNull = (String n, Object o) -> {
            if (o == null) {
                throw new IllegalArgumentException(String.format("The %s must not be null", n));
            }
        };

        assertNotNull.accept("path", path);
        assertNotNull.accept("texts", texts);

        return engine.searchAndReplaceText(path, texts);
    }
}
