# Daily Uses Specification

## Purpose

Define how summon uses are stored, corrected, and bounded.

## Requirements

### Requirement: Valid bounds

The system MUST keep daily uses within the range `0 <= remaining <= maximum` and MUST keep `maximum >= 0`.

#### Scenario: Invalid state

- GIVEN a persisted state would exceed the bounds
- WHEN the app loads it
- THEN the app MUST correct or reject the invalid values

### Requirement: Increment, decrement, reset

The system MUST support increasing, decreasing, and resetting daily uses while preserving bounds.

#### Scenario: Reset uses

- GIVEN the user resets daily uses
- WHEN the action completes
- THEN remaining MUST equal maximum

### Requirement: Non-blocking summon at zero

The system MUST NOT hard-block summoning at zero remaining uses in the MVP.

#### Scenario: Zero remaining

- GIVEN remaining uses are zero
- WHEN the user summons a valid creature
- THEN the remaining count MUST stay at zero and MUST NOT go negative
