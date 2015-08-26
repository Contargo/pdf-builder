package net.contargo.print.pdf;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


/**
 * A utility tool for PDF creation and manipulation.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @since  0.1
 */
public final class PDFTool {

    private final PDFRenderer engine = new PDFBoxRenderer();

    private PDFTool() {

        // OK
    }

    /**
     * Searches and replaces texts in the given file based on the provided map of search-replace pairs.
     *
     * @param  path  path of the file to interpolate the given texts with
     * @param  texts  map of search-replace pairs, to interpolate
     *
     * @return  the interpolated result as a byte array, never {@code null}
     */
    private byte[] searchAndReplaceText(Path path, Map<String, String> texts) {

        BiConsumer<String, Object> assertNotNull = (String n, Object o) -> {
            if (o == null) {
                throw new IllegalArgumentException(String.format("The %s must not be null", n));
            }
        };

        assertNotNull.accept("path", path);
        assertNotNull.accept("texts", texts);

        return engine.searchAndReplaceText(path, texts);
    }


    public static PDFBuilder fromTemplate(Path template) {

        return new PDFBuilder(template);
    }

    public static final class PDFBuilder {

        private final Path template;
        private final Map<String, String> replacements;

        public PDFBuilder(Path template) {

            this.template = template;
            this.replacements = new HashMap<>();
        }

        public PDF generate() {

            return new PDF(new PDFTool().searchAndReplaceText(template, replacements));
        }


        public PDFBuilder withReplacement(String search, String replace) {

            replacements.put(search, replace);

            return this;
        }


        public PDFBuilder withReplacements(Map<String, String> replacements) {

            this.replacements.putAll(replacements);

            return this;
        }
    }

    public static final class PDF {

        private final byte[] data;

        public PDF(byte[] data) {

            this.data = data.clone();
        }

        public void save(Path target) throws IOException {

            Files.write(target, data);
        }


        public void save(OutputStream output) throws IOException {

            output.write(data);
        }
    }
}
