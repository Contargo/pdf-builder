package net.contargo.print.pdf;

import org.junit.Assert;
import org.junit.Test;

import org.junit.runner.RunWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author  Olle Törnström - toernstroem@synyx.de
 * @author  Slaven Travar - slaven.travar@pta.de
 */
@RunWith(MockitoJUnitRunner.class)
public class PDFBuilderTest {

    @Mock
    private PDFRenderer mockedPDFRenderer;
    @Mock
    private QRCodeRenderer mockedQRCodeRenderer;
    @Mock
    private Path mockedPathTemplate;
    @Mock
    private Path mockedInputStreamTemplate;
    @Captor
    private ArgumentCaptor<List<QRCode>> qrCodesCaptor;

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullPathTemplateArgument() {

        Path template = null;
        PDFBuilder.fromTemplate(template);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullStreamTemplateArgument() {

        InputStream template = null;
        PDFBuilder.fromTemplate(template);
    }


    @Test
    public void ensureDelegatesRenderFromPathTemplateToPDFRenderer() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderFromTemplate(mockedPathTemplate);

        Mockito.verify(mockedPDFRenderer).renderFromTemplate(mockedPathTemplate);
    }


    @Test
    public void ensureDelegatesRenderFromStreamTemplateToPDFRenderer() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderFromTemplate(mockedInputStreamTemplate);

        Mockito.verify(mockedPDFRenderer).renderFromTemplate(mockedInputStreamTemplate);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureRenderFromPathTemplateThrowsOnNull() throws RenderException {

        Path template = null;
        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderFromTemplate(template);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureRenderFromStreamTemplateThrowsOnNull() throws RenderException {

        InputStream template = null;
        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderFromTemplate(template);
    }


    @Test
    public void ensureRenderSearchAndReplaceTextDelegatesToPDFRenderer() throws RenderException {

        byte[] pdf = new byte[0];
        Map<String, String> text = Collections.emptyMap();

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderSearchAndReplaceText(pdf, text);

        Mockito.verify(mockedPDFRenderer).renderSearchAndReplaceText(pdf, text);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureRenderSearchAndReplaceTextThrowsOnNullPdfByteArray() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderSearchAndReplaceText(null,
            Collections.emptyMap());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureRenderSearchAndReplaceTextThrowsOnNullTextMap() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderSearchAndReplaceText(new byte[0], null);
    }


    @Test
    public void ensureRenderQRCodesDelegatesToBothRenderers() throws Exception {

        byte[] bytes = new byte[0];
        List<QRSpec> specs = new ArrayList<>(Collections.singletonList(QRSpec.fromCode("foobar")));

        Mockito.when(mockedQRCodeRenderer.render(Matchers.anyString(), Matchers.anyInt())).thenReturn(bytes);

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderQRCodes(bytes, specs);

        Mockito.verify(mockedQRCodeRenderer).render(Matchers.eq("foobar"), Matchers.anyInt());

        Mockito.verify(mockedPDFRenderer).renderQRCodes(Matchers.eq(bytes), qrCodesCaptor.capture());

        Assert.assertEquals("Wrong amount", 1, qrCodesCaptor.getValue().size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureRenderQRCodesThrowsOnNullPdfByteArray() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderQRCodes(null, Collections.emptyList());
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureRenderQRCodesThrowsOnNullQRCodesList() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderQRCodes(new byte[0], (List<QRSpec>) null);
    }
}
