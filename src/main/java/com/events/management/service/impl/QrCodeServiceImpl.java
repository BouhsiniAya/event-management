package com.events.management.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrCodeServiceImpl {

    private static final Logger log =
            LoggerFactory.getLogger(QrCodeServiceImpl.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String generateQrCode(String ticketCode)
            throws WriterException, IOException {

        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                "TICKET:" + ticketCode,
                BarcodeFormat.QR_CODE,
                300, 300, hints
        );

        String fileName = "qr_" + ticketCode + ".png";
        Path outputPath = FileSystems.getDefault()
                .getPath(uploadDir + "/" + fileName);

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", outputPath);
        log.info("QR Code généré : {}", outputPath);
        return uploadDir + "/" + fileName;
    }

    public byte[] generateQrCodeBytes(String ticketCode)
            throws WriterException, IOException {

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
                "TICKET:" + ticketCode,
                BarcodeFormat.QR_CODE,
                300, 300, hints
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}