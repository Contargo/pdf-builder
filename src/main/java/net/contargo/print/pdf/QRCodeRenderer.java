package net.contargo.print.pdf;

public interface QRCodeRenderer {

    /**
     * Render a QR code image from the given code with the specified width and height.
     *
     * @param  code  to render
     * @param  width  of the rendered image
     * @param  height  of the rendered image
     *
     * @return  the rendered image as an image byte array
     */
    byte[] render(String code, int width, int height);
}
