package net.contargo.print.pdf;

public interface QRCodeRenderer {

    /**
     * Render a QR code image from the given code with the specified width and height.
     *
     * @param  code  to render
     * @param  size  of the rendered image (square)
     *
     * @return  the rendered image as an image byte array
     */
    byte[] render(String code, int size);
}
