PDF Builder
===========

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

* Reading and parsing a file into a PDF byte array. Usefull for
  streaming or attachement in emails.

* String interpolation of search-replace pairs, or from a string
  map.

* Rendering of QR-codes, with size and position specification.

## Known Issues

** Currently, the PDF renderer implementation is using a library
   that does not handle True Type Fonts (TTF) very well, or at
   all. Therefore, please make sure to QA check your template
   and generated PDF documents. **

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
there's a practial executable  Java tool to be found under
`src/test/net/contargo/qa/PDFBuilderQA.java`. This will generate some
PDF documents, that a human can view and evaluate - ensuring that
everything looks ok.

Feel free to play around with this tool, to try out the features of
the PDF builder.

Happy hacking!
