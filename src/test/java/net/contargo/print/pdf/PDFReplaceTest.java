package net.contargo.print.pdf;

import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;

import java.util.Map;


/**
 * @author  Olle Törnström - toernstroem@synyx.de
 */
@RunWith(MockitoJUnitRunner.class)
public class PDFReplaceTest {

    @Mock
    private Path mockedPath;

    @Mock
    private Map<String, String> mockedTexts;

    @Mock
    private PDFEngine mockedPDFEngine;

    @Test(expected = IllegalArgumentException.class)
    public void ensureSearchAndReplaceTextRequiresPathParameter() {

        PDFReplace.newInstance().searchAndReplaceText(null, mockedTexts);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureSearchAndReplaceTextRequiresTextsParameter() {

        PDFReplace.newInstance().searchAndReplaceText(mockedPath, null);
    }


    @Test
    public void ensureDoSearchAndReplaceDelegatesToPDFEngine() {

        PDFReplace pdfReplace = PDFReplace.newInstanceWithEngine(mockedPDFEngine);

        pdfReplace.searchAndReplaceText(mockedPath, mockedTexts);

        Mockito.verify(mockedPDFEngine).searchAndReplaceText(mockedPath, mockedTexts);
    }
}
