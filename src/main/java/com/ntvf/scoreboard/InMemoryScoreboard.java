package com.ntvf.scoreboard;

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
        notImplemented();
        return null;
    }

    private void notImplemented() {
        throw new RuntimeException("TODO");
    }
}
