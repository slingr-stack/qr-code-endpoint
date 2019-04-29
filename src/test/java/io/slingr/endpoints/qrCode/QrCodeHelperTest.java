package io.slingr.endpoints.qrCode;

import com.google.zxing.*;
import io.slingr.endpoints.utils.FilesUtils;
import io.slingr.endpoints.utils.Json;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

public class QrCodeHelperTest {

    private QRCodeHelper qrCodeHelper;

    private final String PDF_FILE = "pdfToTest.pdf";
    private final String IMAGE_FILE = "imageQR.png";

    @Before
    public void init() {
        qrCodeHelper = new QRCodeHelper();
    }

    @Test
    public void testGenerateQr() throws IOException, WriterException {

        String message = "Help as QR";

        File temp = qrCodeHelper.createQRImage(message, Json.map());

        InputStream is = new FileInputStream(temp);
        Json codes = qrCodeHelper.decodeQRCodeFromImage(is);

        Assert.assertEquals(message, codes.object(0));

    }

    @Test
    public void testReadQrFromPdf() throws IOException {

        InputStream file = FilesUtils.getInternalFile(PDF_FILE);
        Json res = qrCodeHelper.decodeQRCodeFromPdf(file);

        String str1 = "BCD\n" +
                "001\n" +
                "1\n" +
                "SCT\n" +
                "OKOYFIHH\n" +
                "Asiakas T. Meikäläinen\n" +
                "FI7944052020036082\n" +
                "EUR158.24\n\n" +
                "RF07663321328510\n\n" +
                "ReqdExctnDt/2014-01-22";


        Assert.assertNotNull(res);
        Assert.assertEquals(str1, res.object(0));

    }

    @Test
    public void decodeImageQr() throws IOException {

        InputStream file = FilesUtils.getInternalFile(IMAGE_FILE);
        Json codes = qrCodeHelper.decodeQRCodeFromImage(file);

        Assert.assertEquals(2, codes.size());
        Assert.assertEquals("http://www.cnplus.co.uk", codes.object(0));
        Assert.assertEquals("http://www.architecturetoday.co.uk", codes.object(1));

    }

}
