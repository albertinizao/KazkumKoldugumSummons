# Combat Screen Specification

## Purpose

Define the main screen where the user manages combat in real time.

## Requirements

### Requirement: Main controls

The combat screen MUST always expose Invoke, Clear Summons, daily uses, and the latest roll result when available.

#### Scenario: Main entry

- GIVEN the user opens the app
- WHEN the combat screen renders
- THEN the global controls MUST be visible without extra navigation

### Requirement: Group visibility

The combat screen MUST show active summons grouped by final creature and MUST show each group's summary plus individual cards.

#### Scenario: Active summons

- GIVEN one or more groups exist
- WHEN the screen is displayed
- THEN the user MUST see each group and its instances

### Requirement: Tablet-friendly layout

The combat screen SHOULD remain readable and touch-friendly in portrait and landscape modes.

#### Scenario: Narrow viewport

- GIVEN the app runs on a tablet in portrait orientation
- WHEN the screen renders
- THEN controls and group cards MUST remain usable without hover
