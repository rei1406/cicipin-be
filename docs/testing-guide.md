# Testing Guide

## Email Service

### Test Classes

| Class | Type | Runs by default |
|---|---|---|
| `EmailServiceImplTest` | Unit (Mockito, no SMTP) | ✅ Yes |
| `EmailServiceMailpitTest` | Integration (Mailpit mail catcher) | ✅ Yes |
| `EmailServiceIntegrationTest` | Integration (real Gmail SMTP) | ❌ No (tagged `gmail`) |

---

### Mailpit Tests

Sends emails to a local mail catcher and asserts on the captured messages via Mailpit's REST API — subject, recipient, and body content.

Mailpit is included in `docker-compose.dev.yml` (SMTP on port 1025, REST API + web UI on port 8025). No credentials needed.

**Run:**
```bash
# All default tests (includes Mailpit)
./dev.sh exec email-service mvn test

# Mailpit tests only
./dev.sh exec email-service mvn test -Dtest=EmailServiceMailpitTest
```

Open `http://localhost:8025` to inspect captured emails visually.

---

### Gmail SMTP Tests

Sends real emails via Gmail SMTP and verifies delivery. Excluded from the default run — must be opted in explicitly.

**Requires** `MAIL_USERNAME` and `MAIL_PASSWORD` set in `.env.dev`. `MAIL_PASSWORD` must be a Gmail App Password (16 characters), not your account password.

**Run:**
```bash
./dev.sh exec email-service mvn test -Dsurefire.excludedGroups= -Dsurefire.groups=gmail
```

> Both flags are required. `-Dsurefire.groups=gmail` alone does **not** override `excludedGroups` — you must explicitly clear it with `-Dsurefire.excludedGroups=`.

---

### Unit Tests

No SMTP, no Docker. Uses Mockito to mock `JavaMailSender` and `TemplateEngine`.

**Run:**
```bash
./dev.sh exec email-service mvn test -Dtest=EmailServiceImplTest
```
