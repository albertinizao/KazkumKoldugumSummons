# Summoning Specification

## Purpose

Define the summon flow from the main screen to active combat state.

## Requirements

### Requirement: Open summon flow

The system MUST open a summon modal or panel from the main screen when the user chooses Invoke.

#### Scenario: Entry point

- GIVEN the user is on the combat screen
- WHEN they press Invoke
- THEN the app MUST show the summon flow

### Requirement: Quantity calculation

The system MUST calculate summon quantity from the current maximum summon level and the creature's summon level.

#### Scenario: Level difference

- GIVEN the creature is below the maximum summon level
- WHEN the user confirms the summon
- THEN the app MUST roll the configured quantity formula automatically

### Requirement: Daily use decrement

The system MUST decrement daily uses by one on summon and MUST NOT allow the remaining value to go below zero.

#### Scenario: No remaining uses

- GIVEN daily uses are already at zero
- WHEN the user summons a valid creature
- THEN the summon MAY proceed and remaining uses MUST stay at zero

### Requirement: Add to active groups

The system MUST add summoned instances to the matching active group when the final creature already exists, otherwise it MUST create a new group.

#### Scenario: Same final creature

- GIVEN a group already exists for the same final creature
- WHEN the user summons it again
- THEN the app MUST add new instances to that group
