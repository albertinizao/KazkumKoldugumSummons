package com.pathfinder.summons.infrastructure.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.pathfinder.summons.domain.model.DamageAbility;
import com.pathfinder.summons.domain.model.HitDice;
import com.pathfinder.summons.domain.model.SpecialDefenseType;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JsonCreatureTemplateRepositoryTest {

    @Test
    void parsesSpellResistanceAliases() throws Exception {
        JsonCreatureTemplateRepository repository = new JsonCreatureTemplateRepository();
        Method method = JsonCreatureTemplateRepository.class.getDeclaredMethod("parseSpecialDefenseType", String.class);
        method.setAccessible(true);

        assertThat(method.invoke(repository, "sr")).isEqualTo(SpecialDefenseType.SPELL_RESISTANCE);
        assertThat(method.invoke(repository, "spell_resistance")).isEqualTo(SpecialDefenseType.SPELL_RESISTANCE);
    }

    @Test
    void normalizesSpellResistanceStoredAsOtherInCatalogJson() {
        JsonCreatureTemplateRepository repository = new JsonCreatureTemplateRepository();

        assertThat(repository.findById("hound-archon"))
                .isPresent()
                .get()
                .extracting(template -> template.getSpecialDefenses().stream()
                        .filter(defense -> defense.getType() == SpecialDefenseType.SPELL_RESISTANCE)
                        .findFirst()
                        .orElseThrow()
                        .getValue())
                .isEqualTo("15");
    }

    @Test
    void parsesParenthesizedHitDiceFormulaIntoStructuredMetadata() {
        JsonCreatureTemplateRepository repository = new JsonCreatureTemplateRepository();

        assertThat(repository.findById("ankylosaurus"))
                .isPresent()
                .get()
                .extracting(template -> template.getHitPoints().getHitDice())
                .isEqualTo(HitDice.builder().count(10).dieSize(8).build());
    }

    @Test
    void loadsExplicitChallengeRatingFromCatalogJson() {
        JsonCreatureTemplateRepository repository = new JsonCreatureTemplateRepository();

        assertThat(repository.findById("badger"))
                .isPresent()
                .get()
                .extracting(template -> template.getChallengeRating())
                .isEqualTo("1/2");
    }

    @Test
    void loadsMaximumPossibleHitPointsFromCatalogJson() {
        JsonCreatureTemplateRepository repository = new JsonCreatureTemplateRepository();

        assertThat(repository.findById("ankylosaurus"))
                .isPresent()
                .get()
                .extracting(template -> template.getHitPoints().getMaximum())
                .isEqualTo(110);

        assertThat(repository.findById("allosaurus"))
                .isPresent()
                .get()
                .extracting(template -> template.getHitPoints().getMaximum())
                .isEqualTo(132);
    }

    @Test
    void loadsExplicitDamageAbilityMultipliersPerAttackFromCatalogJson() {
        JsonCreatureTemplateRepository repository = new JsonCreatureTemplateRepository();

        var houndArchon = repository.findById("hound-archon").orElseThrow();
        Map<String, Double> multipliers = houndArchon.getAttacks().stream()
                .collect(java.util.stream.Collectors.toMap(
                        attack -> attack.getName().toLowerCase(),
                        attack -> attack.getDamageComponents().getFirst().getDamageAbilityMultiplier()));

        Map<String, DamageAbility> abilities = houndArchon.getAttacks().stream()
                .collect(java.util.stream.Collectors.toMap(
                        attack -> attack.getName().toLowerCase(),
                        attack -> attack.getDamageComponents().getFirst().getDamageAbility()));

        assertThat(multipliers).containsEntry("bite", 1.5d).containsEntry("slam", 0.5d);
        assertThat(abilities).containsEntry("bite", DamageAbility.STRENGTH).containsEntry("slam", DamageAbility.STRENGTH);
    }
}
