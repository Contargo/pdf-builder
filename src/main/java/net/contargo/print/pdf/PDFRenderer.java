package net.contargo.print.pdf;

import net.contargo.print.pdf.PDFBuilder.QRCode;

import java.io.InputStream;

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
     * @param  template  the input stream to consume
     *
     * @return  a PDF document as a byte array
     *
     * @throws  RenderException  in case a failure occurs during rendering
     */
    byte[] renderFromTemplate(InputStream template) throws RenderException;


    /**
     * Performs text interpolation using a map of search-replace pairs, on a PDF-template file, specified by the given
     * path.
     *
     * @param  pdf  document byte array, to interpolate in. Must not be copied or detached, since it is assumed that the
     *              renderer is part of a internal process.
     * @param  text  map of search-replace pairs
     *
     * @return  the interpolated PDF file as a byte array
     *
     * @throws  RenderException  in case a failure occurs during rendering
     */
    byte[] renderSearchAndReplaceText(byte[] pdf, Map<String, String> text) throws RenderException;


    /**
     * Renders the list of given QR codes into the provided pdf document.
     *
     * @param  pdf  document byte array, to render on. Must not necessarily be copied since it is assumed that the
     *              renderer is only part of an internal process.
     * @param  codes  to render into the document
     *
     * @return  the changed PDF document byte array
     *
     * @throws  RenderException  in case a failure occurs during rendering
     */
    byte[] renderQRCodes(byte[] pdf, List<QRCode> codes) throws RenderException;
}
