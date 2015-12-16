package net.contargo.print.pdf;

import net.contargo.print.pdf.PDFBuilder.QRCode;
import net.contargo.print.pdf.PDFBuilder.QRSpec;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author  Olle Törnström - toernstroem@synyx.de
 */
@RunWith(MockitoJUnitRunner.class)
public class PDFBuilderTest {

    @Mock
    PDFRenderer mockedPDFRenderer;
    @Mock
    QRCodeRenderer mockedQRCodeRenderer;
    @Mock
    InputStream mockedTemplate;
    @Captor
    ArgumentCaptor<List<QRCode>> qrCodesCaptor;

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullTemplatePathArgument() throws RenderException {

        Path template = null;
        PDFBuilder.fromTemplate(template);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsOnNullTemplateInputStreamArgument() throws RenderException {

        InputStream template = null;
        PDFBuilder.fromTemplate(template);
    }


    @Test
    public void ensureDelegatesRenderFromTemplateToPDFRenderer() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderFromTemplate(mockedTemplate);

        Mockito.verify(mockedPDFRenderer).renderFromTemplate(mockedTemplate);
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureRenderFromTemplateThrowsOnNull() throws RenderException {

        new PDFBuilder(mockedPDFRenderer, mockedQRCodeRenderer).renderFromTemplate(null);
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
        List<QRSpec> specs = new ArrayList<>(Arrays.asList(QRSpec.fromCode("foobar")));

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
