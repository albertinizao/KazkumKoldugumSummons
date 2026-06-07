# Combat Rolls Specification

## Purpose

Define attack rolls, damage presentation, saving throws, and critical handling.

## Requirements

### Requirement: Group attack rolls

The system MUST roll all attacks for all instances in the selected group and MUST show each creature separately.

#### Scenario: Multiple attacks

- GIVEN a creature has more than one attack or repeated attack quantity
- WHEN the user chooses Attack All
- THEN the app MUST roll each attack separately

### Requirement: Damage by type

The system MUST display damage as separate typed components and MUST label it as damage if it hits.

#### Scenario: Mixed damage

- GIVEN an attack has piercing and fire damage
- WHEN the result is shown
- THEN the app MUST show both components separately

### Requirement: Critical flow

The system MUST show threat, confirmation, normal damage, and critical damage when a roll threatens a critical hit.

#### Scenario: Threat occurs

- GIVEN the natural die roll threatens a critical
- WHEN the attack result is produced
- THEN the app MUST show the confirmation roll and both damage versions

### Requirement: Saving throws

The system MUST roll Fortitude, Reflex, and Will for every instance in the selected group.

#### Scenario: Group saving throws

- GIVEN the user chooses Roll Saves
- WHEN the action completes
- THEN the app MUST show the three saving throws per instance
