package net.contargo.print.pdf;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;


/**
 * A rendered PDF document.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public final class PDFDocument {

    private final byte[] data;

    PDFDocument(byte[] data) {

        this.data = data.clone();
    }

    public void save(Path target) throws IOException {

        Files.write(target, data);
    }


    public void save(OutputStream output) throws IOException {

        output.write(data);
    }
}
