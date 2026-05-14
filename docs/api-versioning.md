# API Versioning

The user service implements a custom **header-based API versioning** system. It does not use URL path versioning (e.g., `/v1/api/...`).

## How It Works

Clients send an `X-API-Version` header with an integer version number:

```
X-API-Version: 1
```

If the header is absent or contains a non-integer value, the server defaults to version `1`.

---

## Components

All versioning code lives in `com.cicipin.userservice.common.versioning`.

### `@ApiVersion` Annotation

```java
@ApiVersion(1)           // handles v1 and any future version with no override
@ApiVersion(2)           // handles v2+ (overrides v1 for the same endpoint)
@ApiVersion({1, 2})      // explicitly handles both v1 and v2
```

Can be placed on a **controller class** (applies to all methods) or on an **individual method** (overrides the class-level annotation).

### `ApiVersionCondition`

Contains the matching logic:

1. Reads the `X-API-Version` header from the request.
2. Defaults to `1` if absent or invalid.
3. Finds the **highest version** in the annotation's `value[]` that is **≤ the requested version**.
4. If such a version exists, the condition matches; otherwise it doesn't.

**Priority**: When multiple conditions match, the one with the **highest version number wins** (most specific match).

**Method vs. class**: When both a class-level and method-level `@ApiVersion` exist, the **method-level annotation wins** (via `combine()`).

### `VersionRequestMappingHandlerMapping`

Extends Spring's `RequestMappingHandlerMapping` to plug in the custom condition:

```java
@Override
protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
    ApiVersion annotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
    return annotation != null ? new ApiVersionCondition(annotation.value()) : null;
}

@Override
protected RequestCondition<?> getCustomMethodCondition(Method method) {
    ApiVersion annotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
    return annotation != null ? new ApiVersionCondition(annotation.value()) : null;
}
```

### `WebMvcConfig`

Registers the custom handler mapping by extending `WebMvcConfigurationSupport`:

```java
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new VersionRequestMappingHandlerMapping();
    }
}
```

---

## Usage Example

```java
// Handles all requests with X-API-Version: 1 (or no header)
@ApiVersion(1)
@RestController
@RequestMapping("/api/auth")
public class AuthController { ... }
```

To add a v2 variant of a single endpoint without duplicating the whole controller:

```java
@ApiVersion(1)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Handles v1 requests
    @PostMapping("/login")
    public ResponseEntity<?> loginV1(...) { ... }

    // Handles v2+ requests (overrides the class-level v1 for this method)
    @ApiVersion(2)
    @PostMapping("/login")
    public ResponseEntity<?> loginV2(...) { ... }
}
```

---

## Current Usage

Only `AuthController` currently uses `@ApiVersion(1)`. `UserController` and `HealthController` do not have the annotation, so they respond to all requests regardless of the header.

---

## Versioning Strategy

The "best lower-or-equal match" strategy means:

- A client sending `X-API-Version: 3` will match a `@ApiVersion(2)` handler if no `@ApiVersion(3)` handler exists.
- This provides **forward compatibility** — old clients don't break when new versions are added.
- New versions only need to be defined when behavior actually changes.
