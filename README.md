# Live Football World Cup Scoreboard Library

## Description
This is a simple in-memory, thread-safe library to manage live football scores for ongoing matches. It provides functionality to:
1. Start a new match.
2. Update the score of an ongoing match.
3. Finish a match, removing it from the scoreboard.
4. Retrieve a summary of matches in progress, sorted by their total score and the most recently started match.

## Features
- **Start Match:** Initialize a match with two teams and a starting score of 0-0.
- **Update Score:** Update the score for both teams in an ongoing match.
- **Finish Match:** Finish a match and remove it from the scoreboard.
- **Summary of Matches:** Get a summary of all ongoing matches, ordered by the total score and the most recent start time for ties in score.

## Assumptions and Edge Cases

1. **Unique Match Identification:**
    - Matches are uniquely identified by the combination of the two teams (Home team and Away team).
    - **Edge Case:** If a match between two teams is already in progress, attempting to start another match between the same two teams (in any order, e.g., Mexico vs. Canada or Canada vs. Mexico) will throw an error to prevent duplicate matches.

2. **Match Lifecycle:**
    - Once a match is finished, it is removed from the system and no further updates or operations can be performed on it.
    - **Edge Case:** If an attempt is made to update or finish a match that has already been finished, an error will be returned.

3. **Non-Existent Matches:**
    - Trying to update or finish a match that hasn’t been started will result in an error. This prevents operations on non-existent matches.

4. **Score Updates:**
    - Score updates must be valid absolute scores (non-negative integers for both home and away teams).
    - **Edge Case:** Negative scores (e.g., -1) will not be allowed, and the system will raise an error if invalid scores are provided.

5. **Match Summary Ordering:**
    - Matches in progress are listed in descending order of their total score (sum of home and away team scores). If two matches have the same total score, they will be ordered by the most recently started match.
    - **Edge Case:** In the case of matches with frequent score updates, the ordering will be stable and respect the order of match creation when scores are tied.

6. **Invalid or Empty Team Names:**
    - Team names must be valid strings. Empty or null team names are not allowed.
    - **Edge Case:** Attempting to start a match with invalid (empty or null) team names will result in an error.

7. **Empty Scoreboard:**
    - If no matches are currently in progress, the summary will return an empty list inside Summary object.

## Usage Example
```java
// Create the scoreboard instance
Scoreboard scoreboard = new InMemoryScoreboard();

// Start a few matches
scoreboard.startMatch("Mexico", "Canada");
scoreboard.startMatch("Spain", "Brazil");

// Update scores
scoreboard.updateScore("Mexico", 0, "Canada", 5);
scoreboard.updateScore("Spain", 10, "Brazil", 2);

// Get summary
Scoreboard.Summary summary = scoreboard.getSummary();

// Finish a match
scoreboard.finishMatch("Mexico", "Canada");