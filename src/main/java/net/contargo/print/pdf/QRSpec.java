package net.contargo.print.pdf;

/**
 * Describing a QR code to render.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public final class QRSpec {

    private final String code;

    private int x;
    private int y;
    private int size;

    private QRSpec(String code) {

        this.code = code;
        this.x = 0;
        this.y = 0;
        this.size = 125; // NOSONAR
    }

    /**
     * Creates a new default specification with the given code.
     *
     * @param  code  for the QR-graphics
     *
     * @return  this specification for chaining
     */
    public static QRSpec fromCode(String code) {

        return new QRSpec(code);
    }


    QRCode render(QRCodeRenderer renderer) throws RenderException {

        byte[] qrCode = renderer.render(code, size);

        return new QRCode(qrCode, x, y);
    }


    /**
     * Set the y position for this specification.
     *
     * @param  y  position
     *
     * @return  this specification for chaining
     */
    public QRSpec withPositionY(int y) {

        this.y = y;

        return this;
    }


    /**
     * Set the x position for this specification.
     *
     * @param  x  position
     *
     * @return  this specification for chaining
     */
    public QRSpec withPositionX(int x) {

        this.x = x;

        return this;
    }


    /**
     * Set the x and y position of this specification.
     *
     * @param  x  position
     * @param  y  position
     *
     * @return  this specification for chaining
     */
    public QRSpec withPosition(int x, int y) {

        this.x = x;
        this.y = y;

        return this;
    }


    /**
     * Set the size of this specification.
     *
     * @param  size  to set
     *
     * @return  this specification for chaining
     */
    public QRSpec withSize(int size) {

        this.size = size;

        return this;
    }
}
