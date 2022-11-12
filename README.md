# mail-it

### Help commands

#### Build native executable
```shell
./gradlew distribution:assemble -Dquarkus.package.type=native -PdatabaseProvider=postgresql
```

#### To set up git hooks
```shell
./gradlew copyGitHooks
```
