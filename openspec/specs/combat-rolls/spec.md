# Combat Rolls Specification

## Purpose

Define the current roll presentation rules.

## Requirements

### Requirement: Group attack rolls

The system MUST roll attacks for every instance in the selected group and MUST render each creature separately.

#### Scenario: Multi-instance group

- GIVEN a group has several instances
- WHEN the user presses `Atacar`
- THEN each instance MUST receive its own attack result set

### Requirement: Multiple attacks

The system MUST roll repeated attacks separately when `quantity > 1`.

#### Scenario: Two claws

- GIVEN an attack has `quantity = 2`
- WHEN the attack is rolled
- THEN the UI MUST show `Claw 1` and `Claw 2`

### Requirement: Critical presentation

The system MUST show the attack roll, normal damage, confirmation roll, and critical damage when a threat occurs.

#### Scenario: Threat

- GIVEN the natural roll threatens a critical hit
- WHEN the result is displayed
- THEN the UI MUST show threat and confirmation

### Requirement: Typed damage

The system MUST keep damage components separated by type.

#### Scenario: Mixed damage

- GIVEN an attack deals piercing and fire damage
- WHEN the result is shown
- THEN both types MUST be visible separately

### Requirement: Saving throws

The system MUST roll Fortitude, Reflex, and Will for every instance in the selected group.

#### Scenario: Group saves

- GIVEN the user presses `Salvaciones`
- WHEN the roll resolves
- THEN each instance MUST show all three saving throws
