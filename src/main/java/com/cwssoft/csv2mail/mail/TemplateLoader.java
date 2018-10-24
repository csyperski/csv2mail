package com.cwssoft.csv2mail.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Optional;
import java.util.stream.Collectors;

public class TemplateLoader {

    private final static Logger logger = LoggerFactory.getLogger(TemplateLoader.class);

    public static Optional<BodyTemplate> load(String file) {
        try {
            String body = Files.readAllLines(FileSystems.getDefault().getPath(file))
                    .stream()
                    .collect(Collectors.joining("\n"));
            return Optional.of(new BodyTemplate(body));
        } catch (IOException ioEx) {
            logger.warn("Failed to open: {} due to: {}", file, ioEx.getMessage(), ioEx);
        }
        return Optional.empty();
    }

}
