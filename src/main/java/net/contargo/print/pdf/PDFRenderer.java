package net.contargo.print.pdf;

import net.contargo.print.pdf.PDFBuilder.QRCode;

import java.io.IOException;

import java.nio.file.Path;

import java.util.List;
import java.util.Map;


/**
 * Provides PDF creation, interpolation and rendering capabilities.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public interface PDFRenderer {

    /**
     * Renders a PDF document byte array, by consuming the given PDF template (document) path.
     *
     * @param  template  path to consume
     *
     * @return  a PDF document as a byte array
     *
     * @throws  IOException  in case there was an error consuming the template
     */
    byte[] renderFromTemplate(Path template) throws IOException;


    /**
     * Performs text interpolation using a map of search-replace pairs, on a PDF-template file, specified by the given
     * path.
     *
     * @param  pdf  document byte array, to interpolate in
     * @param  text  map of search-replace pairs
     *
     * @return  the interpolated PDF file as a byte array
     */
    byte[] renderSearchAndReplaceText(byte[] pdf, Map<String, String> text);


    /**
     * Renders the list of given QR codes into the provided pdf document.
     *
     * @param  pdf  document byte array, to render on
     * @param  codes  to render into the document
     *
     * @return  the changed PDF document byte array
     *
     * @throws  IOException  in case rendering failes
     */
    byte[] renderQRCodes(byte[] pdf, List<QRCode> codes);
}
