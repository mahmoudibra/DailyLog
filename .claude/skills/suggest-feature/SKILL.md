---
name: suggest-feature
description: Research and suggest a new feature for the DailyWorkTracker app by analyzing the current feature set, searching for trending productivity app patterns, and proposing a well-structured addition. Use this skill when the user asks to suggest, brainstorm, recommend, or come up with new feature ideas. Also trigger when the user mentions "feature suggestions", "what should I build next", "new feature ideas", or wants to update feature-suggestions.txt.
---

# Suggest Feature

Research and suggest a new feature for the DailyWorkTracker app by analyzing the current feature set, searching for trending productivity app patterns, and proposing a well-structured addition that complements existing functionality.

## When to Use

Use when the user asks to suggest, brainstorm, recommend, or come up with new feature ideas for the app. Also trigger when the user mentions "feature suggestions", "what should I build next", "new feature ideas", or wants to update feature-suggestions.txt with a new entry.

## Process

### 1. Read the Current Feature List

Read `agents/feature-suggestions.txt` to understand:
- What features are already listed (both implemented and suggested)
- The format used for suggestions (priority ranking, description bullets)
- What gaps exist in the current feature set

Also scan the `features/` directory to confirm which modules actually exist as code.

### 2. Research Trending Features

Search the web for current trends in productivity, work tracking, and personal effectiveness apps. Good sources of inspiration:

- **Productivity apps**: Todoist, Sunsama, Reclaim.ai, Motion, Centered, Akiflow
- **Journaling/reflection apps**: Day One, Reflect, Obsidian
- **Time/focus apps**: Forest, Session, Toggl, Clockify
- **Habit apps**: Streaks, Habitica, Fabulous
- **Work tracking tools**: Jira, Linear, Notion

Look for features that are:
1. Trending or gaining popularity recently
2. Not already covered by the existing feature set
3. Feasible for a desktop Compose app (no mobile-specific features like GPS)
4. Complementary to existing data (daily logs, time entries, objectives, reviews)

If web search is unavailable, draw on knowledge of the productivity app ecosystem and note that the suggestion is based on training knowledge rather than live research.

### 3. Evaluate and Select

Pick the single best feature suggestion based on these criteria:

| Criteria | Weight |
|----------|--------|
| User value (solves a real problem) | HIGH |
| Leverages existing data/infrastructure | HIGH |
| Differentiation (not already covered) | HIGH |
| Implementation feasibility | MEDIUM |
| Trend relevance (modern, in-demand) | MEDIUM |

### 4. Present the Suggestion

Use this output format:

```
## [Feature Name] (Impact/Effort rating)

**What it does:**
- Bullet points describing core functionality (3-5 bullets)

**Why it fits this app:**
- How it leverages existing features/data
- What gap it fills
- What apps inspired it

**Technical notes:**
- Suggested database tables and key fields
- Which existing repositories/data it cross-references
- Any new infrastructure needed (e.g., notifications, file I/O)
```

Rate impact and effort each as HIGH/MEDIUM/LOW, formatted as `(IMPACT/EFFORT)`.

### 5. Offer to Save

Ask the user if they want to append the suggestion to `agents/feature-suggestions.txt`. If yes, append using the existing format:

```
NEW SUGGESTION:
{N}. {Feature Name} ({IMPACT}/{EFFORT})
   - {bullet 1}
   - {bullet 2}
   - {bullet 3}
   - Leverages existing {data sources}
   - Inspiration: {app1}, {app2}, {app3}
```

Where `{N}` is the next number after the last suggestion in the file.

## Guidelines

- Suggest only ONE feature per invocation — focused is better than scattered
- Never suggest features already in the file, even if not yet implemented
- Prioritize features that create cross-feature synergies (e.g., a feature that makes daily logs + time tracking + objectives work together better)
- Keep technical notes grounded in the app's actual architecture: SQLite, raw JDBC, Compose Desktop, manual DI, kotlinx-datetime
