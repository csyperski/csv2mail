package com.cwssoft.csv2mail.report;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



public class ReportUtils {

    private final static Logger logger = LoggerFactory.getLogger(ReportUtils.class);

    public static Optional<String> convertByteArrayToBase64(byte[] bytes) {
        if (bytes != null) {
            try {
                return Optional.ofNullable(new String(Base64.getEncoder().encode(bytes), "UTF8"));
            } catch (Exception e) {
                logger.warn("Unable to convert to base64: {}", e.getLocalizedMessage(), e);
            }
        }
        return Optional.empty();
    }

    public static byte[] combinePdfs(List<byte[]> pdfFiles) {
        if (pdfFiles != null) {

            PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
            pdfMergerUtility.addSources(pdfFiles.stream().filter(pdfFile -> pdfFile.length > 0 ).map( bytes -> new ByteArrayInputStream(bytes))
                    .collect(Collectors.toList())
            );
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                pdfMergerUtility.setDestinationStream(baos);
                pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                return baos.toByteArray();
            } catch ( Exception ex ) {
                logger.warn("Error merging PDF files: {}", ex.getMessage(), ex);
            }
        }
        return null;
    }



}
