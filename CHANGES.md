PDF-Builder Changes
===================

## v0.3.0 [NEXT]

* Adds the capability to set error correction level for QR-codes.

## v0.3.1 (Release on 08.02.2016)

### Change Request:

* Allow usage of multi line replacement with empty text.

## v0.3.0 (Release on 08.02.2016)

### Features:

* Adds multi line replacement feature to be able to distribute a long text
  to multiple placeholders.

* Update to PDFBox version 1.8.11.

## v0.2.1 (Release on 17.12.2015)

### Features:

* Restores the public API PDFRenderer.renderFromTemplate(Path template) from
  v0.1.0 which was eliminated by mistake in v0.2.0

## v0.2.0 (Release on 16.12.2015)

### Features:

* Extends the API to accept java.io.InputStream as template source.
  So far only java.nio.file.Path was accepted as template source, but Path
  cannot be determined for a pdf-file which resides within a jar-file.
  Now a client can provide an InputStream as template source to the API.

## v0.1.0 (Initial Release)

### Features:

* Adds initial PDF rendering features; simple PDF-templating with string-
  interpolation and QR-Code rendering with position and size-parameters.

### Known Issues:

* PDF-renderer is not fully compatible with templates using True-Type
  fonts. See README.md file for more information.
