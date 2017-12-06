package net.contargo.print.pdf;

/**
 * Provides QR-code rendering and generation capabilities.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public interface QRCodeRenderer {

    /**
     * Render a QR code image from the given code with the specified width and height, using the default (highest)
     * error correction level.
     *
     * @param  code  to render
     * @param  size  of the rendered image (square)
     *
     * @return  the rendered image as an image byte array
     *
     * @throws  RenderException  in case a failure occurs during rendering
     *
     * @deprecated  Use {@link #render(String, int, int)} instead
     */
    @Deprecated
    byte[] render(String code, int size) throws RenderException;


    /**
     * Render a QR code image from the given code with the specified width and height.
     *
     * @param  code  to render
     * @param  size  of the rendered image (square)
     * @param  level  to use as error correction level, one of the values 7%, 15%, 25% or 30% - representing the
     *                approximate correction percentage for Reed–Solomon error correction
     *
     * @return  the rendered image as an image byte array
     *
     * @throws  RenderException  in case a failure occurs during rendering
     *
     * @since  0.3
     */
    byte[] render(String code, int size, int level) throws RenderException;
}
