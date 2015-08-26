package net.contargo.print.pdf;

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

    private PDFReplace() {

        // Hidden
    }

    /**
     * Searches and replaces texts in the given file based on the provided map of search-replace pairs.
     *
     * @param  path  path of the file to interpolate the given texts with
     * @param  texts  map of search-replace pairs, to interpolate
     *
     * @return  the interpolated result as a byte array, never {@code null}
     */
    public static byte[] searchAndReplaceText(Path path, Map<String, String> texts) {

        BiConsumer<String, Object> assertNotNull = (String n, Object o) -> {
            if (o == null) {
                throw new IllegalArgumentException(String.format("The %s must not be null", n));
            }
        };

        assertNotNull.accept("path", path);
        assertNotNull.accept("texts", texts);

        return new byte[0];
    }
}
