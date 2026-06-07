# Product Specification

## Purpose

Define the product boundaries and the overall user experience for the summons management app.

## Requirements

### Requirement: Tabletop support

The system MUST provide a local web application for managing active summons during Pathfinder 1e combat.

#### Scenario: Tablet combat use

- GIVEN the user opens the app on an Android tablet
- WHEN they enter combat mode
- THEN the app MUST be usable without desktop-only interactions

### Requirement: Dark, responsive shell

The system MUST use a dark visual theme and SHALL remain usable in portrait and landscape layouts.

#### Scenario: Default display

- GIVEN a fresh app load
- WHEN the main screen renders
- THEN dark mode MUST be active and primary controls MUST be readable

### Requirement: Non-goals

The system MUST NOT automate contextual Pathfinder rules such as hit resolution, enemy damage application, initiative, flanking, or temporary modifiers.

#### Scenario: Contextual combat

- GIVEN an attack roll is shown
- WHEN the user needs to determine if it hits
- THEN the system MUST leave that decision to the table
