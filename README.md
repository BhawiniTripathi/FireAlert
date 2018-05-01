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
![image](https://user-images.githubusercontent.com/33559403/39484069-c69f61b6-4d31-11e8-9ab5-1f5797505187.png)

## Component List
### Mobile Application
The application connects with MetaWear C using bluetooth. Once the temperature exceeds the specified threshold, an alert is sent to the user.

#### Temperature Display
Displays the recently tracked temperature.

#### Door Security Disabled Button
Shows when the door security is disabled in case of an emergency.

### MetaWear C API
This allows to connect and transfer data between client(Mobile device) and server(MetaWear). The interfaces in the API capture data from temperature sensor. The data is streamed using Bluetooth Low Energy.

#### NCP15XH103F03RC Temperature Thermistor
This sensor captures temperature.

## Security analysis
|                    |                             |                                                                                                                                   |                                                                         | 
|--------------------|-----------------------------|-----------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------| 
| Component Name     | Category of Vulnerability   | Issue Description                                                                                                                 | Mitigation                                                              | 
| MetaWear           | Data Sniffing               | As data is transmitted through BLE channel, information can be extracted the from the BLE interface between device and smartphone | Encrypting the BLE channel.                                             | 
|                    | Injecting malicious scripts | An attacker can inject malicious scripts using the BLE interface between device and smartphone                                    | Encrypting the BLE channel.                                             | 
|                    | Data tampering              | Physically manipulating sensor components.                                                                                        | Physically securing the MetaWear device so that it is tamper proof.     | 
| Mobile Application | Shoulder surfing            | Nosy-visitors might try to interfere to know information displayed on the application.                                            | User-awareness to ensure they keep their devices safely.                | 
|                    | Phishing                    | Attacks through malicious advertisements, popups or alert                                                                         | User training to ensure they click on popups from trusted sources only. | 
