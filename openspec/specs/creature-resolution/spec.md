# Creature Resolution Specification

## Purpose

Describe how a base creature becomes the resolved creature used in play.

## Requirements

### Requirement: Fixed rules

The resolver MUST apply Augment Summoning, optional template effects, and Deep Guardian when eligible.

#### Scenario: Standard summon

- GIVEN a creature is resolved
- WHEN resolution completes
- THEN the final creature MUST include the applicable fixed rules

### Requirement: Template legality

The resolver MUST reject templates for outsiders.

#### Scenario: Outsider creature

- GIVEN the creature type is outsider
- WHEN a template is requested
- THEN resolution MUST fail

### Requirement: Template effects

The resolver MUST add the current template-specific bonuses for Chthonic, Fiery, Celestial, Entropic, and Resolute.

#### Scenario: Fiery summon

- GIVEN the selected template is Fiery
- WHEN resolution completes
- THEN fire-related template defenses MUST be present

### Requirement: Deep Guardian

The resolver MUST apply Deep Guardian when the resolved creature has burrow speed or an earth subtype.

#### Scenario: Burrow speed

- GIVEN the creature gains burrow
- WHEN resolution completes
- THEN AC and attack bonuses MUST be applied
