package com.ntvf.scoreboard;

import java.util.List;

import static java.util.Optional.ofNullable;

public interface Scoreboard {

    void startMatch(String homeTeam, String awayTeam);

    void updateScore(String homeTeam, int homeTeamScore, String awayTeam, int awayTeamScore);

    void finishMatch(String homeTeam, String awayTeam);

    Summary getSummary();

    interface Summary {
        List<Match> getMatches();

        default boolean isEmpty() {
            return ofNullable(getMatches())
                    .map(List::isEmpty)
                    .orElse(Boolean.TRUE);
        }
    }

    interface Match {
        String getHomeTeamName();

        int getHomeTeamScore();

        String getAwayTeamName();

        int getAwayTeamScore();
    }

}
