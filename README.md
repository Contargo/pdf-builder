PDF Builder
===========

[![Build Status](https://travis-ci.org/Contargo/pdf-builder.svg?branch=master)](https://travis-ci.org/Contargo/pdf-builder)

**WIP: We're currently working on deploying to the Sonatype OSS repository. In the mean time, please consider building and deploying the project locally (mvn install).**

A library tool that provides a pragmatic solution to PDF-generation
and manipulation.

## General Idea

Instead of being a platform, with template authoring and such
features, this library aids in more simple text interpolation,
in already existing PDF templates.

The intent is aimed at the 80% use case - there is already a
pretty PDF document where some values, or placeholders, just
need to be replaced by the program.

Well good for you, that's just what this library does.

### Current features

* Reading and parsing a file into a PDF byte array. Useful for
  streaming or attachment in emails.

* String interpolation of search-replace pairs, or from a string
  map.

* Multi-line string interpolation, providing distribution of a
  text over a number of lines.

* Rendering of QR-codes, with size and position specification.

## Known Issues

### Not compatible with True Type Fonts (TTF)

** Currently, the PDF renderer implementation is using a library
   that does not handle True Type Fonts (TTF) very well, or at
   all. Therefore, please make sure to QA check your template
   and generated PDF documents. **

### Fonts must be fully embedded and not sub-setted

Font embedding and font sub-setting, are critical to the success of
templates for interpolation and rendering. At the current time, only
Adobe Illustrator successfully produces a non-subsetted and embedded
PDF.

### Ligatures cannot be used as placeholders

In PDFs [ligatures](https://en.wikipedia.org/wiki/Typographic_ligature)
are traditionally supported for optimal type setting. This can cause
word-mangling which may break string-interpolation.

For example the letters "fi" are encoded as a ligature in the PDF
(Tj operand) as a "~" (tilde), rendering the placeholder useless.
Beware!

## Development

This is a pretty straight-forward Java-project, use `mvn` to build,
test and deploy. Happy hacking!

### Deployment

Apart from the usual `mvn` release-dance, this project contains
tutorial and guide-documentation that must be published into the
detached branch `guides`. If any changes are made, please use:

    > git push origin `git subtree split --prefix guides`:guides --force

to push the new documentation to the remote.

### Practical Use

In order to help with quality assurance (QA) and template development,
there's a practical executable  Java tool to be found under
`src/test/net/contargo/qa/PDFBuilderQA.java`. This will generate some
PDF documents, that a human can view and evaluate - ensuring that
everything looks ok.

Feel free to play around with this tool, to try out the features of
the PDF builder.

## License

This project is distributed under the Apache 2.0 License. The full set of
terms and conditions can be seen in the [LICENSE](LICENSE) file.

See the [NOTICE](NOTICE) file distributed with this work for additional
information regarding copyright ownership.

Happy hacking!
