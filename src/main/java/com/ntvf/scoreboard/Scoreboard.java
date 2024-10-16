package com.ntvf.scoreboard;

import java.util.List;

public interface Scoreboard {

    void startMatch(String homeTeam, String awayTeam);

    void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore);

    void finishMatch(String homeTeam, String awayTeam);

    Summary getSummary();

    interface Summary {
        List<Match> getMatches();
    }

    interface Match {
        String getHomeTeamName();

        int getHomeTeamScore();

        String getAwayTeamName();

        int getAwayTeamScore();
    }

}
