package net.contargo.print.pdf;

import java.nio.file.Path;

import java.util.Map;


/**
 * Provides PDF creation, interpolation and rendering capabilities.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public interface PDFEngine {

    /**
     * Performs text interpolation using a map of search-replace pairs, on a PDF-template file, specified by the given
     * path.
     *
     * @param  path  to the PDF template file
     * @param  texts  map of search-replace pairs
     *
     * @return  the interpolated PDF file as a byte array
     */
    byte[] searchAndReplaceText(Path path, Map<String, String> texts);
}
