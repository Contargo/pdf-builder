package net.contargo.print.pdf;

/**
 * Describing a QR code to render.
 *
 * <p>Using a nifty builder pattern, a QR-code spec can easily be built for to be appended on a PDF page.</p>
 *
 * <pre><code>
        QRSpec spec = QRSpec.fromCode("some-code")
                             .withPosition(42, 7)
                             .withSize(233)
                             .withoutMargin()
                             .withErrorCorrection(Level.Low);
   </code></pre>
 *
 * <p>Positioning can be done either by X or Y axis separately, or using the {@code withPosition(x, y)} method. Please
 * note that positioning on PDF pages, are done <strong>from the lower left corner.</strong></p>
 *
 * <p>In the example above we also override the automatic border, or silent-zone, that is part of the QR-code standard.
 * Please note that this is not always deterministic, so different error correction modes will honor the flag
 * differently.</p>
 *
 * <p>Lastly, we can adjust the error correction level, setting the level to {@code Low}, {@code Medium},
 * {@code Quartile} or {@code High}. More information is here: https://en.wikipedia.org/wiki/QR_code#Error_correction.
 * </p>
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
     * Set the y position for this specification, as an offset from the page left edge.
     *
     * <p>NOTE: The page origin is at the bottom lower left corner.</p>
     *
     * @param  y  position offset, from the left side of the page
     *
     * @return  this specification for chaining
     */
    public QRSpec withPositionY(int y) {

        this.y = y;

        return this;
    }


    /**
     * Set the x position for this specification, specified as an offset from the page bottom.
     *
     * <p>NOTE: The page origin is at the bottom lower left corner.</p>
     *
     * @param  x  position offset, from the bottom of the page
     *
     * @return  this specification for chaining
     */
    public QRSpec withPositionX(int x) {

        this.x = x;

        return this;
    }


    /**
     * Set the x and y position of this specification, as an offset coordinate from the page bottom left corner.
     *
     * <p>NOTE: The page origin is at the bottom lower left corner.</p>
     *
     * @param  x  position offset, from the bottom of the page
     * @param  y  position offset, from the left edge of the page
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
