# Summoning Specification

## Purpose

Describe the current summon flow.

## Requirements

### Requirement: Invoke from UI

The system MUST allow the user to start summoning from the Invocations screen.

#### Scenario: Entry point

- GIVEN the user is on the summon screen
- WHEN the user chooses `Invocar`
- THEN the app MUST send a summon request

### Requirement: Invocation assistant

The system MUST provide a guided summon assistant on the Invocations screen.

#### Scenario: Guided choice

- GIVEN the user opens the assistant
- WHEN they answer the questions
- THEN the assistant MUST produce a suggested creature/template combination

### Requirement: Quantity calculation

The system MUST calculate the summon quantity from the configured max summon level and the creature summon level.

#### Scenario: Lower-level summon

- GIVEN the creature is below the maximum level
- WHEN the summon resolves
- THEN the amount MUST come from the configured formula

### Requirement: Shortcut reuse

The system MUST reuse recent/popular summon shortcuts when available.

#### Scenario: Shortcut summon

- GIVEN a shortcut exists
- WHEN the user taps it
- THEN the stored creature-template combination MUST be used

### Requirement: Group merge

The system MUST append new instances to the existing group when the resolved creature already exists.

#### Scenario: Same resolved creature

- GIVEN the group already exists
- WHEN the same resolved creature is summoned again
- THEN the new instances MUST join the group
