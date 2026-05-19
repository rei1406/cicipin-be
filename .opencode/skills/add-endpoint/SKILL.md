---
name: add-endpoint
description: Add a new REST API endpoint following Cicipin conventions (layered, i18n, ApiResponse, @Valid)
---

## Steps

### 1. Create Request/Response DTOs

Place in the feature's `dto/` subpackage (e.g. `user/dto/`, `auth/dto/`). Use Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`). Use `@NotBlank`, `@Email`, `@Size`, etc. for validation.

### 2. Add Service Interface + Implementation

- Interface in the feature package (e.g. `user/UserService.java`)
- Implementation in `*Impl.java` (e.g. `user/UserServiceImpl.java`)
- Annotate impl with `@Service` and `@RequiredArgsConstructor`

### 3. Add Controller Endpoint

- Annotate with `@ApiVersion(n)`, `@RestController`, `@RequestMapping("/api/...")`, `@RequiredArgsConstructor`
- Inject the service interface and `MessageSource`
- Use `@Valid` on `@RequestBody` params
- Return `ResponseEntity<ApiResponse<T>>`
- Use a private `resolve(String code)` helper for i18n messages

### 4. Add i18n Messages

Add keys to both `messages.properties` (English) and `messages_id.properties` (Indonesian):
- `success.<endpoint>.<action>=...`
- `error.<endpoint>.<action>=...`

### 5. Handle Errors

If the endpoint needs new error types, define custom exception classes in the `exception/` package (extend `RuntimeException`, include `messageCode` + `args` fields). Update `GlobalExceptionHandler` if needed for new HTTP status codes.

### 6. Add Flyway Migration (if DB changes needed)

Create `user-service/src/main/resources/db/migration/V{n}__{description}.sql` — see `add-flyway-migration` skill.

### 7. Register Route in Gateway (if new path prefix)

Update `api-gateway/src/main/resources/application.yml` with a new route predicate if the path doesn't match existing patterns.

## Example Pattern

```java
@ApiVersion(1)
@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
public class ExampleController {
    private final ExampleService exampleService;
    private final MessageSource messageSource;

    private String resolve(String code) {
        try {
            return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    @PostMapping("/action")
    public ResponseEntity<ApiResponse<?>> action(@Valid @RequestBody ExampleRequest request) {
        ExampleResponse data = exampleService.doSomething(request);
        return ApiResponse.success(HttpStatus.OK, data, resolve("success.example.action"));
    }
}
```
