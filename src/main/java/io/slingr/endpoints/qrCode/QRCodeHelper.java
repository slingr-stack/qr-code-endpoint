package io.slingr.endpoints.qrCode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.slingr.endpoints.utils.Json;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;

public class QRCodeHelper {

    private Logger logger = LoggerFactory.getLogger(QrCodeEndpoint.class);

    private final String WIDTH = "width";
    private final String HEIGHT = "height";
    private final String FILE_TYPE = "fileType";
    private final String COLOR = "color";

    public File createQRImage(String qrCodeText, Json options) throws WriterException, IOException {

        int width = 250;
        if (options.integer(WIDTH) != null) {
            width = options.integer(WIDTH);
        }
        int height = 250;
        if (options.integer(HEIGHT) != null) {
            height = options.integer(HEIGHT);
        }
        String fileType = "png";
        if (options.string(FILE_TYPE) != null) {
            fileType = options.string(FILE_TYPE);
        }
        Color color = Color.BLACK;
        if (options.string(COLOR) != null) {
            try {
                color = Color.decode(options.string(COLOR));
            } catch (NumberFormatException nfe) {
                logger.info(String.format("Color %s can not be converted.", options.string(COLOR)));
                color = Color.BLACK;
            }
        }

        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable hintMap = new Hashtable();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, width, height, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        int matrixHeight = byteMatrix.getHeight();
        BufferedImage image = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(color);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        File temp = File.createTempFile("qr-code-" + new Date().getTime(), "." + fileType);
        ImageIO.write(image, fileType, temp);

        return temp;
    }

    public Json decodeQRCodeFromImage(InputStream fileInputStream) throws IOException {

        BufferedImage bufferedImage = ImageIO.read(fileInputStream);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        return getCodesFromBinary(bitmap);

    }

    private Json getCodesFromBinary(BinaryBitmap binaryBitmap){
        Json res = Json.list();
        try {
            QRCodeMultiReader multiReader = new QRCodeMultiReader();
            Result[] results = multiReader.decodeMultiple(binaryBitmap);

            for(Result r : results){
                res.push(r.getText());
            }
        } catch (NotFoundException e) {
            logger.warn("There is no QR code in the image");
            return null;
        }

        return res;
    }

    public Json decodeQRCodeFromPdf(InputStream inputStream) {
        Reader reader = new MultiFormatReader();

        Json codes = Json.list();

        try (final PDDocument document = PDDocument.load(inputStream)) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 500, ImageType.RGB);

                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bim)));

                Json tmp = getCodesFromBinary(binaryBitmap);
                if(tmp != null) {
                    for(Object t : tmp.toList()){
                        codes.push(t);
                    }
                }

            }
            document.close();

        } catch (IOException e) {
            System.err.println("Exception while trying to create pdf document - " + e);
        }

        return codes;

    }

}
