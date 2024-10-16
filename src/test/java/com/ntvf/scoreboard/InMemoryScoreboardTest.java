package com.ntvf.scoreboard;

import com.ntvf.scoreboard.Scoreboard.Summary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryScoreboardTest {
    private Scoreboard scoreboard;

    @BeforeEach
    void setUp() {
        scoreboard = new InMemoryScoreboard();
    }

    @Test
    @DisplayName("Start a match and verify it appears on the scoreboard")
    void startMatch() {
        // GIVEN a scoreboard with no matches
        assertTrue(scoreboard.getSummary().isEmpty());

        // WHEN we start a match between Mexico and Canada
        scoreboard.startMatch("Mexico", "Canada");

        // THEN the scoreboard should contain that match with a score of 0 - 0
        assertState("""
                Mexico 0 - Canada 0
                """, scoreboard.getSummary());
    }

    @Test
    @DisplayName("Update the score of an ongoing match")
    void updateScore() {
        // GIVEN a match between Spain and Brazil
        scoreboard.startMatch("Spain", "Brazil");

        // WHEN we update the score to 2 - 1
        scoreboard.updateScore("Spain", 2, "Brazil", 1);

        // THEN the scoreboard should reflect the updated score
        assertState("""
                Spain 2 - Brazil 1
                """, scoreboard.getSummary());
    }


    @Test
    @DisplayName("Finish an ongoing match and remove it from the scoreboard")
    void testFinishMatch() {
        // GIVEN a match between Uruguay and Italy
        scoreboard.startMatch("Uruguay", "Italy");

        // WHEN we finish the match
        scoreboard.finishMatch("Uruguay", "Italy");

        // THEN the match should no longer appear on the scoreboard
        assertTrue(scoreboard.getSummary().isEmpty());
    }

    @Test
    @DisplayName("Get a summary of matches sorted by total score and most recent match")
    void testGetSummary() {
        // GIVEN multiple matches in progress with different scores
        scoreboard.startMatch("Mexico", "Canada");
        scoreboard.updateScore("Mexico", 0, "Canada", 5);

        scoreboard.startMatch("Spain", "Brazil");
        scoreboard.updateScore("Spain", 10, "Brazil", 2);

        scoreboard.startMatch("Germany", "France");
        scoreboard.updateScore("Germany", 2, "France", 2);

        scoreboard.startMatch("Uruguay", "Italy");
        scoreboard.updateScore("Uruguay", 6, "Italy", 6);

        scoreboard.startMatch("Argentina", "Australia");
        scoreboard.updateScore("Argentina", 3, "Australia", 1);

        // WHEN we request the summary
        var summary = scoreboard.getSummary();

        // THEN the matches should be ordered by total score and most recently started match
        assertState("""
                Uruguay 6 - Italy 6
                Spain 10 - Brazil 2
                Mexico 0 - Canada 5
                Argentina 3 - Australia 1
                Germany 2 - France 2
                """, summary);
    }

    @Test
    @DisplayName("Handle invalid score updates with negative values")
    void updateScoreWithNegativeValues() {
        // GIVEN a match between Mexico and Canada
        scoreboard.startMatch("Mexico", "Canada");

        // WHEN we attempt to update the score with negative values
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.updateScore("Mexico", -1, "Canada", 5);
        });

        // THEN an error should be thrown and no update should occur
        assertEquals("Scores cannot be negative", exception.getMessage());
    }

    @Test
    @DisplayName("Handle updating score of a non-existent match")
    void updateNonExistentMatch() {
        // GIVEN no matches are currently in progress
        assertTrue(scoreboard.getSummary().isEmpty());

        // WHEN we attempt to update the score of a non-existent match
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.updateScore("Mexico", 1, "Canada", 1);
        });

        // THEN an error should be thrown indicating the match does not exist
        assertEquals("Match not found", exception.getMessage());
    }

    @Test
    @DisplayName("Handle starting a match with invalid team names")
    void startMatchWithInvalidTeamNames() {
        // WHEN we try to start a match with null or empty team names
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.startMatch(null, "Canada");
        });

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.startMatch("Mexico", "");
        });

        // THEN appropriate errors should be thrown
        assertEquals("Team names cannot be null or empty", exception1.getMessage());
        assertEquals("Team names cannot be null or empty", exception2.getMessage());
    }

    @Test
    @DisplayName("Update the score of an ongoing match")
    void startMatchWithDuplicates() {
        // GIVEN a match between Spain and Brazil
        scoreboard.startMatch("Spain", "Brazil");

        // WHEN we want to start a match of duplicates
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.startMatch("Brazil", "Spain");
        });
        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.startMatch("Spain", "Brazil");
        });

        // THEN errors should be thrown
        assertEquals("Match already started", exception1.getMessage());
        assertEquals("Match already started", exception2.getMessage());
    }

    @Test
    @DisplayName("Handle finishing a match that does not exist")
    void finishNonExistentMatch() {
        // GIVEN no matches are currently in progress
        assertTrue(scoreboard.getSummary().isEmpty());

        // WHEN we attempt to finish a match that hasn't been started
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scoreboard.finishMatch("Mexico", "Canada");
        });

        // THEN an error should be thrown indicating the match does not exist
        assertEquals("Match not found", exception.getMessage());
    }


    private void assertState(String expectedState, Summary summary) {
        assertEquals(expectedState, renderSummaryState(summary));
    }

    private String renderSummaryState(Summary summary) {
        return summary.getMatches()
                .stream()
                .map(it -> "%s %s - %s %s".formatted(it.getHomeTeamName(), it.getHomeTeamScore(),
                        it.getAwayTeamName(), it.getAwayTeamScore()))
                .collect(Collectors.joining("\n")) + "\n";
    }
}