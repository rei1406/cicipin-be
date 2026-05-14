package com.cicipin.userservice.common.versioning;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller or method with the API versions it supports.
 * Requests without X-API-Version header default to version 1.
 *
 * Example:
 *   @ApiVersion(1)         → handles v1 and any future version that has no override
 *   @ApiVersion(2)         → handles v2+ (overrides v1 for the same endpoint)
 *   @ApiVersion({1, 2})    → explicitly handles both v1 and v2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {
    int[] value();
}
