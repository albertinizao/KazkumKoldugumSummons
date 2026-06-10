# Summoner Configuration Specification

## Purpose

Describe the current singleton configuration for summons.

## Requirements

### Requirement: Maximum summon level

The system MUST persist `maxSummonMonsterLevel` and default it to 3.

#### Scenario: First start

- GIVEN the app starts for the first time
- WHEN the configuration is loaded
- THEN `maxSummonMonsterLevel` MUST be 3

### Requirement: Daily uses maximum

The system MUST persist the maximum daily uses alongside the summon-level configuration.

#### Scenario: Update config

- GIVEN the user updates configuration
- WHEN the save completes
- THEN both the summon level and the daily-use maximum MUST be stored

### Requirement: Alias endpoint

The system MUST expose the configuration through `/api/configuration` and `/api/summoner-configuration`.

#### Scenario: Read config

- GIVEN the frontend loads settings
- WHEN it requests the configuration
- THEN either path MUST return the same data
