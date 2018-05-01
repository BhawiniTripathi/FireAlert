# FireAlert

## Executive Summary
Many organizations and corporations over the world have a security feature that makes their employees swipe identity cards as they enter or leave the organization. However, in any emergency, such as in case of fire inside the building, the security feature at the door should be temporarily disabled such that the employees can evacuate the building in a hassle-free manner. This application aims to alert the employees in the building in case of any fire emergency and disable the security at the doors so that employees can escape without having to swipe their identity cards.

## Project Goals
* Sense temperature inside a building.
*	Display temperature.
*	Send alerts when the temperature is beyond a threshold.
*	Disable door security features.

## User stories
As an **employee**, I want to **receive fire alerts** so I can **escape to safety**.
**Acceptance Criteria:**
* Sense temperature.
* Send alert if the temperature exceeds a threshold.

As an **employee**, I want to **know if the door security feature has been disabled** so I can **understand that I no longer need to swipe my identity card to escape to safety**.
**Acceptance Criteria:**
* Display temperature.
* Show door security disabled.

As an **administrator**, I want to **keep vigil over normal temperature**, so I can **monitor if the organization is maintaining prescribed environment for its employees**.
**Acceptance Criteria:**
* Sense temperature.
* Display temperature.

## Misuser stories
As a **malicious attacker**, I want to **inject malicious script into the application**, so I can **see if my attack worked**.
**Mitigations:**
* Reminding users in the instruction sheet to encrypt bluetooth channel.

As a **nosy visitor**, I want to **know the normal temperature inside the organization** so I can **know the comfortable environment for the employees**.
**Mitigations:**
* Reminding users in the instruction sheet to keep their devices safely.

## High Level Design
