package com.pathfinder.summons.infrastructure.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.pathfinder.summons.domain.model.HitDice;
import com.pathfinder.summons.domain.model.SpecialDefenseType;
import java.lang.reflect.Method;
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
}
