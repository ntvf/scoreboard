package com.ntvf.scoreboard;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryScoreboard implements Scoreboard {
    private final ConcurrentHashMap<String, Match> map = new ConcurrentHashMap<>();

    @Override
    public void startMatch(String homeTeam, String awayTeam) {
        map.put(homeTeam + awayTeam, Match.builder()
                .homeTeamName(homeTeam)
                .awayTeamName(awayTeam)
                .createdAt(Instant.now())
                .build());
    }

    @Override
    public void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {
        map.put(homeTeam + awayTeam, Match.builder()
                .homeTeamName(homeTeam)
                .homeTeamScore(homeTeamScore)
                .awayTeamName(awayTeam)
                .awayTeamScore(awayTeamScore)
                .createdAt(map.get(homeTeam + awayTeam).getCreatedAt())
                .build());
    }

    @Override
    public void finishMatch(String homeTeam, String awayTeam) {
        map.remove(homeTeam + awayTeam);
    }

    @Override
    public Summary getSummary() {
        return () -> map.values().stream()
                .sorted(Comparator.<Match>comparingInt(it -> it.getHomeTeamScore() + it.getAwayTeamScore()).reversed()
                        .thenComparing(Comparator.comparing(Match::getCreatedAt).reversed()))
                .collect(Collectors.toList());
    }

    @Data
    @Builder
    private static class Match implements Scoreboard.Match {
        private final String homeTeamName;
        private final int homeTeamScore;
        private final String awayTeamName;
        private final int awayTeamScore;
        private final Instant createdAt;
    }
}

