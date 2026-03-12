---
name: feature-implementation-orchestrator
description: Use this skill when the user wants to implement a new feature using multiple coordinated agents responsible for UI, business logic, database work, and build/review validation. This skill is appropriate for any general product or application feature request.
---

# Feature Implementation Orchestrator

## Purpose
This skill coordinates multiple specialized agents to implement a feature end-to-end in a structured, repeatable way. It is designed to work for **any general feature request**, regardless of the specific domain, as long as the work may involve frontend, backend/business logic, persistence, and integration/build validation.

Use this skill when the user wants a feature to be planned, implemented, reviewed, and verified through a multi-agent workflow.

---

## When to Use
Use this skill when the user asks to:

- add a new feature
- implement a product enhancement
- build a new flow or capability
- extend an existing app or system
- coordinate multiple engineering responsibilities
- ensure the app builds successfully after implementation
- iteratively fix issues until the feature is integrated correctly

Do not use this skill when the user only wants:

- a small isolated code snippet
- a simple explanation
- a purely design-only task with no implementation
- a one-line bug fix with no coordination required

---

## Core Agents

### 1. UI Agent
**Responsibility:**  
Owns all user interface work related to the feature.

**Tasks may include:**
- creating or updating screens, pages, components, layouts, forms, modals, and interactions
- ensuring consistent styling and UX patterns
- wiring UI elements to exposed actions and state
- handling user-facing validation messages and loading/error states

**Output:**
- frontend components
- UI state handling
- interaction wiring
- accessibility and usability improvements where needed

---

### 2. Business Logic Agent
**Responsibility:**  
Owns the feature behavior and application rules.

**Tasks may include:**
- implementing feature logic
- adding validations, workflows, services, controllers, handlers, or use cases
- coordinating between UI and persistence layers
- enforcing domain rules and feature-specific constraints

**Output:**
- services and business rules
- controllers/actions/handlers
- validation logic
- orchestration between systems

---

### 3. Database Agent
**Responsibility:**  
Owns all persistence and data model changes.

**Tasks may include:**
- updating schemas, models, migrations, queries, repositories, or data access layers
- ensuring storage supports the new feature
- handling indexes, constraints, defaults, and relationships where needed
- preserving backward compatibility when possible

**Output:**
- schema/model updates
- migrations
- repository/query changes
- persistence logic

---

### 4. Review and Build Agent
**Responsibility:**  
Owns final integration review and build verification.

**Tasks may include:**
- reviewing changes across all agents
- validating that all parts of the feature are connected correctly
- checking for missing imports, broken references, type mismatches, integration gaps, and build issues
- running build or compile validation
- identifying errors and assigning fixes to the responsible agent
- repeating review/fix/build cycles until successful

**Output:**
- integration review
- issue list
- build validation results
- final readiness confirmation

---

## General Workflow

### Step 1: Understand the Feature
- Read the feature request or specification.
- Summarize the feature in clear implementation terms.
- Identify expected user behavior, data requirements, and integration points.

### Step 2: Break Work by Agent
- Divide implementation into UI, business logic, database, and review/build tasks.
- Identify dependencies between agents.
- Determine implementation order.

### Step 3: Implement the Feature
- UI Agent builds the user-facing parts.
- Business Logic Agent implements behavior and rules.
- Database Agent adds or updates persistence support.

### Step 4: Integrate
- Connect all layers together.
- Ensure data flows correctly from UI to logic to storage and back.
- Verify feature completeness.

### Step 5: Review and Build
- Review and Build Agent checks the implementation holistically.
- Attempt a full build, compile, or validation run.

### Step 6: Fix and Repeat
If the app does not build successfully:
- identify the error
- determine which agent is responsible
- fix the issue
- retry the build

Repeat this cycle until:
- the application builds successfully
- the feature is fully integrated
- there are no blocking errors

### Step 7: Final Report
Provide:
- feature summary
- work completed by each agent
- files changed
- database/schema changes
- issues found and fixed
- final build/integration status

---

## Execution Rules

1. Every feature must be treated as an end-to-end implementation task unless the user explicitly limits scope.
2. Agents should stay within their area of responsibility, but collaborate when dependencies exist.
3. Do not stop at partial wiring if the requested feature requires full integration.
4. The Review and Build Agent must always validate final integration.
5. If build or compile errors occur, fix them and retry until the project builds successfully.
6. Avoid leaving placeholders, TODOs, or incomplete connections unless absolutely necessary.
7. Keep implementation aligned with the project’s existing architecture, conventions, and style.
8. Prefer minimal, maintainable changes over unnecessary rewrites.
9. Call out assumptions clearly when requirements are incomplete.
10. If a requested layer is not relevant for a feature, explicitly note that and continue with the applicable agents.

---

## Generic Output Format

Use this structure when executing the skill:

### Feature Summary
Brief summary of the requested feature and intended behavior.

### Agent Plan
- UI Agent:
- Business Logic Agent:
- Database Agent:
- Review and Build Agent:

### Implementation
Describe what each agent implemented.

### Integration Review
Summarize how the parts were connected and what was verified.

### Build and Fix Iterations
List any errors encountered, what caused them, and how they were fixed.

### Final Result
- Files changed
- Database changes
- Remaining limitations or assumptions
- Final build status

---

## Example Invocation
Use this skill for requests like:

- "Implement a new notification settings feature"
- "Add a wishlist feature to the app"
- "Build a new onboarding flow"
- "Create a feature using separate agents for frontend, backend, database, and validation"
- "Implement this feature and keep fixing issues until the app builds"

---

## Notes
- This skill is intentionally generic and can be reused for almost any feature.
- Not every feature will require database changes; in those cases, the Database Agent should explicitly report that no persistence updates were needed.
- Not every feature will require UI work; in API-only features, the UI Agent may report no action required.
- The workflow remains the same: understand, split responsibility, implement, review, build, fix, repeat.