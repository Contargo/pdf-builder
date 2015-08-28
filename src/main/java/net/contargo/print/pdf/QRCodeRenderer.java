package net.contargo.print.pdf;

/**
 * Provides QR-code rendering and generation capabilities.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public interface QRCodeRenderer {

    /**
     * Render a QR code image from the given code with the specified width and height.
     *
     * @param  code  to render
     * @param  size  of the rendered image (square)
     *
     * @return  the rendered image as an image byte array
     *
     * @throws  RenderException  in case a failure occurs during rendering
     */
    byte[] render(String code, int size) throws RenderException;
}
