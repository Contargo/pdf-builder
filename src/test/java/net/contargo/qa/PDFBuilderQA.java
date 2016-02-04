package net.contargo.qa;

import net.contargo.print.pdf.BuildablePDF;
import net.contargo.print.pdf.PDFBuilder;
import net.contargo.print.pdf.QRSpec;
import net.contargo.print.pdf.RenderException;

import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * A quality assurance (QA) utility class, that provides a pre-baked way of visually (manually) proofing of PDF
 * manipulations. A crutch, but an effective one.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public class PDFBuilderQA {

    private static final String FOO_PDF = "foo.pdf";
    private static final String LETTER_PDF = "letter.pdf";
    private static final String FOOTER_PDF = "footer.pdf";

    private static final String NO_CHANGE_CHECK = "check-no-change-still-foo.pdf";
    private static final String TITLE_REPLACED_CHECK = "check-letter-title-replaced.pdf";
    private static final String ONLY_BAR_CHECK = "check-no-foo-only-bar.pdf";
    private static final String QR_CODE_CHECK = "check-with-qr-code.pdf";
    private static final String MORE_QR_CODES_CHECK = "check-with-more-qr-codes.pdf";
    private static final String TITLES_REPLACED_AND_QR_CODES_CHECK = "check-with-replaced-titles-and-qr-codes.pdf";
    private static final String MULTI_LINE_REPLACE_CHECK = "check-multi-line-footer.pdf";

    private static final Path RESOURCES = FileSystems.getDefault().getPath("src/test/resources");

    private final List<Path> targets = new ArrayList<>();

    /*
     * Starter-method for the QA-scenarios.
     */
    public static void main(String[] args) throws IOException, RenderException {

        new PDFBuilderQA().execute();
    }


    private void execute() throws IOException, RenderException {

        performGenerateWithoutReplacements();
        performGenerateWithReplacement();
        performGenerateWithReplacements();
        performGenerateOneQRCode();
        performGenerateMoreQRCodes();
        performReplaceAndQRCodesGeneration();
        performGenerateWithMultiLineReplacements();

        alertUserAndWaitForEnter();

        clearGeneratedTargets();

        kthxBye();
    }


    private void performGenerateWithoutReplacements() throws IOException, RenderException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve(NO_CHANGE_CHECK);
        PDFBuilder.fromTemplate(source).build().save(target);
        targets.add(target);
    }


    private void performGenerateWithReplacement() throws IOException, RenderException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve(ONLY_BAR_CHECK);
        PDFBuilder.fromTemplate(source).withReplacement("foo", "bar").build().save(target);
        targets.add(target);
    }


    private void performGenerateWithReplacements() throws IOException, RenderException {

        Path source = RESOURCES.resolve(LETTER_PDF);
        Path target = RESOURCES.resolve(TITLE_REPLACED_CHECK);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("TESTBRIEF", "REPLACED");
        replacements.put("Testbrief", "Replaced");

        PDFBuilder.fromTemplate(source).withReplacements(replacements).build().save(target);
        targets.add(target);
    }


    private void performGenerateOneQRCode() throws IOException, RenderException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve(QR_CODE_CHECK);

        PDFBuilder.fromTemplate(source)
            .withQRCode(QRSpec.fromCode("code").withPositionY(-24).withPositionX(20).withSize(140))
            .build()
            .save(target);
        targets.add(target);
    }


    private void performGenerateMoreQRCodes() throws IOException, RenderException {

        Path source = RESOURCES.resolve(FOO_PDF);
        Path target = RESOURCES.resolve(MORE_QR_CODES_CHECK);

        PDFBuilder.fromTemplate(source)
            .withQRCode(QRSpec.fromCode("one").withPosition(20, -20))
            .withQRCode(QRSpec.fromCode("two").withPositionX(-20).withPositionY(55).withSize(160))
            .build()
            .save(target);
        targets.add(target);
    }


    private void performReplaceAndQRCodesGeneration() throws IOException, RenderException {

        Path source = RESOURCES.resolve(LETTER_PDF);
        Path target = RESOURCES.resolve(TITLES_REPLACED_AND_QR_CODES_CHECK);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("TESTBRIEF", "REPLACED AGAIN!");
        replacements.put("Testbrief", "Replaced Again!");

        PDFBuilder.fromTemplate(source)
            .withReplacements(replacements)
            .withQRCode(QRSpec.fromCode("Hi there!").withPosition(30, 220))
            .withQRCode(QRSpec.fromCode("Hello again!").withPosition(-13, 220))
            .build()
            .save(target);
        targets.add(target);
    }


    private void performGenerateWithMultiLineReplacements() throws IOException, RenderException {

        Function<String, String> placeholder = (s) -> String.format("\\$\\{%s\\}", s);

        Path source = RESOURCES.resolve(FOOTER_PDF);
        Path target = RESOURCES.resolve(MULTI_LINE_REPLACE_CHECK);

        PDFBuilder.fromTemplate(source)
            .withMultiLineReplacement(veryLongText(), 210, BuildablePDF.MultiLineTextFillMode.BOTTOM,
                    placeholder.apply("FOOTER1"), placeholder.apply("FOOTER2"), placeholder.apply("FOOTER3"),
                    placeholder.apply("FOOTER4"))
            .build()
            .save(target);
        targets.add(target);
    }


    private String veryLongText() {

        return "Contargo GmbH & Co.KG. · Sitz: Duisburg · AG Duisburg HRA 7682 · PhG: Contargo Verwaltungs GmbH, "
            + "Duisburg · AG Duisburg HRB 11844 · Geschäftsführung: Heinrich Kerstgens, Thomas Löffler, Thomas Maaßen, "
            + "Dr. Martin Neese · Es findet deutsches Recht Anwendung · Erfüllungs- und Gerichtsstand ist "
            + "Duisburg · Ust-IdNr. DE 813870828 · St-Nr. 134/5844/0430 · Bankverbindung: Dresdner Bank, "
            + "BLZ 480 800 00, Kto 100 255 700";
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
