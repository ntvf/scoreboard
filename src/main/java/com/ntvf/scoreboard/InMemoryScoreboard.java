package com.ntvf.scoreboard;

import java.util.List;

public class InMemoryScoreboard implements Scoreboard {

    @Override
    public void startMatch(String homeTeam, String awayTeam) {
        notImplemented();
    }

    @Override
    public void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore) {
        notImplemented();
    }

    @Override
    public void finishMatch(String homeTeam, String awayTeam) {
        notImplemented();
    }

    @Override
    public Summary getSummary() {
        return new Summary() {
            @Override
            public List<Match> getMatches() {
                return List.of();
            }
        };
    }

    private void notImplemented() {
        throw new RuntimeException("TODO");
    }
}
