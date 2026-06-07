# Summoner Configuration Specification

## Purpose

Define the configurable values that drive summon availability and summon quantity.

## Requirements

### Requirement: Maximum summon level

The system MUST persist a configurable `maxSummonMonsterLevel` value and MUST default it to 3.

#### Scenario: Default configuration

- GIVEN the app is initialized for the first time
- WHEN the configuration is loaded
- THEN `maxSummonMonsterLevel` MUST equal 3

### Requirement: Summon rules use configuration

The system MUST use `maxSummonMonsterLevel` to determine available creatures and the quantity formula for summons.

#### Scenario: Lower-level summon

- GIVEN `maxSummonMonsterLevel` is configured
- WHEN the user summons a creature below that level
- THEN the summon quantity MUST follow the configured level difference rules

### Requirement: Configurable persistence

The system MUST persist configuration changes and restore them on reload.

#### Scenario: User update

- GIVEN the user changes the maximum summon level
- WHEN the change is saved
- THEN the new value MUST survive refreshes
