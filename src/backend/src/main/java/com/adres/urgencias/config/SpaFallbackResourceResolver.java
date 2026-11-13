package com.adres.urgencias.config;

import java.io.IOException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Resolves static front-end files and falls back to {@code index.html} for client-side routes.
 *
 * <p>The React application handles its own routes (e.g. {@code /turns}). A browser refresh on such
 * a route reaches the server, which has no file with that name: the resolver then serves the entry
 * page so the front end can render the route. Requests that must stay real 404s are never
 * redirected: API calls and missing files (paths whose last segment has an extension).
 */
public class SpaFallbackResourceResolver extends PathResourceResolver {

    static final String ENTRY_PAGE = "index.html";
    static final String API_PREFIX = "api/";

    @Override
    protected @Nullable Resource getResource(String resourcePath, Resource location)
            throws IOException {
        Resource requested = location.createRelative(resourcePath);
        if (requested.isReadable()) {
            return requested;
        }
        if (isClientSideRoute(resourcePath)) {
            Resource entryPage = location.createRelative(ENTRY_PAGE);
            return entryPage.isReadable() ? entryPage : null;
        }
        return null;
    }

    private boolean isClientSideRoute(String resourcePath) {
        return !resourcePath.startsWith(API_PREFIX) && !hasFileExtension(resourcePath);
    }

    private boolean hasFileExtension(String resourcePath) {
        String lastSegment = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        return lastSegment.contains(".");
    }
}
