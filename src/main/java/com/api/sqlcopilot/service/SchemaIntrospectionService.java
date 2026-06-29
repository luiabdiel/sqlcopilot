package com.api.sqlcopilot.service;

import com.api.sqlcopilot.exception.SchemaIntrospectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class SchemaIntrospectionService {

    private final String schema;

    public SchemaIntrospectionService() throws IOException {
        schema = new ClassPathResource("schema/schema.md")
                .getContentAsString(StandardCharsets.UTF_8);
    }

    public String introspect() {
        return schema;
    }
}
