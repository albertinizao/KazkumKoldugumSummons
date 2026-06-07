# Delta for Creature Resolution

## MODIFIED Requirements

### Requirement: Apply fixed rules

The system MUST resolve a final creature by applying Augment Summoning, summon templates when selected, Deep Guardian when eligible, and any additional template-derived effects that belong to the final creature.

#### Scenario: Standard summon

- GIVEN a base creature is summoned
- WHEN resolution runs
- THEN the final creature MUST include the fixed rules that apply to it

#### Scenario: Template summon

- GIVEN a base creature is summoned with a template
- WHEN resolution runs
- THEN the final creature MUST include the template effects and derived bonuses

(Previously: resolution only referenced Augment Summoning, Versatile Summon Monster, and Deep Guardian.)

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

### Requirement: Earth or burrow enhancement

The system MUST grant +1 attack and +1 AC normal and flat-footed to any resolved creature that has the earth subtype or a burrow speed, including creatures that had those traits before template application.

#### Scenario: Native earth creature

- GIVEN a creature already has the earth subtype before summoning
- WHEN resolution completes
- THEN the enhancement bonus MUST still apply

### Requirement: Template damage components

The system MUST add acid or fire damage components from Chthonic and Fiery to every attack and MUST keep those components available for critical multiplication when the component is marked as multiplying.

#### Scenario: Critical damage

- GIVEN a Chthonic or Fiery attack threatens critical
- WHEN critical damage is shown
- THEN the template damage component MUST follow the configured critical rule
