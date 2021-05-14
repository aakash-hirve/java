# Cowin Slot Notifier
This project is created with the intention of easing the process of getting email notifications for available COVID vaccination slots across India.

## Pre-requisites
1. JDK 8+
2. Redis

## Working Principle
The project is written in Java. The vert.x framework is used for polling protected APIs for Cowin by default at a frequency of 600ms.
All the relevant configurations are defined in the "cowin.config" file, you can tweak and modify the configurations as per requirement.
JWT token generation is handled automatically for a seamless access to protected APIs. Token is auto-generated after expiry.
Redis cache is temporarily maintaining -
1. JWT token (15 minutes) 
2. Session IDs (no expiry yet, so that one email is sent per available session)
3. txnId (60 seconds)
