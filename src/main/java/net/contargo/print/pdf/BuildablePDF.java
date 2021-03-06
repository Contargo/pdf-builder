package net.contargo.print.pdf;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A chaining API for specifying a PDF to build.
 *
 * @author  Olle Törnström - toernstroem@synyx.de
 * @author  Aljona Murygina - murygina@synyx.de*
 * @since  0.1
 */
public final class BuildablePDF {

    private static final Logger LOG = LoggerFactory.getLogger(BuildablePDF.class);

    private static final String WHITESPACE = " ";

    /**
     * A ligature is a combination of two or more letters into a single symbol thus this combination of letters should
     * be never used within a placeholder. TODO: Add further ligatures!
     */
    private static final String[] LIGATURES = { "fi" };

    private static final BiConsumer<String, Object> ASSERT_NOT_NULL = (String name, Object value) -> {
        if (value == null) {
            throw new IllegalArgumentException(String.format("The %s must not be null", name));
        }
    };

    private static final BiConsumer<String, String> ASSERT_NOT_EMPTY = (String name, String value) -> {
        ASSERT_NOT_NULL.accept(name, value);

        if (value.isEmpty()) {
            throw new IllegalArgumentException(String.format("The %s must not be empty", name));
        }
    };

    private static final Consumer<String> ASSERT_VALID_SEARCH_VALUE = (String value) -> {
        ASSERT_NOT_EMPTY.accept("search value", value);

        for (String ligature : LIGATURES) {
            if (value.contains(ligature)) {
                throw new IllegalArgumentException(String.format("The search value must not contain the ligature `%s`",
                        ligature));
            }
        }
    };

    /**
     * Describes text filling behaviour of multi line replacement: fill text from top to bottom
     * {@link MultiLineTextFillMode#TOP} or from bottom to top {@link MultiLineTextFillMode#BOTTOM}.
     */
    public enum MultiLineTextFillMode {

        TOP,
        BOTTOM
    }

    private final PDFBuilder builder;
    private Path templateAsPath;
    private InputStream templateAsStream;
    private final Map<String, String> replacements;
    private final List<QRSpec> qrCodes;
    private final List<PDFImage> images;

    BuildablePDF(Path template, PDFBuilder builder) {

        this.builder = builder;
        this.templateAsPath = template;
        this.replacements = new HashMap<>();
        this.qrCodes = new ArrayList<>();
        this.images = new ArrayList<>();
    }


