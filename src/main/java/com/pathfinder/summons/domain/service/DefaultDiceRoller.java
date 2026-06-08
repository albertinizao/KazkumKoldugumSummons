package com.pathfinder.summons.domain.service;

import com.pathfinder.summons.domain.model.DiceRoll;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class DefaultDiceRoller implements DiceRoller {

    private static final Pattern DICE_PATTERN = Pattern.compile("^(?:(\\d*)[dD](\\d+))(?:([+-]\\d+))?$");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[+-]?\\d+$");

    @Override
    public DiceRoll roll(String formula) {
        String normalized = normalizeFormula(formula);
        if (normalized.isBlank() || "—".equals(normalized) || "-".equals(normalized)) {
            return DiceRoll.builder()
                    .formula(normalized)
                    .naturalResults(List.of())
                    .modifier(0)
                    .total(0)
                    .build();
        }

        Matcher matcher = DICE_PATTERN.matcher(normalized);
        if (matcher.matches()) {
            int diceCount = parseDiceCount(matcher.group(1));
            int dieSize = Integer.parseInt(matcher.group(2));
            int modifier = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
            return roll(diceCount, dieSize, modifier, normalized);
        }

        if (NUMBER_PATTERN.matcher(normalized).matches()) {
            int modifier = Integer.parseInt(normalized);
            return DiceRoll.builder()
                    .formula(normalized)
                    .naturalResults(List.of())
                    .modifier(modifier)
                    .total(modifier)
                    .build();
        }

        throw new IllegalArgumentException("No se pudo interpretar la fórmula de dado: " + formula);
    }

    @Override
    public DiceRoll roll(int diceCount, int dieSize, int modifier) {
        return roll(diceCount, dieSize, modifier, buildFormula(diceCount, dieSize, modifier));
    }

    private DiceRoll roll(int diceCount, int dieSize, int modifier, String formula) {
        if (diceCount < 0) {
            throw new IllegalArgumentException("diceCount must be positive");
        }
        if (dieSize <= 0) {
            throw new IllegalArgumentException("dieSize must be positive");
        }

        List<Integer> results = new ArrayList<>(diceCount);
        int total = modifier;
        for (int index = 0; index < diceCount; index++) {
            int natural = ThreadLocalRandom.current().nextInt(1, dieSize + 1);
            results.add(natural);
            total += natural;
        }

        return DiceRoll.builder()
                .formula(formula)
                .naturalResults(List.copyOf(results))
                .modifier(modifier)
                .total(total)
                .build();
    }

    private int parseDiceCount(String rawDiceCount) {
        return rawDiceCount == null || rawDiceCount.isBlank() ? 1 : Integer.parseInt(rawDiceCount);
    }

    private String normalizeFormula(String formula) {
        return formula == null ? "" : formula.trim().replace(" ", "");
    }

    private String buildFormula(int diceCount, int dieSize, int modifier) {
        String prefix = diceCount + "d" + dieSize;
        if (modifier == 0) {
            return prefix;
        }
        return prefix + (modifier > 0 ? "+" + modifier : modifier);
    }
}
