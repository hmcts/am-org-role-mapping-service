# Financial Remedy (Consented & Contested) Work Allocation Onboarding Guide

## Overview

This document combines:

1. The generic Work Allocation onboarding process
2. Consented & Contested Financial Remedy (FR) service configuration
3. Required changes across:

   * Ref Data
   * Access Management
   * Org Role Mapping (ORM)
   * Role Assignment Service (RAS)
   * Task Management
   * Testing & Release

---

# 1. Service Information

| Property       | Value                                         |
| -------------- | --------------------------------------------- |
| Service        | Financial Remedy                              |
| Service Code   | ABA2                                          |
| Jurisdiction   | DIVORCE                                       |
| CCD Case Types | FinancialRemedyMVP2, FinancialRemedyContested |
| Ticket Code    | 410                                           |

## Location Reference Data Mapping

| service_code | ccd_service_name | ccd_case_type            |
| ------------ | ---------------- | ------------------------ |
| ABA2         | DIVORCE          | FinancialRemedyContested |
| ABA2         | DIVORCE          | FinancialRemedyMVP2      |
| ABA2         | DIVORCE          | FINREM_ExceptionRecord   |

---

# 2. Onboarding Process

## 2.1 Reference Data

### Jurisdiction

```json
{
  "ID": "DIVORCE",
  "Name": "Family Divorce",
  "Description": "Family Divorce: Dissolution of Marriage"
}
```

### Case Types

| Case Type Name             | CCD Identifier           |
| -------------------------- | ------------------------ |
| Financial Remedy Consented | FinancialRemedyMVP2      |
| Financial Remedy Contested | FinancialRemedyContested |

---

# 3. Org Role Mapping (ORM) Changes

For a new service onboarding, complete the following:

## Feature Flags

* Create flag enum
* Create DB migration
* Enable only in:

  * Local
  * Preview
* Enable additional environments through:

  * Flux changes
  * Migration script

## Work Areas

Add:

* ABA2 Work Area
* Service Code Mapping

## Jurisdiction

Add:

* DIVORCE jurisdiction

## Drools Configuration

Update:

```text
validationrules.financialremedy
```

Required rule files:

* admin
* judicial-org-role
* judicial-office-holder
* caseworker
* ctsc

### Judicial Roles

| Role                               |
| ---------------------------------- |
| hmcts-judiciary                    |
| judge                              |
| fee-paid-judge                     |
| leadership-judge                   |
| task-supervisor                    |
| case-allocator                     |
| specific-access-approver-judiciary |

### Staff Roles

| Role                               |
| ---------------------------------- |
| hmcts-ctsc                         |
| hmcts-admin                        |
| task-supervisor                    |
| case-allocator                     |
| specific-access-approver-ctsc      |
| specific-access-approver-admin     |
| specific-access-approver-legal-ops |

---

## ORM Unit Tests

Update:

### JudicialOfficeMapping

Test all:

* Circuit Judges
* District Judges
* High Court Judges
* Deputy Judges
* Tribunal Judges

### JudicialRoleMapping

Test:

* Judge
* Fee Paid Judge
* Leadership Judge
* Task Supervisor
* Case Allocator

### StaffOrgRoles

Test:

* CTSC
* Admin
* Team Leaders
* NBC Roles
* Specific Access Roles

---

# 4. Role Assignment Service (RAS) Changes

## Feature Flags

* Create enum
* Create DB migration
* Enable local & preview

## Validation Rules

Update:

```text
validationrules.financialremedy
```

### Role Common

Add DIVORCE jurisdiction for:

* Judge roles
* Staff roles
* Specific access roles

### Specific Access Rules

Add:

```text
specific-access-global
```

### Fallback Data

Update:

```java
DataStoreApiFallback
```

Add:

```text
DIVORCE
```

---

## RAS Tests

Update:

### Required Tests

* AllServicesOrgRoleTest
* CaseRolesDroolsTest
* SpecificAccessDroolsTest
* DroolBase
* RoleAssignmentIntegrationTest

Update expected role counts accordingly.

---

# 5. Judicial Configuration

## Judicial Appointments

Supported appointments include:

* Circuit Judge
* Senior Circuit Judge
* Specialist Circuit Judge
* High Court Judge
* District Judge
* Recorder
* Deputy High Court Judge
* Deputy District Judge
* Deputy Costs Judge
* Tribunal Judge
* Tribunal Member Disability

### Generic Role Mapping

| Appointment Type  | Generic Role             |
| ----------------- | ------------------------ |
| Fee Paid          | Generic Fee Paid         |
| Salaried          | Generic Salaried         |
| Leadership Judges | Generic Leadership Judge |

---

## Judicial Organisational Roles

### Fee Paid Judges

Assigned:

* hmcts-judiciary
* judge
* fee-paid-judge

### Salaried Judges

Assigned:

* hmcts-judiciary
* judge

### Leadership Judges

Assigned:

* hmcts-judiciary
* judge
* leadership-judge
* task-supervisor
* case-allocator
* specific-access-approver-judiciary

---

## Judicial Case Roles

Supported case roles:

* allocated-judge
* hearing-judge
* lead-judge
* case-allocator

Assignment authority:

* case-allocator organisational role

---

# 6. Staff Configuration

## Staff Business Roles

### CTSC

| Role               | ID |
| ------------------ | -- |
| CTSC Administrator | 10 |
| CTSC Team Leader   | 9  |

### Admin

