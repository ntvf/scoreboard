package com.ntvf.scoreboard;

import com.ntvf.scoreboard.Scoreboard.Summary;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryScoreboardTest {
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            5
    );

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

    @Test
    @DisplayName("Test concurrent start, update, and finish of matches")
    void testConcurrentOperations() throws InterruptedException {
        // GIVEN an ExecutorService to run concurrent operations
        ExecutorService executorService = Executors.newFixedThreadPool(5);

        // WHEN starting, updating, and finishing multiple matches concurrently
        val tasks = List.of(
                (Runnable) () -> scoreboard.startMatch("Mexico", "Canada"),
                () -> scoreboard.startMatch("Spain", "Brazil"),
                () -> scoreboard.startMatch("Germany", "France"),
                () -> scoreboard.startMatch("Argentina", "Australia"),
                () -> scoreboard.updateScore("Mexico", 1, "Canada", 0),
                () -> scoreboard.updateScore("Spain", 2, "Brazil", 1),
                () -> scoreboard.updateScore("Germany", 1, "France", 1),
                () -> scoreboard.updateScore("Argentina", 3, "Australia", 0),
                () -> scoreboard.finishMatch("Mexico", "Canada"),
                () -> scoreboard.finishMatch("Spain", "Brazil")
        );
        executeConcurrently(tasks);

        // Wait for all tasks to finish
        executorService.shutdown();
        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

        // THEN the scoreboard should reflect correct and consistent data after concurrent operations
        assertState("""
                Argentina 3 - Australia 0
                Germany 1 - France 1
                """, scoreboard.getSummary());
    }

    @Test
    @DisplayName("Test concurrent match start with the same teams")
    void testConcurrentDuplicateStartMatch() {
        // GIVEN a match between Spain and Brazil
        scoreboard.startMatch("Spain", "Brazil");

        // WHEN attempting to start the same match concurrently
        executeConcurrently(IntStream.range(1, 100)
                .boxed()
                .map(it -> (Runnable) () -> scoreboard.startMatch("Spain", "Brazil")
                ).toList()
        );
        executeConcurrently(IntStream.range(1, 100)
                .boxed()
                .map(it -> (Runnable) () -> scoreboard.startMatch("Brazil", "Spain")
                ).toList()
        );

        // THEN only one match between Spain and Brazil should exist
        assertState("""
                Spain 0 - Brazil 0
                """, scoreboard.getSummary());
    }

    @Test
    @DisplayName("Test concurrent score updates")
    void testConcurrentScoreUpdates() {
        // GIVEN a match started between Argentina and Australia, Spain and Brazil
        scoreboard.startMatch("Argentina", "Australia");
        scoreboard.startMatch("Spain", "Brazil");
        scoreboard.updateScore("Spain", 4, "Brazil", 2);

        // WHEN updating the score of Argentina and Australia concurrently

        executeConcurrently(IntStream.range(1, 100)
                .boxed()
                .map(it -> (Runnable) () -> scoreboard.updateScore("Argentina", it, "Australia", it + 1))
                .toList());

        scoreboard.updateScore("Argentina", 5, "Australia", 0);

        // THEN the final score should be the one from the last update, Spain and Brazil not changed
        assertState("""
                Spain 4 - Brazil 2
                Argentina 5 - Australia 0
                """, scoreboard.getSummary());
    }

    @SneakyThrows
    private void executeConcurrently(List<Runnable> tasks) {
        val futures = tasks.stream()
                .map(executorService::submit)
                .toList();
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception ignored) {
                // Ignoring exceptions
            }
        }
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