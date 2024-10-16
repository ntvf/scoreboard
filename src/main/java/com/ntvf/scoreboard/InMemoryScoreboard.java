package com.ntvf.scoreboard;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class InMemoryScoreboard implements Scoreboard {
    private final Map<String, Match> map = new ConcurrentHashMap<>();

    @Override
    public void startMatch(String homeTeam, String awayTeam) {
        validateMatchStart(homeTeam, awayTeam);

        map.put(constructKey(homeTeam, awayTeam), Match.builder()
                .homeTeamName(homeTeam)
                .awayTeamName(awayTeam)
                .createdAt(Instant.now())
                .build());

        log.debug("Started match {} - {}", homeTeam, awayTeam);
    }

    @Override
    public void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {
        validate("Scores cannot be negative", () -> homeTeamScore < 0 || awayTeamScore < 0);

        String key = constructKey(homeTeam, awayTeam);

        validateForUpdate(homeTeam, awayTeam, key);

        map.put(key, map.get(key).toBuilder()
                .homeTeamScore(homeTeamScore)
                .awayTeamScore(awayTeamScore)
                .build());

        log.debug("Updated match {}:{} - {}:{}", homeTeam, homeTeamScore, awayTeam, awayTeamScore);
    }

    @Override
    public void finishMatch(String homeTeam, String awayTeam) {
        String key = constructKey(homeTeam, awayTeam);

        validateForUpdate(homeTeam, awayTeam, key);

        map.remove(key);

        log.debug("Finished match {} - {}", homeTeam, awayTeam);
    }


    @Override
    public Summary getSummary() {
        return () -> map.values().stream()
                .sorted(Comparator.<Match>comparingInt(it -> it.getHomeTeamScore() + it.getAwayTeamScore()).reversed()
                        .thenComparing(Comparator.comparing(Match::getCreatedAt).reversed()))
                .collect(Collectors.toList());
    }

    private String constructKey(String homeTeam, String awayTeam) {
        return (homeTeam + awayTeam).toLowerCase();
    }

    private void validateMatchStart(String homeTeam, String awayTeam) {
        validateTeamNames(homeTeam, awayTeam);

        validate("Match already started", () ->
                map.containsKey(constructKey(homeTeam, awayTeam)) || map.containsKey(constructKey(awayTeam, homeTeam))
        );
    }

    private void validateForUpdate(String homeTeam, String awayTeam, String key) {
        validateTeamNames(homeTeam, awayTeam);
        validate("Match not found", () -> !map.containsKey(key));
    }

    private void validateTeamNames(String homeTeam, String awayTeam) {
        validate("Team names cannot be null or empty", () ->
                homeTeam == null || homeTeam.isBlank() || awayTeam == null || awayTeam.isBlank()
        );
    }

    private void validate(String message, Supplier<Boolean> condition) {
        if (condition.get()) {
            log.warn("Validation error: {}", message);
            throw new IllegalArgumentException(message);
        }
    }

    @Data
    @Builder(toBuilder = true)
    private static class Match implements Scoreboard.Match {

        private final String homeTeamName;

        private final int homeTeamScore;

        private final String awayTeamName;

        private final int awayTeamScore;

        private final Instant createdAt;
    }
}

