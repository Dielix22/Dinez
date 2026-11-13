package com.adres.urgencias.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

class SpaFallbackResourceResolverTest {

    private final SpaFallbackResourceResolver resolver = new SpaFallbackResourceResolver();

    @TempDir
    Path staticDir;

    private Resource location;

    @BeforeEach
    void createFrontendFiles() throws IOException {
        Files.writeString(staticDir.resolve("index.html"), "<html></html>");
        Files.createDirectories(staticDir.resolve("assets"));
        Files.writeString(staticDir.resolve("assets/app.js"), "console.log(1);");
        location = new FileSystemResource(staticDir.toString() + "/");
    }

    @Test
    void servesExistingFileAsIs() throws IOException {
        Resource resource = resolver.getResource("assets/app.js", location);

        assertThat(resource).isNotNull();
        assertThat(resource.getFilename()).isEqualTo("app.js");
    }

    @Test
    void servesEntryPageForUnknownClientSideRoute() throws IOException {
        Resource resource = resolver.getResource("turns/42", location);

        assertThat(resource).isNotNull();
        assertThat(resource.getFilename()).isEqualTo("index.html");
    }

    @Test
    void returnsNothingForMissingFileWithExtension() throws IOException {
        assertThat(resolver.getResource("assets/missing.js", location)).isNull();
    }

    @Test
    void returnsNothingForUnknownApiPath() throws IOException {
        assertThat(resolver.getResource("api/unknown", location)).isNull();
    }

    @Test
    void returnsNothingWhenFrontEndIsNotBuilt() throws IOException {
        Files.delete(staticDir.resolve("index.html"));

        assertThat(resolver.getResource("turns", location)).isNull();
    }
}
