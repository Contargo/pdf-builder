package net.contargo.print.pdf;

/**
 * Describing a QR code to render.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public final class QRSpec {

    /**
     * The available correction levels to use, as defined by the standard
     * https://en.wikipedia.org/wiki/QR_code#Error_correction.
     */
    public enum Level {

        Low(7),
        Medium(15),
        Quartile(25),
        High(30);

        int val;

        Level(int val) {

            this.val = val;
        }
    }

    private final String code;

    private int x;
    private int y;
    private int size;
    private Level level;
    private boolean margin;

    private QRSpec(String code) {

        this.code = code;
        this.x = 0;
        this.y = 0;
        this.size = 125; // NOSONAR
        this.level = Level.High;
        this.margin = true;
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


    PDFImage render(QRCodeRenderer renderer) throws RenderException {

        byte[] qrCode = renderer.render(code, size, level.val, margin);

        return new PDFImage(qrCode, x, y);
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


    /**
     * Set the error correction level of this specification.
     *
     * @param  level  to set
     *
     * @return  this specification for chaining
     *
     * @since  0.4
     */
    public QRSpec withErrorCorrection(Level level) {

        this.level = level;

        return this;
    }


    /**
     * Tries to disable the silent-zone default margin.
     *
     * @return  this spec for chaining
     *
     * @since  0.4
     */
    public QRSpec withoutMargin() {

        this.margin = false;

        return this;
    }


    /**
     * Enables the silent-zone default margin (on by default).
     *
     * @return  this spec by default
     *
     * @since  0.4
     */
    public QRSpec withMargin() {

        this.margin = true;

        return this;
    }
}
