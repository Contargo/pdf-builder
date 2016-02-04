package net.contargo.print.pdf;

import java.io.InputStream;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * A chaining API for specifying a PDF to build.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @author  Aljona Murygina - murygina@synyx.de*
 * @since  0.1
 */
public final class BuildablePDF {

    private static final String WHITESPACE = " ";

    /**
     * A ligature is a combination of two or more letters into a single symbol thus this combination of letters should
     * be never used within a placeholder. TODO: Add further ligatures!
     */
    private static final String[] LIGATURES = { "fi" };

    private static final Consumer<String> ASSERT_VALID_SEARCH_VALUE = (String value) -> {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("The search value must not be empty");
        }

        for (String ligature : LIGATURES) {
            if (value.contains(ligature)) {
                throw new IllegalArgumentException(String.format("The search value must not contain the ligature `%s`",
                        ligature));
            }
        }
    };

    private final PDFBuilder builder;
    private Path templateAsPath;
    private InputStream templateAsStream;
    private final Map<String, String> replacements;
    private final List<QRSpec> qrCodes;

    BuildablePDF(Path template, PDFBuilder builder) {

        this.builder = builder;
        this.templateAsPath = template;
        this.replacements = new HashMap<>();
        this.qrCodes = new ArrayList<>();
    }


    BuildablePDF(InputStream template, PDFBuilder builder) {

        this.builder = builder;
        this.templateAsStream = template;
        this.replacements = new HashMap<>();
        this.qrCodes = new ArrayList<>();
    }

    /**
     * Builds a PDF from this builder.
     *
     * @return  the built PDF document
     *
     * @throws  RenderException  in case rendering fails, describing the originating cause of failure
     */
    public PDFDocument build() throws RenderException {

        byte[] pdf;

        pdf = templateAsPath != null ? builder.renderFromTemplate(templateAsPath)
                                     : builder.renderFromTemplate(templateAsStream);

        pdf = builder.renderSearchAndReplaceText(pdf, replacements);
        pdf = builder.renderQRCodes(pdf, qrCodes);

        return new PDFDocument(pdf);
    }


    /**
     * Add a search-replace pair to this builder.
     *
     * @param  search  string to search for
     * @param  replace  string to replace with
     *
     * @return  this builder for chaining
     */
    public BuildablePDF withReplacement(String search, String replace) {

        ASSERT_VALID_SEARCH_VALUE.accept(search);

        this.replacements.put(search, replace);

        return this;
    }


    /**
     * Adds a map of search-replace pairs to this builder.
     *
     * @param  replacements  map of string pairs to search and replace with
     *
     * @return  this builder for chaining
     */
    public BuildablePDF withReplacements(Map<String, String> replacements) {

        for (String key : replacements.keySet()) {
            ASSERT_VALID_SEARCH_VALUE.accept(key);
        }

        this.replacements.putAll(replacements);

        return this;
    }


    /**
     * Add a QR code specification to this builder.
     *
     * @param  qrSpec  describing the QR code to add
     *
     * @return  this builder for chaining
     */
    public BuildablePDF withQRCode(QRSpec qrSpec) {

        this.qrCodes.add(qrSpec);

        return this;
    }


    /**
     * Adds a multi-line text to this builder by replacing the given placeholders with split text.
     *
     * @param  text  string to replace with
     * @param  maxCharactersPerLine  describes how many characters fit into a line of the multi-line text
     * @param  placeholders  string(s) to search for
     *
     * @return  this builder for chaining
     *
     * @since  0.3
     */
    public BuildablePDF withMultiLineReplacement(String text, int maxCharactersPerLine, String... placeholders) {

        int numberOfPlaceholders = placeholders.length;
        int numberOfCharacters = text.length();

        if (numberOfPlaceholders * maxCharactersPerLine < numberOfCharacters) {
            throw new IllegalArgumentException(String.format(
                    "The given text contains %d characters, but there are only %d lines with maximum %d characters each",
                    numberOfCharacters, numberOfPlaceholders, maxCharactersPerLine));
        }

        // TODO: Refactor the shit out of it!

        // Initialize replace values
        String[] replace = new String[placeholders.length];

        for (int i = 0; i < placeholders.length; i++) {
            replace[i] = "";
        }

        // Split text to words
        String[] words = text.split(WHITESPACE, Integer.MAX_VALUE);
        System.out.println("Number of words: " + words.length);

        int wordCounter = 0;

        boolean allWordsCompleted = false;

        for (int i = 0; i < replace.length; i++) {
            if (allWordsCompleted) {
                break;
            }

            while (replace[i].length() + words[wordCounter].length() <= maxCharactersPerLine) {
                replace[i] = replace[i].concat(words[wordCounter]);
                replace[i] = replace[i].concat(WHITESPACE);

                if (wordCounter == words.length - 1) {
                    allWordsCompleted = true;

                    break;
                }

                wordCounter += 1;
            }

            if (replace[i].endsWith(WHITESPACE)) {
                replace[i] = replace[i].substring(0, replace[i].length() - 1);
            }

            System.out.println("");
        }

        // Fill replacements map
        for (int i = 0; i < placeholders.length; i++) {
            System.out.println(placeholders[i] + "=`" + replace[i] + "`");

            this.replacements.put(placeholders[i], replace[i]);
        }

        return this;
    }
}
