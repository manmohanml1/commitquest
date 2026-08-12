# ADR 0001: Demo-first modular evolution

**Status:** Accepted

## Decision

Begin with a provider-independent web demonstration. Introduce the Spring Boot modular monolith when public repository analysis requires a server boundary, then add a separately scalable worker and durable event processing for connected campaigns.

## Why

The initial product risk is whether repository evidence creates a compelling and understandable campaign. Authentication, distributed infrastructure, and AI do not validate that risk.

## Consequences

- The first release is fast, inexpensive, and outage-resistant.
- Fixture and domain contracts must be versioned before live ingestion.
- The web application must consume a projection shaped like the future API rather than provider payloads.
- Infrastructure is added only when used by a shipped milestone.
