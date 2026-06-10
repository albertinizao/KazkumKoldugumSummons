# Delta for Creature Resolution

## MODIFIED Requirements

### Requirement: Apply fixed rules

The system MUST resolve a final creature by applying Augment Summoning, the selected summon template, and Deep Guardian when eligible.

#### Scenario: Standard summon

- GIVEN a base creature is summoned
- WHEN resolution runs
- THEN the final creature MUST include the fixed rules that apply to it

#### Scenario: Template summon

- GIVEN a base creature is summoned with a template
- WHEN resolution runs
- THEN the final creature MUST include the template effects and derived bonuses

### Requirement: Deep Guardian trigger

The system MUST grant the Deep Guardian bonus when the resolved creature has burrow speed or the earth subtype.

#### Scenario: Earth creature

- GIVEN the resolved creature has burrow speed
- WHEN resolution completes
- THEN the final creature MUST receive the Deep Guardian attack and AC bonus

### Requirement: Template damage components

The system MUST add the template-specific damage components for Chthonic and Fiery to natural attacks and MUST preserve them as typed damage components.

#### Scenario: Critical damage

- GIVEN a Chthonic or Fiery attack threatens critical
- WHEN critical damage is shown
- THEN the template damage component MUST be visible in the critical damage output