| Role                                   | ID |
| -------------------------------------- | -- |
| Hearing Centre Administrator           | 4  |
| Hearing Centre Team Leader             | 3  |
| National Business Centre Administrator | 11 |
| National Business Centre Team Leader   | 6  |

---

## Staff Organisational Roles

### CTSC

* hmcts-ctsc
* ctsc
* ctsc-team-leader
* task-supervisor
* case-allocator
* specific-access-approver-ctsc

### Admin

* hmcts-admin
* hearing-centre-admin
* hearing-centre-team-leader
* national-business-centre
* nbc-team-leader
* task-supervisor
* case-allocator
* specific-access-approver-admin
* specific-access-approver-legal-ops

---

## Staff Case Roles

### Restricted Case Roles

* allocated-ctsc-caseworker
* allocated-admin-caseworker
* case-allocator

---

# 7. Reference Data Skills

## Consented Financial Remedy

| Skill                            |
| -------------------------------- |
| FR_Managing_ScannedDocs          |
| FR_Checking_HWF                  |
| FR_Checking_Applications         |
| FR_Processing_Orders             |
| FR_Closing_Cases                 |
| FR_ReviewingConsent_Applications |

## Contested Financial Remedy

| Skill                                     |
| ----------------------------------------- |
| Contested_Processing_General_Applications |
| Contested_Processing_Consent_Applications |
| Contested_Checking_HWF                    |
| Contested_Checking_Applications           |
| Contested_Issuing_Applications            |
| Contested_Progressing_Applications        |
| Contested_Amending_Orders                 |
| Contested_General_Queries                 |
| Contested_Closing_Cases                   |
| Contested_Managing_Scanned_Docs           |
| Contested_Managing_Hearings               |
| Contested_Processing_Orders               |
| Contested_Sending_Orders                  |
| Contested_Uploading_Draft_Orders          |
| Contested_Handling_Urgent_Cases           |
| Contested_Reallocating_Cases              |

---

# 8. Specific Access & Challenged Access

## Specific Access Roles

* specific-access-judiciary
* specific-access-legal-ops
* specific-access-admin
* specific-access-ctsc

Approval Roles:

* leadership-judge
* CTSC Team Leader
* Hearing Centre Team Leader

## Challenged Access

Provided through standard Work Allocation capability.

Role:

```text
challenged-access-judiciary
```

Attributes include:

* caseId
* jurisdiction
* caseType
* authorisations
* justification notes

---

# 9. Task Management

## Consented Work Types

| Work Type            |
| -------------------- |
| hearing_work         |
| routine_work         |
| decision_making_work |
| applications         |
| review_case          |
| evidence             |

## Contested Work Types

| Work Type                      |
| ------------------------------ |
| contested_hearing_work         |
| contested_routine_work         |
| contested_decision_making_work |
| contested_applications         |
| contested_evidence             |
| contested_pre_hearing          |
| contested_post_hearing         |

---

## Specific Access Tasks

| Task      | Approver                   |
| --------- | -------------------------- |
| Judiciary | leadership-judge           |
| CTSC      | CTSC Team Leader           |
| Admin     | Hearing Centre Team Leader |
| Legal Ops | CTSC Team Leader           |

SLA:

```text
2 days
```

---

## Consented Service Tasks

Examples:

* Process Scanned Documents
* Check HWF
* Check & Issue Application
* Check Response Received
* Process Approved Order
* Review Refused Order
* Review Order Response
* Review Application

---

## Contested Service Tasks

Examples:

* Check HWF
* Check Application
* Progress Application
* Process Scanned Documents
* Review Application (Gatekeeping)
* Schedule Hearing
* Review Order
* Process Order
* Check General Application
* Review General Application
* Check Consent Application
* Review Consent Application
* Process General Application
* Process Consent Application
* Send Order Drawn

---

# 10. DMN Requirements

## Task Initiation DMN

Inputs:

* CCD Event
* CCD State
* Case Data

Outputs:

* Task ID
* Task Name
* Delay
* Working Days Allowed
* Process Category

---

## Task Configuration DMN

Maps:

* Case data
* Task attributes
* Role category
* Location
* Region
* Priority

---

## Task Permissions DMN

Maps:

* Task Type
* Role Category
* Authorisations
* Assignment Priority
* Auto Assignment

---

## Task Cancellation DMN

Current Rule:

| Event     | Action           |
| --------- | ---------------- |
| closeCase | Cancel All Tasks |

---

# 11. Testing

## Test Users

Create users for:

### Judicial

* Judge
* Fee Paid Judge
* Leadership Judge
* Task Supervisor
* Case Allocator

### Staff

* CTSC Administrator
* CTSC Team Leader
* Hearing Centre Administrator
* Hearing Centre Team Leader
* NBC Administrator
* NBC Team Leader

### Specific Access

* Judiciary Approver
* CTSC Approver
* Admin Approver
* Legal Ops Approver

Every role must have:

* Test user
* Functional test
* Role validation test

---

# 12. Release Activities

## Enable Flags

Use Flux Config to enable feature flags in:

* AAT
* PTL
* Demo
* Prod

## User Refresh

Execute:

* Refresh Users Process
* Refresh User Role Assignments

## Reconciliation

Complete:

* IDAM Role Mapping Upload
* User Role Verification
* Role Assignment Validation

---

# 13. Example Implementation References

## ORM

COT-1149

Add STAFF mappings in ORM.

Repository:

```text
am-org-role-mapping-service
```

## RAS

COT-1150

Add CASE role validation rules.

Repository:

```text
am-role-assignment-service
```

## Flux

Enable feature flags via:

```text
cnp-flux-config
```
