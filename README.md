# Project 1: Cowin
This project is created with the intention of easing the process of getting notifications for COVID vaccination slots across India.

## Pre-requisites
1. JDK 8+
2. Redis

## Working Principle
The project is written in Java. The vert.x framework is used for polling public APIs for Cowin by default at a frequency of 10 seconds.
All the relevant configurations are defined in the "cowin.config" file, you can tweak and modify the configurations as per requirement.
