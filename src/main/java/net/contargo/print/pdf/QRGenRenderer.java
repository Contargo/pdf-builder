package net.contargo.print.pdf;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import net.glxn.qrgen.core.exception.QRGenerationException;
import net.glxn.qrgen.javase.QRCode;


/**
 * A QR-code renderer implementation using the QRGen library (https://github.com/kenglxn/QRGen).
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public class QRGenRenderer implements QRCodeRenderer {

    @Override
    public byte[] render(String code, int size, int level, boolean margin) throws RenderException {

        try {
            QRCode c = QRCode.from(code).withErrorCorrection(toErrorCorrectionLevel(level)).withSize(size, size);

            if (!margin) {
                c = c.withHint(EncodeHintType.MARGIN, 0);
            }

            return c.stream().toByteArray();
        } catch (QRGenerationException e) {
            throw new RenderException("QR-code render failed.", e);
        }
    }


    private ErrorCorrectionLevel toErrorCorrectionLevel(int level) {

        if (level == 7) {
            return ErrorCorrectionLevel.L;
        } else if (level == 15) {
            return ErrorCorrectionLevel.M;
        } else if (level == 25) {
            return ErrorCorrectionLevel.Q;
        } else {
            return ErrorCorrectionLevel.H;
        }
    }
}
