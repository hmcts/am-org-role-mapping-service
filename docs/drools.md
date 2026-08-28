# How Drools is used

Drools is the project’s rules engine for converting user access data into organisational role assignments.

The flow is:

1. The service retrieves user profiles from the relevant upstream service. These are flattened into facts such as `CaseWorkerAccessProfile` and `JudicialAccessProfile`.

2. For judicial users, it may also retrieve `JudicialBooking` data.

3. `RequestMappingService` inserts these objects, plus database-backed `FeatureFlag` objects, into Drools working memory:

   - access profiles
   - feature flags
   - judicial bookings
   - then fires all rules

   See [`RequestMappingService.java:185`](../src/main/java/uk/gov/hmcts/reform/orgrolemapping/domain/service/RequestMappingService.java:185).

4. Matching `.drl` rules create `RoleAssignment` objects. For example, the SSCS rules match a role ID, service code, suspension status, and feature flag, then create roles such as `hmcts-admin`, `hearing-centre-admin`, or `clerk`:

   [`sscs-admin-mapping.drl:23`](../src/main/resources/validationrules/sscs/sscs-admin-mapping.drl:23)

5. The generated assignments are retrieved using the `getRoleAssignments` Drools query and de-duplicated:

   [`core.drl:7`](../src/main/resources/validationrules/core/core.drl:7)

6. The service groups assignments by user and sends them to the Role Assignment Service, using either the staff or judicial organisational mapping process:

   [`RequestMappingService.java:265`](../src/main/java/uk/gov/hmcts/reform/orgrolemapping/domain/service/RequestMappingService.java:265)

The rules are organised by jurisdiction and role type:

- `core`
- `civil`
- `employment`
- `iac`
- `sscs`
- `privatelaw`
- `publiclaw`
- `stcic`
- `fr`
- `probate`
- `possessions`

They cover administrative, caseworker, CTSC, judicial, judicial-office-holder, hearing, and multi-region mappings. All these packages are loaded into the same KIE base and stateless session in [`kmodule.xml:3`](../src/main/resources/META-INF/kmodule.xml:3).

Drools is configured as a Spring bean using a classpath KIE container and a `StatelessKieSession`:

[`DroolConfig.java:16`](../src/main/java/uk/gov/hmcts/reform/orgrolemapping/config/DroolConfig.java:16)

Feature flags act as rule switches. A rule generally requires a flag such as `SSCS_WA_1_0` or `PRIVATELAW_WA_1_6` to be enabled, allowing new mappings to be rolled out or retired without changing Java orchestration code.

For judicial mappings, rules can work in stages: judicial access profiles may first produce `JudicialOfficeHolder` facts, and subsequent rules use those facts—sometimes together with bookings—to create final judicial role assignments. The generated assignments are then sent to the external Role Assignment Service; Drools itself does not persist them.

## How the rules are triggered

Drools is not triggered only by a REST request. The application supports these runtime entry points:

| Method | Endpoint or source | How it triggers the rules |
| --- | --- | --- |
| `POST` | `/am/role-mapping/refresh?jobId={jobId}` | Asynchronously refreshes assignments for a refresh job. The job can represent caseworker or judicial users. |
| `POST` | `/am/role-mapping/judicial/refresh` | Directly refreshes judicial assignments. |
| `POST` | `/am/testing-support/createOrgMapping?userType=CASEWORKER` | Directly runs caseworker mapping rules. Available only when testing support is enabled. |
| `POST` | `/am/testing-support/createOrgMapping?userType=JUDICIAL` | Directly runs judicial mapping rules. Available only when testing support is enabled. |
| `POST` | `/am/role-mapping/staff/users` | Deprecated legacy equivalent of `createOrgMapping`; it takes `userType` in a request header. |
| Azure Service Bus | CRD topic | A consumed message runs the caseworker mapping flow. |
| Azure Service Bus | JRD topic | A consumed message runs the judicial mapping flow. |

The testing-support endpoints `/am/testing-support/send2CrdTopic` and `/am/testing-support/send2JrdTopic` publish messages to the corresponding Service Bus topics, indirectly triggering the mapping flow when the consumers are enabled.

The `/am/role-mapping/professional/refresh` endpoint and the scheduled professional refresh jobs use the professional access-type mapping logic; they do not invoke Drools.

All direct mapping paths converge on `RequestMappingService`, which inserts facts, fires the rules, and retrieves the generated assignments:

[`RequestMappingService.java:191`](../src/main/java/uk/gov/hmcts/reform/orgrolemapping/domain/service/RequestMappingService.java:191)
