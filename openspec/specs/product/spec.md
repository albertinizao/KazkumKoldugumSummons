# Product Specification

## Purpose

Describe the current tabletop-focused application for managing Pathfinder 1e summons.

## Requirements

### Requirement: Local tabletop app

The system MUST remain a local web application focused on a single player at the table.

#### Scenario: Tablet use

- GIVEN the app is opened on a tablet
- WHEN the user navigates through the main screens
- THEN the UI MUST remain usable without hover-only interactions

### Requirement: Current screens

The system MUST expose the current screens: Combat, Summons, Catalog, and Settings.

#### Scenario: Navigation

- GIVEN the app is loaded
- WHEN the user uses the top bar
- THEN the screens MUST be reachable directly

### Requirement: Clear summons action

The top bar MUST expose a clear-summons button that asks for confirmation.

#### Scenario: Confirm clear

- GIVEN active summons exist
- WHEN the user presses `Limpiar`
- THEN the app MUST require confirmation before deleting them

### Requirement: Non-goals

The system MUST NOT automate context-dependent Pathfinder rules such as hit resolution, enemy damage application, initiative, or temporary modifiers.

#### Scenario: Attack shown

- GIVEN the app rolls an attack
- WHEN the result is displayed
- THEN the user MUST still decide the outcome in mesa
