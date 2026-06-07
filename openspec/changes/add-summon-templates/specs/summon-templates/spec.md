# Summon Templates Specification

## Purpose

Define how summon templates modify resolved creatures.

## Requirements

### Requirement: Template set

The system MUST support the summon templates Chthonic, Fiery, Celestial, Entropic, and Resolute.

#### Scenario: Template selection

- GIVEN a creature allows summon templates
- WHEN the user selects one of the supported templates
- THEN the system MUST apply the matching template behavior

### Requirement: Template identity

The system MUST treat template effects as part of the final creature identity.

#### Scenario: Different templates

- GIVEN the same base creature is summoned twice
- WHEN different templates are applied
- THEN the app MUST create distinct resolved creatures and groups

### Requirement: Chthonic template

Chthonic MUST change alignment to NG, add earth subtype, add darkvision 60 ft and tremorsense 60 ft, add burrow speed equal to half of the creature's highest existing speed, add 1 acid damage to all attacks, add acid resistance 10, and add the listed immunities.

#### Scenario: Chthonic creature

- GIVEN a base creature receives Chthonic
- WHEN resolution completes
- THEN the final creature MUST include all Chthonic effects

### Requirement: Fiery template

Fiery MUST change alignment to NG, add fire subtype, add darkvision 60 ft, add 1 fire damage to all attacks, and add fire resistance 10.

#### Scenario: Fiery creature

- GIVEN a base creature receives Fiery
- WHEN resolution completes
- THEN the final creature MUST include all Fiery effects

### Requirement: Celestial, Entropic, and Resolute templates

Celestial, Entropic, and Resolute MUST change alignment to NG, add darkvision 60 ft, add their listed resistances, and add a single-use swift-action smite special attack with the specified target alignment.

#### Scenario: Smite special attack

- GIVEN a creature receives one of these templates
- WHEN the resolved creature is shown
- THEN the special attack list MUST include the corresponding smite ability

### Requirement: Earth or burrow enhancement

Any resolved creature that has the earth subtype or a burrow speed MUST gain +1 attack and +1 AC normal and flat-footed as an enhancement bonus.

#### Scenario: Base earth creature

- GIVEN a creature already has earth subtype before template application
- WHEN the final creature is resolved
- THEN the enhancement bonus MUST still apply
