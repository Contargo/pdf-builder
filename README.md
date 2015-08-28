PDF Builder
===========

A library tool that provides a pragmatic solution to PDF-generation
and manipulation.

## General Idea

Instead of being a platform, with template authoring and such
features, this library aids in more simple text interpolation,
in already existing PDF templates.

The intent is aimed at the 80% use cases - there's a pretty
PDF document, where some values, or placeholder, can be easily
replaced.

## Development

This is a pretty straight-forward Java-project, use `mvn` to build,
test and deploy. Happy hacking!

### Practical Use

In order to help with quality assurance (QA) and template development,
there's a practial executable  Java tool to be found under
`src/test/net/contargo/qa/PDFBuilderQA.java`. This will generate some
PDF documents, that a human can view and evaluate - ensuring that
everything looks ok.

Feel free to play around with this tool, to try out the features of
the PDF builder.

Happy hacking!