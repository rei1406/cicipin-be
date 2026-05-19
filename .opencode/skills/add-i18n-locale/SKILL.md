---
name: add-i18n-locale
description: Add a new locale to all services (messages file + LocaleResolver config)
---

## Steps

### 1. Create Messages File for Each Service

Copy the existing properties file for each service that has i18n:

| Service | Existing File | New File |
|---|---|---|
| user-service | `user-service/src/main/resources/messages.properties` | `messages_{locale}.properties` |
| email-service | `email-service/src/main/resources/messages.properties` | `messages_{locale}.properties` |

Translate all keys from the `.properties` file.

### 2. Update LocaleResolver in Each Service

Find the `WebMvcConfig` or `I18nConfig` class in each service and add the new locale to `setSupportedLocales(...)`:

```java
resolver.setSupportedLocales(List.of(
    Locale.of("en"),
    Locale.of("id"),
    Locale.of("{locale_code}")
));
```

### 3. Key Files Per Service

| Service | Config File |
|---|---|
| user-service | `user-service/src/main/java/com/cicipin/userservice/config/WebMvcConfig.java` (or `I18nConfig.java`) |
| email-service | `email-service/src/main/java/com/cicipin/emailservice/config/WebMvcConfig.java` (or `I18nConfig.java`) |

**Note:** `api-gateway` is a reactive WebFlux app with no i18n — it just passes `X-Locale` through.

### 4. Testing

Restart the service and test with the `X-Locale` header:

```bash
curl -H "X-Locale: {locale_code}" localhost:8080/api/auth/register
```
