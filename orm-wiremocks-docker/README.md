# ORM wiremock docker :whale:

## Prerequisites

- [Docker](https://www.docker.com)
- `am-docker` environment configured and running,
see [Run am-docker containers](#Run-am-docker-containers) for details

## Environment Variables

The following group of environment variables must be set in ORM to allow a local instance to connect to the wiremock:

| Variable                               | Path in [application.yml](../src/main/resources/application.yaml) | Description                                                                                  |
|----------------------------------------|-------------------------------------------------------------------|----------------------------------------------------------------------------------------------|
| `ORM_IDAM_CLIENT_ID`                   | `idam.client.id`                                                  | IDAM client ID: see client registered in [am-docker](https://github.com/hmcts/am-docker).    |
| `ORG_ROLE_MAPPING_IDAM_CLIENT_SECRET`  | `idam.client.secret`                                              | IDAM client secret paired with the above.                                                    |
| `ORG_ROLE_MAPPING_IDAM_ADMIN_SCOPE`    | `idam.client.admin.scope`                                         | IDAM client scope registered with the client ID above.                                       |
| `ORG_ROLE_MAPPING_IDAM_ADMIN_USERID`   | `idam.client.admin.userId`                                        | ORM system account: see users registered in [am-docker](https://github.com/hmcts/am-docker). |
| `ORG_ROLE_MAPPING_IDAM_ADMIN_PASSWORD` | `idam.client.admin.secret`                                        | ORM system account password paired with the above.                                           |
| `CASE_WORKER_REF_APP_URL`              | `feign.client.config.crdclient`                                   | URL to CRD wiremock: `"http://localhost:4099"`                                               |
| `JUDICIAL_REF_APP_URL`                 | `feign.client.config.jrdClient`                                   | URL to JRD wiremock: `"http://localhost:4099"`                                               |


## Run am-docker containers
- Install and run AM stack as advised [here](https://github.com/hmcts/am-docker).


## Run ORM wiremock containers

Please run ORM wiremocks docker as follows.

```bash
cd ./orm-wiremocks-docker/
docker-compose -f compose/orm-wiremocks.yml up -d
```
