package net.contargo.print.pdf;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A rendered QR code, providing it's byte array and position information.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public class QRCode {

    private final byte[] data;
    private final int x;
    private final int y;

    public QRCode(byte[] data, int x, int y) {

        this.data = data.clone();
        this.x = x;
        this.y = y;
    }

    public int getX() {

        return x;
    }


    public int getY() {

        return y;
    }


    public void save(OutputStream output) throws IOException {

        output.write(data);
    }
}
