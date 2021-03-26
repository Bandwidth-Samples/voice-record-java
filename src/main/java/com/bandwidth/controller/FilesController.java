package com.bandwidth.controller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("files")
public class FilesController {

    Logger logger = LoggerFactory.getLogger(FilesController.class);

    @GetMapping(
            value = "/tone",
            produces = "audio/mp3"
    )
    public @ResponseBody byte[] getTone() throws IOException {
        Resource resource = new ClassPathResource("Tone.mp3");

        InputStream stream = resource.getInputStream();

        return IOUtils.toByteArray(stream);
    }
}
