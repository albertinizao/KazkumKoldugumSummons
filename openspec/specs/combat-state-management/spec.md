# Combat State Management Specification

## Purpose

Define how active summon groups, individual instances, hit points, and deletion work.

## Requirements

### Requirement: Grouped active summons

The system MUST represent active summons as groups of instances that share one final creature definition.

#### Scenario: Shared final creature

- GIVEN multiple instances have the same resolved creature
- WHEN the combat screen renders
- THEN the app MUST show one group with multiple instances

### Requirement: Instance hit points

Each instance MUST track current hit points independently and MUST derive its visible state from current and maximum hit points.

#### Scenario: Damage and recovery

- GIVEN one instance is damaged
- WHEN the user heals it
- THEN the instance state MUST recalculate to HEALTHY or DAMAGED

### Requirement: Individual delete and clear all

The system MUST allow deleting one instance without affecting the others and MUST provide a clear-all action for the whole combat state.

#### Scenario: Remove one creature

- GIVEN a group has multiple instances
- WHEN the user deletes one instance
- THEN the other instances MUST remain visible

### Requirement: Derived states

The system MUST expose HEALTHY, DAMAGED, and DOWN as the only visible instance states.

#### Scenario: Zero hit points

- GIVEN an instance reaches zero or fewer hit points
- WHEN the screen updates
- THEN the app MUST mark the instance as DOWN and keep it visible
