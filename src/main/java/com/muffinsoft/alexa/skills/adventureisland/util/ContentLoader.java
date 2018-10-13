package com.muffinsoft.alexa.skills.adventureisland.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ContentLoader {

    private static final Logger logger = LoggerFactory.getLogger(ContentLoader.class);

    public <T> T loadContent(T object, String path, TypeReference typeReference) {
        try {
            //URL url = ContentLoader.class.getClassLoader().getResource(path);
            //File file = Paths.get(url.toURI()).toFile();
            File file = new File(path);
            object = new ObjectMapper().readValue(file, typeReference);
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        return object;
    }

}
