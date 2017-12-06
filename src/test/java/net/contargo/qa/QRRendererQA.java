package net.contargo.qa;

import net.contargo.print.pdf.QRGenRenderer;
import net.contargo.print.pdf.RenderException;

import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * A quality assurance tool, that provides a means to generate and assess generated QR-codes.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.4
 */
public class QRRendererQA {

    private static final Path RESOURCES = FileSystems.getDefault().getPath("src/test/resources");

    private List<Path> targets = new ArrayList<>();

    public static void main(String[] args) throws IOException, RenderException {

        new QRRendererQA().execute();
    }


    private void execute() throws IOException, RenderException {

        generateQrCode(20);
        generateQrCode(60);
        generateQrCode(125);
        generateQrCode(250);

        alertUserAndWaitForEnter();
        clearGeneratedTargets();
        kthxBye();
    }


    private void generateQrCode(int size) throws IOException, RenderException {

        List<Integer> levels = Arrays.asList(7, 15, 25, 30);

        for (int level : levels) {
            Path path = RESOURCES.resolve(String.format("size-%d-error-%d-qr.png", size, level));
            targets.add(path);
            Files.write(path, new QRGenRenderer().render(UUID.randomUUID().toString(), size, level));
        }
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
