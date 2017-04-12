package conlife;

import java.text.ParseException;

class Rules {

    enum Rule {
        BIRTH, SURVIVE, DEATH;
    }

    static Rules parseRules(String rulesString) throws ParseException {
        String[] rules = rulesString.split("/");
        if (rules.length != 2) {
            throw new ParseException("Invalid rules", -1);
        } else if (rules[0].length() != 3) {
            throw new ParseException("Invalid rules", -1);
        } else if (rules[1].length() != 3) {
            throw new ParseException("Invalid rules", -1);
        }
        try {
            int birthMin = Integer.parseInt(rules[0].substring(1,1));
            int birthMax = Integer.parseInt(rules[0].substring(1,1));
            if (birthMin == 0) {
                birthMin = birthMax;
            }
            int surviveMin = Integer.parseInt(rules[0].substring(1,1));
            int surviveMax = Integer.parseInt(rules[0].substring(1,1));
            return new Rules(birthMin, birthMax, surviveMin, surviveMax);
        } catch (NumberFormatException | Rules.RulesException e) {
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

    Rule getRule(int numNeighbors) {
        if (isBirth(numNeighbors)) {
            return Rule.BIRTH;
        } else if (isSurvive(numNeighbors)) {
            return Rule.SURVIVE;
        }
        return Rule.DEATH;
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
