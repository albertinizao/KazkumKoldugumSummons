# Creature Catalog Specification

## Purpose

Define how base creatures are stored, queried, and previewed before invocation.

## Requirements

### Requirement: Structured base creatures

The catalog MUST store base creatures as structured JSON data with level, alignment, size, type, attacks, defenses, speeds, and full stat block fields.

#### Scenario: Catalog item

- GIVEN a creature exists in the catalog
- WHEN the app loads its entry
- THEN the app MUST expose the structured fields needed to resolve the final creature

### Requirement: Search and filter

The system SHOULD allow the user to search catalog creatures by name and filter by summon level and allowed template.

#### Scenario: Manual selection

- GIVEN the user opens the summon modal
- WHEN they search for a creature
- THEN the catalog MUST return matching base creatures

### Requirement: Final preview

The catalog MUST provide a resolved preview of the final creature after fixed rules and optional template application.

#### Scenario: Preview before summon

- GIVEN a base creature and an allowed template
- WHEN the user requests preview
- THEN the app MUST show the resolved creature instead of the raw base creature
