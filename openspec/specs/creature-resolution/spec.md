# Creature Resolution Specification

## Purpose

Define how a base creature becomes the final creature used in combat.

## Requirements

### Requirement: Apply fixed rules

The system MUST resolve a final creature by applying Augment Summoning, Versatile Summon Monster when selected, and Deep Guardian when eligible.

#### Scenario: Standard summon

- GIVEN a base creature is summoned
- WHEN resolution runs
- THEN the final creature MUST include the fixed rules that apply to it

### Requirement: Template handling

The system MUST require a template choice when a manually selected creature allows multiple templates, and MUST reuse the stored template when a shortcut already encodes it.

#### Scenario: Manual vs shortcut

- GIVEN a creature supports several templates
- WHEN selected manually
- THEN the app MUST ask which template to apply

- GIVEN the same creature is selected from recent or popular summons
- WHEN the shortcut already contains a template
- THEN the app MUST summon it directly

### Requirement: Deep Guardian trigger

The system MUST grant the Deep Guardian bonus when the resolved creature has burrow speed or the earth subtype.

#### Scenario: Earth creature

- GIVEN the resolved creature has burrow speed
- WHEN resolution completes
- THEN the final creature MUST receive the Deep Guardian attack and AC bonus
