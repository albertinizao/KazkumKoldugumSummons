# Persistence Specification

## Purpose

Define what state must survive refreshes and screen reloads.

## Requirements

### Requirement: Persist combat state

The system MUST persist active groups, instances, hit points, daily uses, configuration, and recent summons after every relevant change.

#### Scenario: State mutation

- GIVEN the user summons, damages, heals, or deletes a creature
- WHEN the operation finishes
- THEN the updated state MUST be stored immediately

### Requirement: Restore on reload

The system MUST restore the last valid combat state when the app reloads.

#### Scenario: Browser refresh

- GIVEN the user refreshes the page
- WHEN the app boots again
- THEN the active combat state MUST reappear

### Requirement: Recent and popular summons

The system MUST preserve recent and most-used summon shortcuts so the summon flow can reuse them.

#### Scenario: Shortcut reuse

- GIVEN the user summons the same creature repeatedly
- WHEN the app recomputes shortcut data
- THEN the shortcut lists MUST be updated
