package conlife;

import java.text.ParseException;

class Rules {

    enum Rule {
        BIRTH, SURVIVE, UNDER_POPULATION, OVER_POPULATION, DEAD_NO_BIRTH;
    }

    static Rules parseRules(String rulesString) throws ParseException, RulesException {
        String[] rules = rulesString.split("/");
        if (rules.length != 2) {
            throw new ParseException("Invalid rules", -1);
        } else if (rules[0].length() != 3) {
            throw new ParseException("Invalid rules", -1);
        } else if (rules[1].length() != 3) {
            throw new ParseException("Invalid rules", -1);
        }
        try {
            int birthMin = Integer.parseInt(rules[0].substring(1,2));
            int birthMax = Integer.parseInt(rules[0].substring(2,3));
            if (birthMin == 0) {
                birthMin = birthMax;
            }
            int surviveMin = Integer.parseInt(rules[1].substring(1,2));
            int surviveMax = Integer.parseInt(rules[1].substring(2,3));
            return new Rules(birthMin, birthMax, surviveMin, surviveMax);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid rules", -1);
        }
    }

    int birthMin, birthMax;
    int surviveMin, surviveMax;

    private Rules(int birthMin, int birthMax, int surviveMin, int surviveMax) throws RulesException {
        this.birthMin = birthMin;
        this.birthMax = birthMax;
        this.surviveMin = surviveMin;
        this.surviveMax = surviveMax;

        if (birthMax < birthMin) {
            throw new RulesException();
        } else if (surviveMax < surviveMin) {
            throw new RulesException();
        } else if (surviveMax > birthMin) {
            throw new RulesException();
        } else if (birthMax > 8) {
            throw new RulesException();
        }
    }

    public Rule getRule(boolean alive, int numNeighbors) {
        if (!alive && isBirth(numNeighbors)) {
            return Rule.BIRTH;
        } else if (alive && isSurvive(numNeighbors)) {
            return Rule.SURVIVE;
        } else if (alive && numNeighbors < surviveMin) {
            return Rule.UNDER_POPULATION;
        } else if (alive){
            return Rule.OVER_POPULATION;
        } else {
            return Rule.DEAD_NO_BIRTH;
        }
    }

    boolean isBirth(int numNeighbors) {
        return numNeighbors <= birthMax && numNeighbors >= birthMin;
    }

    boolean isSurvive(int numNeighbors) {
        return numNeighbors <= surviveMax && numNeighbors >= surviveMin;
    }

    static class RulesException extends Exception {
        public RulesException() {
            super();
        }
    }
}
