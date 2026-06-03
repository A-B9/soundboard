package com.soundboard.soundboard.models;

import org.springframework.core.io.Resource;

public record AudioDownload(
        String contentType,
        Resource audioResource
) {
}
