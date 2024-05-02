# supreme medicator

## Setup instructions

### Setting up the project

1. Install IntelliJ IDEA
   - It is recommended to use IntelliJ IDEA Ultimate Edition to configure and run this project for development and debugging purposes
2. Clone the project with git
3. Open the root directory of the project in IntelliJ
4. If IntelliJ asks to install anything, accept
5. Open Run/Debug configurations and ensure a Spring Boot application is recognized with location `com.suprememedicator.suprememedicator.SupremeMedicatorApplication` with JDK 18.
   - If this was not automatically recognized, try restarting the IDE and clicking accept on anything IntelliJ offers
   - If this still doesn't work, [create the configuration manually](https://www.jetbrains.com/help/idea/run-debug-configuration-spring-boot.html)
6. Still in the Run/Debug configurations window, add a `dev` profile
7. In the directory `suprememedicator/src/main/resources`, create a file `application-dev.yml` with the following content:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/suprememedicator
    username: postgres
    password: postgres
open-ai:
  api-key: my-secret-open-ai-api-key
suprememedicator:
  database:
    import:
      should-import: false
      dataset:
        path: out/full database.xml
```

### OpenAI API Key

1. Create or obtain an API key for using the OpenAI GPT services
2. In `application-dev.yml`, edit the parameter `open-ai.api-key` with your API key

### PostgreSQL

1. Install PostgreSQL
2. Create a new database named `suprememedicator`
3. If necessary, in `application-dev.yml`, edit parameters such as `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`

### DrugBank dataset

1. Obtain the DrugBank dataset in XML format from [here](https://go.drugbank.com/releases/latest)
2. In the project root directory, create an `out` directory if it does not exist
3. Place the dataset XML file in the `out` directory
4. If necessary, in `application-dev.yml`, edit the parameter `suprememedicator.database.import.dataset.path`

## Run instructions

If running the application for the first time:
- The first time the application is run, the database should be imported. This can be done by going to `application-dev.yml` and setting `suprememedicator.database.import.should-import` to `true`. Make sure to set this to `false` any future time you run the application.

Then, simply run the application through IntelliJ as you would run any Spring application.

The website is at [http://localhost:8080/](http://localhost:8080/) by default.