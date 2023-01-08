# mail-it

Open source e-mail service featuring template management and processing

## Building mail-it

### Requirements
- JDK 17
- Kotlin 1.7.21
- GraalVM 22.3.0 (for native build only)
- Node.js 18 and [yarn](https://yarnpkg.com/getting-started/install)
- Docker (for tests only)

### Building artifact

To build the artifact run the following command: 
```shell
./gradlew clean distribution:build -Dquarkus.package.type=${PACKAGE_TYPE} -PdatabaseProvider=${DATABASE_PROVIDER} -Pconnectors=${CONNECTORS}
```

To skip tests replace `distribution:build` with `distribution:assemble`

| Variable name     | Description                                                                                                |
|-------------------|------------------------------------------------------------------------------------------------------------|
| PACKAGE_TYPE      | Type of result artifact. Possible values are `uber-jar` or `native` (graalvm is required)                  | 
| DATABASE_PROVIDER | Database that will be used by this application. Possible values are `h2` (set by default) and `postgresql` |
| CONNECTORS        | List of connectors for this application. Only `http` is supported (set by default)                         |

Please, note that `h2` database runs in embedded mode only, thus all data is lost on every application restart, so this mode is appropriate only for dev purposes. Also, `native` packaging doesn't support `h2` database 

Resulting artifact path is `distribution/build/mail-it-VERSION-runner.jar` in case of `uber-jar` or `distribution/build/mail-it-VERSION-runner` in case of `native` 
