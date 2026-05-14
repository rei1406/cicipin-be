package com.cicipin.userservice.common.versioning;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import java.util.Arrays;

public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

    private static final String VERSION_HEADER = "X-API-Version";
    private static final int DEFAULT_VERSION = 1;

    private final int[] versions;

    public ApiVersionCondition(int[] versions) {
        this.versions = versions;
    }

    /**
     * When both type-level and method-level @ApiVersion exist, method-level wins.
     */
    @Override
    public ApiVersionCondition combine(ApiVersionCondition other) {
        return other;
    }

    /**
     * Core logic:
     * - Read X-API-Version header; default to 1 if absent or invalid.
     * - Match if the requested version >= the highest version this condition supports
     *   that doesn't exceed the requested version (i.e., best lower-or-equal match).
     */
    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        int requestedVersion = resolveVersion(request);

        // Find the highest version in this condition that is <= requestedVersion
        int bestMatch = Arrays.stream(versions)
                .filter(v -> v <= requestedVersion)
                .max()
                .orElse(-1);

        return bestMatch >= 0 ? this : null;
    }

    /**
     * Among multiple matching conditions, prefer the one with the highest version
     * (most specific match wins).
     */
    @Override
    public int compareTo(ApiVersionCondition other, HttpServletRequest request) {
        int thisMax = Arrays.stream(this.versions).max().orElse(0);
        int otherMax = Arrays.stream(other.versions).max().orElse(0);
        return otherMax - thisMax; // higher version = higher priority
    }

    private int resolveVersion(HttpServletRequest request) {
        String header = request.getHeader(VERSION_HEADER);
        if (header == null || header.isBlank()) {
            return DEFAULT_VERSION;
        }
        try {
            return Integer.parseInt(header.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_VERSION;
        }
    }
}
