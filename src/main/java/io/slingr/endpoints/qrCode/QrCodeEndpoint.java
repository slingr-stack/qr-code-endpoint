package io.slingr.endpoints.qrCode;

import com.google.zxing.WriterException;
import io.slingr.endpoints.Endpoint;
import io.slingr.endpoints.framework.annotations.ApplicationLogger;
import io.slingr.endpoints.framework.annotations.EndpointFunction;
import io.slingr.endpoints.framework.annotations.SlingrEndpoint;
import io.slingr.endpoints.services.AppLogs;
import io.slingr.endpoints.services.rest.DownloadedFile;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.ws.exchange.FunctionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.Executors;

/**
 * <p>QR Code endpoint
 * <p>
 * <p>Created by hpacini on 04/20/18.
 */
@SlingrEndpoint(name = "qr-code")
public class QrCodeEndpoint extends Endpoint {

    private Logger logger = LoggerFactory.getLogger(QrCodeEndpoint.class);
    private QRCodeHelper qrCodeHelper;

    @ApplicationLogger
    private AppLogs appLogger;

    @Override
    public void endpointStarted() {
        qrCodeHelper = new QRCodeHelper();
    }

    @EndpointFunction(name = "_generateQRCode")
    public Json generateQRCode(FunctionRequest request) {

        Json resp = Json.map();
        Json body = request.getJsonParams();

        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                Json options = body.json("options");

                String contentType = "image/png";
                if (options.string("fileType") != null) {
                    if (Arrays.asList(new String[]{"png", "jpg"}).contains(options.string("fileType"))) {
                        contentType = "image/" + options.string("fileType");
                    } else {
                        contentType = "image/png";
                        options.set("fileType", "png");
                    }
                }

                File temp = qrCodeHelper.createQRImage(body.string("data"), options);

                Json fileJson = files().upload(temp.getName(), new FileInputStream(temp), contentType);

                resp.set("status", "ok");
                resp.set("file", fileJson);

                events().send("qrResponse", resp, request.getFunctionId());

            } catch (IOException | WriterException e) {
                appLogger.warn("Error when create qr code", e);
                resp.set("status", "fail");
                resp.set("message", e.toString());

                events().send("qrResponse", resp, request.getFunctionId());
            }
        });

        resp.set("status", "ok");
        return resp;
    }

    @EndpointFunction(name = "_parseQRCode")
    public Json parseQRCode(FunctionRequest request) {

        Json resp = Json.map();
        Json body = request.getJsonParams();

        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            try {
                final String fileId = body.string("fileId");

                final DownloadedFile file = files().download(fileId);
                final InputStream is = file.getFile();

                final Json fileProps = files().metadata(fileId);
                final String contentType = fileProps.string("contentType");

                if ("application/pdf".equals(contentType)) {
                    Json res = qrCodeHelper.decodeQRCodeFromPdf(is);
                    resp.set("status", "ok");
                    resp.set("codes", res);

                } else if (Arrays.asList(new String[]{"image/jpg", "image/png"}).contains(contentType)) {
                    Json decodedCodes = qrCodeHelper.decodeQRCodeFromImage(is);
                    resp.set("status", "ok");
                    resp.set("codes", decodedCodes);

                } else {
                    resp.set("status", "fail");
                    resp.set("message", "File content type is not supported. Content type: " + contentType);
                }

                events().send("qrResponse", resp, request.getFunctionId());

            } catch (Exception e) {
                appLogger.warn("Error when create qr code", e);
                resp.set("status", "fail");
                resp.set("message", e.toString());

                events().send("qrResponse", resp, request.getFunctionId());
            }
        });

        resp.set("status", "ok");

        return resp;
    }

}
