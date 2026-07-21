package com.cwssoft.csv2mail.mail;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;

public class DescriptionDataSource extends ByteArrayDataSource {

    private final String description;

    public DescriptionDataSource(byte[] data, String type) {
        super(data, type);
        this.description = null;
    }

    public DescriptionDataSource(byte[] data, String type, String name, String description) {
        super(data, type);
        this.description = description;
        this.setName(name);
    }

    public DescriptionDataSource(InputStream stream, String type, String name, String description) throws IOException {
        super(stream, type);
        this.description = description;
        this.setName(name);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String getContentType() {
        return super.getContentType();
    }

    public String getDescription() {
        return description;
    }
}
