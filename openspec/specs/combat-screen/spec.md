# Combat Screen Specification

## Purpose

Define the current main combat view.

## Requirements

### Requirement: Global actions

The combat screen MUST expose global actions for rolling attacks and saving throws across all active groups.

#### Scenario: Global buttons

- GIVEN the combat view is open
- WHEN active groups exist
- THEN the user MUST see `Atacar con todas` and `Tirar TS con todas`

### Requirement: Clear summons

The top bar MUST expose a clear-summons action with confirmation.

#### Scenario: Clear all

- GIVEN active summons exist
- WHEN the user presses `Limpiar`
- THEN the app MUST ask for confirmation before deleting all summons

### Requirement: Group cards

The combat screen MUST show one card per active summon group with a summary and individual instance cards.

#### Scenario: Active groups

- GIVEN the user has summoned creatures
- WHEN the combat screen renders
- THEN each group MUST show its summary and instances

### Requirement: Instance actions

Each instance card MUST show hit points, status, quick damage/heal buttons, a custom amount modal, and delete.

#### Scenario: Instance control

- GIVEN an instance is visible
- WHEN the user interacts with the card
- THEN the app MUST allow damage, healing, and deletion

### Requirement: Expanded stat block

The combat screen MUST show the final creature's expanded stat block in the group modal.

#### Scenario: Expand creature

- GIVEN the user presses `Expandir ficha`
- WHEN the modal opens
- THEN the full resolved stat block MUST be visible
