# mail-it

Open source e-mail service featuring template management and processing

## Building mail-it

### Requirements

- JDK 17
- Kotlin 1.8
- GraalVM 22.3 (for native build only)
- Node.js 18
- Docker (for tests only)

### Building artifact

To build the artifact run the following command:

```shell
./gradlew clean distribution:build \
  -Dquarkus.package.type=${PACKAGE_TYPE} \
  -PdatabaseProvider=${DATABASE_PROVIDER} \
  -Pconnectors=${CONNECTORS} \
  -PwithUi
```

To skip tests replace `distribution:build` with `distribution:assemble`

| Variable name     | Description                                                                                                |
|-------------------|------------------------------------------------------------------------------------------------------------|
| PACKAGE_TYPE      | Type of result artifact. Possible values are `uber-jar` or `native` (graalvm is required)                  | 
| DATABASE_PROVIDER | Database that will be used by this application. Possible values are `h2` (set by default) and `postgresql` |
| CONNECTORS        | List of connectors for this application. Only `http` is supported (set by default)                         |

Please, note that `h2` database runs in embedded mode only, thus all data is lost on every application restart, so this mode is appropriate only for dev
purposes. Also, `native` packaging doesn't support `h2` database

Resulting artifact path is `distribution/build/mail-it-VERSION-runner.jar` in case of `uber-jar` or `distribution/build/mail-it-VERSION-runner` in case
of `native`

## Configuring mail-it

The service is configured via environment variables. List of all possible variables is below 

### Mailer
| Variable name      | Default value | Description                                                                                                                                                                                                                                                       |
|--------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SMTP_AUTH_METHODS  | `none`        | Sets the allowed authentication methods. These methods will be used only if the server supports them. If not set, all supported methods may be used. The list is given as a space separated list, such as `DIGEST-MD5 CRAM-SHA256 CRAM-SHA1 CRAM-MD5 PLAIN LOGIN` |
| SMTP_HOST          | host.not.set  | The SMTP hostname                                                                                                                                                                                                                                                 |
| SMTP_PORT          | `none`        | The SMTP port                                                                                                                                                                                                                                                     |  
| SMTP_SSL           | false         | Enables or disables the TLS/SSL                                                                                                                                                                                                                                   |
| SMTP_TLS           | DISABLED      | Sets the TLS security mode for the connection. Either `DISABLED`, `OPTIONAL` or `REQUIRED`                                                                                                                                                                        |   
| SMTP_USERNAME      | `none`        | Username to connect to the SMTP server                                                                                                                                                                                                                            |  
| SMTP_PASSWORD      | `none`        | Password to connect to the SMTP server                                                                                                                                                                                                                            |  
| DEFAULT_EMAIL_FROM | `none`        | Default sender address for all emails                                                                                                                                                                                                                             |

### Admin client
| Variable name          | Default value                 | Description                                                             |
|------------------------|-------------------------------|-------------------------------------------------------------------------|
| ADMIN_CLIENT_USERNAME  | username                      | Username of a "user" to access the admin console                        |
| ADMIN_CLIENT_PASSWORD  | password                      | Password of a "user" to access the admin console                        |
| SESSION_ENCRYPTION_KEY | change-me-change-me-change-me | Encryption key for session cookies. Must be at least 16 characters long |

### Connectors

#### Http
todo openapi

Nothing to configure

### Databases

#### H2
Nothing to configure

#### Postgresql
| Variable name     | Default value                       | Description       |
|-------------------|-------------------------------------|-------------------|
| DATABASE_URL      | postgresql://localhost:5432/mail_it | Database url      |
| DATABASE_USERNAME | admin                               | Database username |
| DATABASE_PASSWORD | admin                               | Database password |
