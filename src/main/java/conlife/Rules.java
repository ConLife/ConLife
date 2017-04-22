package conlife;

import java.text.ParseException;

class Rules {

    enum Rule {
        BIRTH, SURVIVE, DEATH, DEAD_NO_BIRTH;
    }

    static Rules parseRules(String rulesString) throws ParseException, RulesException {
        String[] rules = rulesString.split("/");
        if (!rules[0].startsWith("B")) {
            throw new ParseException("Invalid rules", -1);
        } else if (!rules[1].startsWith("S")) {
            throw new ParseException("Invalid rules", -1);
        }
        rules[0] = rules[0].substring(1, rules[0].length());
        rules[1] = rules[1].substring(1, rules[1].length());

        int[] birth = new int[rules[0].length()];
        int[] survive = new int[rules[1].length()];
        for (int i = 0; i < rules[0].length(); i++) {
            try {
                birth[i] = Integer.parseInt(rules[0].substring(i, i + 1));
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid rules", -1);
            }
        }
        for (int i = 0; i < rules[1].length(); i++) {
            try {
                survive[i] = Integer.parseInt(rules[1].substring(i, i + 1));
            } catch (NumberFormatException e) {
                throw new ParseException("Invalid rules", -1);
            }
        }
        return new Rules(birth, survive);
    }

    int birth;
    int survive;

    private Rules(int[] birth, int[] survive) throws RulesException {
        this.birth = 0;
        this.survive = 0;

        for (int i = 0; i < birth.length; i++) {
            if (birth[i] < 0 && birth[i] > 8) {
                throw new RulesException();
            }
            int b = 1 << birth[i];
            this.birth = this.birth | b;
        }

        for (int i = 0; i < survive.length; i++) {
            if (survive[i] < 0 && survive[i] > 8) {
                throw new RulesException();
            }
            int s = 1 << survive[i];
            this.survive = this.survive | s;
        }
    }

    public Rule getRule(boolean alive, int numNeighbors) {
        if (!alive && isBirth(numNeighbors)) {
            return Rule.BIRTH;
        } else if (alive && isSurvive(numNeighbors)) {
            return Rule.SURVIVE;
        } else if (alive){
            return Rule.DEATH;
        } else {
            return Rule.DEAD_NO_BIRTH;
        }
    }

    boolean isBirth(int numNeighbors) {
        return isBitEnabled(birth, numNeighbors);
    }

    boolean isSurvive(int numNeighbors) {
        return isBitEnabled(survive, numNeighbors);
    }

    static class RulesException extends Exception {
        RulesException() {
            super();
        }
    }

    private static boolean isBitEnabled(int field, int position) {
        return ((field >> position) & 1) == 1;
    }
}
