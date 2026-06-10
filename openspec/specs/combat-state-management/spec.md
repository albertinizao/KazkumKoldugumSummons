# Combat State Management Specification

## Purpose

Describe how the current combat state is stored and mutated.

## Requirements

### Requirement: Grouped summons

The system MUST represent active summons as groups of instances sharing one resolved creature.

#### Scenario: Same final creature

- GIVEN two summons resolve to the same creature
- WHEN they are stored
- THEN they MUST end up in the same group

### Requirement: Independent HP

Each instance MUST track its own hit points and status.

#### Scenario: One damaged instance

- GIVEN one instance is damaged
- WHEN another instance is inspected
- THEN its hit points MUST remain unchanged

### Requirement: Visible states

The system MUST expose `HEALTHY`, `DAMAGED`, and `DOWN` as the visible instance states.

#### Scenario: Zero HP

- GIVEN current hit points are 0 or less
- WHEN the instance is rendered
- THEN its status MUST be `DOWN`

### Requirement: Remove and clear

The system MUST support removing a single instance and clearing the whole summon list.

#### Scenario: Clear all

- GIVEN active summons exist
- WHEN the user clears summons
- THEN the active group list MUST become empty
