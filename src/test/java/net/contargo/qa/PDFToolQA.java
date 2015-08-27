package net.contargo.qa;

import net.contargo.print.pdf.PDFBuilder;
import net.contargo.print.pdf.PDFBuilder.QRSpec;

import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A quality assurance (QA) utility class, that provides a pre-baked way of visually (manually) proofing of PDF
 * manipulations. A crutch, but an effective one.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public class PDFToolQA {

    private static final String FOO_PDF = "foo.pdf";
    private static final String LETTER_PDF = "letter.pdf";

    private static final String WITH_QR_CODE_CHECK = "check-with-qr-code.pdf";
    private static final String TITLE_REPLACED_CHECK = "check-letter-title-replaced.pdf";
    private static final String ONLY_BAR_CHECK = "check-no-foo-only-bar.pdf";
    private static final String NO_CHANGE_CHECK = "check-no-change-still-foo.pdf";

    private static final Path RESOURCES = FileSystems.getDefault().getPath("src/test/resources");

    private final List<Path> targets = new ArrayList<>();

    /*
     * Starter-method for the QA-scenarios.
     */
    public static void main(String[] args) throws IOException {

        new PDFToolQA().execute();
    }


    private void execute() throws IOException {

        performGenerateWithoutReplacements();
        performGenerateWithReplacement();
        performGenerateWithReplacements();
        performGenerateOneQRCode();
        performGenerateMoreQRCodes();

        alertUserAndWaitForEnter();

        clearGeneratedTargets();

        kthxBye();
    }


    private void performGenerateWithoutReplacements() throws IOException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve(NO_CHANGE_CHECK);
        PDFBuilder.fromTemplate(source).build().save(target);
        targets.add(target);
    }


    private void performGenerateWithReplacement() throws IOException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve(ONLY_BAR_CHECK);
        PDFBuilder.fromTemplate(source).withReplacement("foo", "bar").build().save(target);
        targets.add(target);
    }


    private void performGenerateWithReplacements() throws IOException {

        Path source = RESOURCES.resolve(LETTER_PDF);
        Path target = RESOURCES.resolve(TITLE_REPLACED_CHECK);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("TESTBRIEF", "REPLACED");
        replacements.put("Testbrief", "Replaced");

        PDFBuilder.fromTemplate(source).withReplacements(replacements).build().save(target);
        targets.add(target);
    }


    private void performGenerateOneQRCode() throws IOException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve(WITH_QR_CODE_CHECK);

        PDFBuilder.fromTemplate(source)
            .withQRCode(QRSpec.fromCode("code").withPositionY(-24).withPositionX(20).withSize(140))
            .build()
            .save(target);
        targets.add(target);
    }


    private void performGenerateMoreQRCodes() throws IOException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve("check-with-more-qr-codes.pdf");

        PDFBuilder.fromTemplate(source)
            .withQRCode(QRSpec.fromCode("one").withPosition(20, -20))
            .withQRCode(QRSpec.fromCode("two").withPositionX(-20).withPositionY(55).withSize(160))
            .build()
            .save(target);
        targets.add(target);
    }


    private void alertUserAndWaitForEnter() throws IOException {

        System.out.println(String.format("Please check the results in %s", RESOURCES));
        System.out.println("Then press any ENTER to cleanup all generated files.");
        System.in.read();
    }


    private void clearGeneratedTargets() {

        targets.stream().filter(o -> o != null).forEach(p -> p.toFile().delete());
    }


    private void kthxBye() {

        System.out.println("Thank you!");
    }
}