    BuildablePDF(InputStream template, PDFBuilder builder) {

        this.builder = builder;
        this.templateAsStream = template;
        this.replacements = new HashMap<>();
        this.qrCodes = new ArrayList<>();
        this.images = new ArrayList<>();
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
        pdf = builder.renderImages(pdf, images);

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

        replacements.keySet().forEach(ASSERT_VALID_SEARCH_VALUE::accept);

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
     * Add a Image to this builder.
     *
     * @param  image image content and desired position
     *
     * @return  this builder for chaining
     */
    public BuildablePDF withImage(PDFImage image) {

        this.images.add(image);

        return this;
    }


    /**
     * Adds a multi-line text to this builder by replacing the given placeholders with split text start filling from
     * top.
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

        return withMultiLineReplacement(text, maxCharactersPerLine, MultiLineTextFillMode.TOP, placeholders);
    }


    /**
     * Adds a multi-line text to this builder by replacing the given placeholders with split text.
     *
     * @param  text  string to replace with
     * @param  maxCharactersPerLine  describes how many characters fit into a line of the multi-line text
     * @param  fillFrom  describes if the placeholders are filled from top to bottom or from bottom to top
     * @param  placeholders  string(s) to search for
     *
     * @return  this builder for chaining
     *
     * @since  0.3
     */
    public BuildablePDF withMultiLineReplacement(String text, int maxCharactersPerLine, MultiLineTextFillMode fillFrom,
        String... placeholders) {

        ASSERT_NOT_NULL.accept("placeholders", placeholders);

        String content = text == null ? "" : text;

        int numberOfPlaceholders = placeholders.length;
        int numberOfCharacters = content.length();

        assertCorrectMultiLineReplacementParameters(numberOfCharacters, maxCharactersPerLine, numberOfPlaceholders);

        LOG.debug("Execute multi line replacement --------------------");

        String[] words = content.split(WHITESPACE, Integer.MAX_VALUE);
        LOG.debug("Number of words: " + words.length);

        String[] replace = distributeWordsToPlaceholders(words, numberOfPlaceholders, maxCharactersPerLine);

        Map<String, String> replacementsMap = createReplacementsMap(fillFrom, placeholders, replace);

        this.replacements.putAll(replacementsMap);

        LOG.debug("Done multi line replacement -----------------------");

        return this;
    }


    private void assertCorrectMultiLineReplacementParameters(int numberOfCharacters, int maxCharactersPerLine,
        int numberOfPlaceholders) {

        if (maxCharactersPerLine < 1) {
            throw new IllegalArgumentException("Invalid number of maximum characters per line: "
                + maxCharactersPerLine);
        }

        if (numberOfPlaceholders < 2) {
            throw new IllegalArgumentException("At least two placeholders must be provided, but was: "
                + numberOfPlaceholders);
        }

        if (numberOfPlaceholders * maxCharactersPerLine < numberOfCharacters) {
            throw new IllegalArgumentException(String.format(
                    "The given text contains %d characters, "
                    + "but there are only %d lines with maximum %d characters each", numberOfCharacters,
                    numberOfPlaceholders, maxCharactersPerLine));
        }
    }


    private String[] distributeWordsToPlaceholders(String[] words, int numberOfPlaceholders, int maxCharactersPerLine) {

        // Initialize replacement values
        String[] replace = new String[numberOfPlaceholders];

        for (int i = 0; i < replace.length; i++) {
            replace[i] = "";
        }

        // Fill replacement values
        int wordCounter = 0;
        boolean allWordsCompleted = false;

        for (int i = 0; i < replace.length; i++) {
            // If all words of the text has already been used, the replacement value will be empty
            if (allWordsCompleted) {
                break;
            }

            // Fill the replacement value with all the words that fit in
            while (replace[i].length() + words[wordCounter].length() <= maxCharactersPerLine) {
                replace[i] = replace[i].concat(words[wordCounter]).concat(WHITESPACE);

                // Stop the process if there are no words left
                if (wordCounter == words.length - 1) {
                    allWordsCompleted = true;

                    break;
                }

                wordCounter += 1;
            }

            // Each replacement value represents a line, no need to have a whitespace at the end
            if (replace[i].endsWith(WHITESPACE)) {
                replace[i] = replace[i].substring(0, replace[i].length() - 1);
            }
        }

        if (!allWordsCompleted) {
            throw new IllegalArgumentException(String.format(
                    "The given text does not fit in %d lines with maximum %d characters because of length of words",
                    numberOfPlaceholders, maxCharactersPerLine));
        }

        return replace;
    }


    private Map<String, String> createReplacementsMap(MultiLineTextFillMode fillFrom, String[] placeholders,
        String[] replace) {

        Map<String, String> replacementMap = new HashMap<>();

        if (MultiLineTextFillMode.BOTTOM.equals(fillFrom)) {
            // Do only use replacement values that have content
            List<String> replacementValues = Arrays.asList(replace)
                .stream()
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());

            // Reverse order because filling from bottom to top
            Collections.reverse(replacementValues);

            Iterator<String> iterator = replacementValues.iterator();

            for (int i = placeholders.length - 1; i >= 0; i--) {
                String replacementValue = "";

                if (iterator.hasNext()) {
                    replacementValue = iterator.next();
                }

                LOG.debug("Replacement " + i + ": " + placeholders[i] + "=`" + replacementValue + "`");

                replacementMap.put(placeholders[i], replacementValue);
            }
        } else {
            for (int i = 0; i < placeholders.length; i++) {
                LOG.debug("Replacement " + i + ": " + placeholders[i] + "=`" + replace[i] + "`");

                replacementMap.put(placeholders[i], replace[i]);
            }
        }

        return replacementMap;
    }
}
