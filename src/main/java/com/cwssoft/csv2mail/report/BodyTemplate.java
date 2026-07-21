package com.cwssoft.csv2mail.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import static com.cwssoft.csv2mail.report.ReportUtils.combinePdfs;
import static com.cwssoft.csv2mail.report.ReportUtils.convertByteArrayToBase64;

public class BodyTemplate {

    private final static Logger logger = LoggerFactory.getLogger(BodyTemplate.class);

    private String body;

    public BodyTemplate(String body) {
        this.body = body;
    }

    public Optional<String> processAsHtml(List<String[]> data) throws IOException {
        return generateHtml(data);
    }

    public Optional<byte[]> processAsPdf(List<String[]> data) throws IOException {
        return generatePdf(data);
    }

    protected Optional<byte[]> generatePdf(List<String[]> data) throws IOException {
        Optional<byte[]> pdfData =  generateBytesPdf(data);
        if (pdfData.isPresent() && pdfData.get().length > 0) {
            final byte[] finalPdf = combinePdfs(Collections.singletonList(pdfData.get()));
            if (finalPdf != null && finalPdf.length > 0) {
                return Optional.of(finalPdf);
            }
        }
        return Optional.empty();
    }

    protected Optional<byte[]> generateBytesPdf(List<String[]> data) throws IOException {
        final Optional<String> maybeHtml = generateHtml(data);
        if (maybeHtml.isPresent()) {
            final String html = maybeHtml.orElse(null);
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.toStream(outputStream);
                builder.withHtmlContent(html, "");
                builder.buildPdfRenderer();
                builder.run();

                return Optional.ofNullable(outputStream.toByteArray());
            }
        }
        return Optional.empty();
    }

    protected Optional<String> generateHtml(List<String[]> data) {
        if (data != null && body != null) {
            TemplateEngine templateEngine = new TemplateEngine();
            final Map<String, String> values = new HashMap<>();

            Context context = new Context();
            context.setVariable("data", data);

            return Optional.of(templateEngine.process(body, context));
        }
        return Optional.empty();
    }
}
