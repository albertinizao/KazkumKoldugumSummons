# Creature Catalog Specification

## Purpose

Describe the current catalog of base creatures and preview behavior.

## Requirements

### Requirement: JSON catalog

The system MUST load creature templates from manually maintained JSON files.

#### Scenario: Catalog boot

- GIVEN the backend starts
- WHEN the catalog repository loads
- THEN the creature list MUST be available to the API

### Requirement: Search and filter

The catalog listing MUST support text search and summon-level filters.

#### Scenario: Query

- GIVEN the user searches the catalog
- WHEN the API is called
- THEN matching base creatures MUST be returned

### Requirement: Template parameter

The listing accepts a `template` parameter, but the current list implementation only excludes outsiders rather than applying a full template-specific filter.

#### Scenario: Template query

- GIVEN the UI sends a template filter
- WHEN the listing endpoint resolves the request
- THEN the list MUST still return the current code's partial filtering behavior

### Requirement: Preview resolved creature

The catalog MUST provide a resolved preview that applies the current fixed rules and optional template.

#### Scenario: Preview

- GIVEN a template is selected
- WHEN preview is requested
- THEN the API MUST return the resolved creature

### Requirement: Summary fields

The listing summary MUST expose AC, max HP, saving throws, speeds text, and attack text.

#### Scenario: Listing row

- GIVEN a creature is shown in the list
- WHEN the card renders
- THEN those summary fields MUST be available
