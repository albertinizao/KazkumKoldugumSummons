# Daily Uses Specification

## Purpose

Describe the current summon-use counter behavior.

## Requirements

### Requirement: Bounds

The system MUST keep daily uses within `0 <= remaining <= maximum` and `maximum >= 0`.

#### Scenario: Normalization

- GIVEN a stored value is outside the valid bounds
- WHEN the configuration or combat state loads
- THEN the values MUST be normalized

### Requirement: Increase, decrease, reset

The system MUST support increasing, decreasing, and resetting the counter.

#### Scenario: Reset

- GIVEN the user presses `Resetear`
- WHEN the action completes
- THEN `remaining` MUST equal `maximum`

### Requirement: Non-blocking summon at zero

The system MUST NOT hard-block summoning when `remaining` is zero.

#### Scenario: Zero remaining

- GIVEN remaining uses are zero
- WHEN a valid summon is executed
- THEN remaining MUST stay at zero
