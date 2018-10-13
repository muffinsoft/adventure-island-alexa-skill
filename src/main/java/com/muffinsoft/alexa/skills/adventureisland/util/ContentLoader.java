package com.muffinsoft.alexa.skills.adventureisland.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class ContentLoader {

    private static final Logger logger = LogManager.getLogger(ContentLoader.class);

    public <T> T loadContent(T object, String path, TypeReference typeReference) {
        try {
            URL url = ContentLoader.class.getClassLoader().getResource(path);
            File file = Paths.get(url.toURI()).toFile();
            object = new ObjectMapper().readValue(file, typeReference);
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        return object;
    }

}
