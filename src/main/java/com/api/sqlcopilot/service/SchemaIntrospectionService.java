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

    public String introspect() {
        try {
            ClassPathResource resource = new ClassPathResource("schema/schema.md");
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.error("Schema file not found");
            throw new SchemaIntrospectionException(ex);
        }
    }
}
