# Persistence Specification

## Purpose

Describe the state that survives refreshes.

## Requirements

### Requirement: Combat state persistence

The system MUST persist groups, instances, hit points, daily uses, configuration, recent summons, and most-used summons after relevant changes.

#### Scenario: Mutation

- GIVEN the user summons, damages, heals, or deletes a creature
- WHEN the operation completes
- THEN the updated combat state MUST be stored

### Requirement: Restore on reload

The system MUST restore the last valid combat state when the app reloads.

#### Scenario: Browser refresh

- GIVEN the user refreshes the page
- WHEN the app boots again
- THEN the active combat state MUST reappear

### Requirement: Client snapshot

The frontend MAY keep a best-effort snapshot in `localStorage`, but the backend remains the source of truth.

#### Scenario: Cached boot

- GIVEN a snapshot exists
- WHEN the app loads
- THEN it MAY render the cached state before the API refresh
