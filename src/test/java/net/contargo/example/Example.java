package net.contargo.example;

import net.contargo.print.pdf.PDFBuilder;
import net.contargo.print.pdf.PDFBuilder.QRSpec;
import net.contargo.print.pdf.RenderException;

import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Path;


/**
 * This class is used only to create a simple example that can be copy-pasted into the Javadoc comment as a
 * `pre`-section.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 */
public class Example {

    {
        String name = "", email = "", code = "";

        Path docs = FileSystems.getDefault().getPath("documents");
        Path template = docs.resolve("template.pdf");
        Path result = docs.resolve("result.pdf");

        try {
            PDFBuilder.fromTemplate(template)
                .withReplacement("${name}", name)
                .withReplacement("${email}", email)
                .withQRCode(QRSpec.fromCode(code).withPosition(20, 50).withSize(145))
                .build()
                .save(result);
        } catch (IOException | RenderException e) {
            // Handle failure...
        }
    }
}
