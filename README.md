# am-org-role-mapping-service

Organisation Role Mapping Service


## Purpose

It provisions various organisation roles for staff & judicial users based on the service specific mapping rules. It is implemented as a Java/SpringBoot application.

### Prerequisites

To run the project you will need to have the following installed:

* Java 11
* Docker

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

### Running the application

To run the API quickly use the docker helper script as follows:

```
./bin/run-in-docker.sh install
```
or

```
docker-compose up
```


Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

If required, to run with a low memory consumption, the following can be used:

```
./gradlew --no-daemon assemble && java -Xmx384m -jar build/libs/rd-case-worker-api.jar
```

### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:4098/health
```

If the API is running, you should see response like below:

```
{"status":"UP"}
```

### DB Initialisation˙

The application uses a Postgres database which can be run through a docker container on its own if required.
The application should automatically apply any database migrations using flyway.

### Running unit tests tests:

If you have some time to spare, you can run the *unit tests* as follows:

```
./gradlew test
```

### Running integration tests:


You can run the *integration tests* as follows:

```
./gradlew integration
```

### Running functional tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *functional tests* as follows:

```
./gradlew functional
```

If you want to run a specific scenario use this command:

```
./gradlew functional --tests <TestClassName> --info -Dscenario=<Scenario>
```

### Running smoke tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *smoke tests* as follows:

```
./gradlew smoke
```

### Running mutation tests tests:

You can run the *mutation tests* as follows:

```
./gradlew pitest
```

As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.

More information about mutation testing can be found here:
http://pitest.org/

### Testing in Postman

To test in Postman the easiest way is to start this service using the ./bin/run-in-docker.sh script.  The in postman paste the following script:

```
pm.sendRequest('http://127.0.0.1:4098/token', function (err, res) {
    if (err) {
        console.log(err);
    } else {
        pm.environment.set("token", res.text());
    }
});
```
into the pre-script window.  Also add a header as follows:

```
ServiceAuthorization: Bearer {{token}}
```

Authorization :  Bearer copy IDAM access token

### Contract testing with pact

Please refer to the confluence on how to run and publish PACT tests.
https://tools.hmcts.net/confluence/display/RTRD/PACT+testing


