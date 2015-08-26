package net.contargo.print.pdf;

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

        alertUserAndWaitForEnter();

        clearGeneratedTargets();

        kthxBye();
    }


    private void performGenerateWithoutReplacements() throws IOException {

        Path source = RESOURCES.resolve("foo.pdf");
        Path target = RESOURCES.resolve("no-change-still-foo.pdf");
        PDFTool.fromTemplate(source).generate().save(target);
        targets.add(target);
    }


    private void performGenerateWithReplacement() throws IOException {

        Path source = RESOURCES.resolve("foo.pdf");
        Path target = RESOURCES.resolve("no-foo-only-bar.pdf");
        PDFTool.fromTemplate(source).withReplacement("foo", "bar").generate().save(target);
        targets.add(target);
    }


    private void performGenerateWithReplacements() throws IOException {

        Path source = RESOURCES.resolve("letter.pdf");
        Path target = RESOURCES.resolve("letter-title-replaced.pdf");

        Map<String, String> replacements = new HashMap<>();
        replacements.put("TESTBRIEF", "REPLACED");
        replacements.put("Testbrief", "Replaced");

        PDFTool.fromTemplate(source).withReplacements(replacements).generate().save(target);
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
