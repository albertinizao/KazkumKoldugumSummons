package com.pathfinder.summons.infrastructure.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pathfinder.summons.domain.model.AbilityDetail;
import com.pathfinder.summons.domain.model.AbilityScores;
import com.pathfinder.summons.domain.model.AbilitySummary;
import com.pathfinder.summons.domain.model.Alignment;
import com.pathfinder.summons.domain.model.ArmorClass;
import com.pathfinder.summons.domain.model.Attack;
import com.pathfinder.summons.domain.model.AttackAbility;
import com.pathfinder.summons.domain.model.AttackType;
import com.pathfinder.summons.domain.model.CriticalProfile;
import com.pathfinder.summons.domain.model.CreatureSize;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.DamageAbility;
import com.pathfinder.summons.domain.model.DamageComponent;
import com.pathfinder.summons.domain.model.DamageType;
import com.pathfinder.summons.domain.model.HitDice;
import com.pathfinder.summons.domain.model.HitPointsDefinition;
import com.pathfinder.summons.domain.model.SavingThrowAbility;
import com.pathfinder.summons.domain.model.SavingThrows;
import com.pathfinder.summons.domain.model.SpecialDefense;
import com.pathfinder.summons.domain.model.SpecialDefenseType;
import com.pathfinder.summons.domain.model.Speed;
import com.pathfinder.summons.domain.model.SpeedType;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import com.pathfinder.summons.domain.repository.CreatureTemplateRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
public class JsonCreatureTemplateRepository implements CreatureTemplateRepository {

    private static final Set<DamageType> PHYSICAL_DAMAGE_TYPES = EnumSet.of(
            DamageType.PIERCING,
            DamageType.SLASHING,
            DamageType.BLUDGEONING
    );

    private final Map<String, CreatureTemplate> templatesById;
    private final List<CreatureTemplate> templatesInOrder;

    public JsonCreatureTemplateRepository() {
        this.templatesById = loadTemplates();
        this.templatesInOrder = templatesById.values().stream()
                .sorted(Comparator.comparingInt(CreatureTemplate::getSummonLevel)
                        .thenComparing(CreatureTemplate::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public List<CreatureTemplate> findAll() {
        return templatesInOrder;
    }

    @Override
    public Optional<CreatureTemplate> findById(String id) {
        return Optional.ofNullable(templatesById.get(id));
    }

    @Override
    public List<CreatureTemplate> findBySummonLevelLessThanEqual(int level) {
        return templatesInOrder.stream()
                .filter(template -> template.getSummonLevel() <= level)
                .toList();
    }

    private Map<String, CreatureTemplate> loadTemplates() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath*:catalog/*.json");
            return java.util.Arrays.stream(resources)
                    .map(this::readTemplate)
                    .collect(Collectors.toUnmodifiableMap(CreatureTemplate::getId, Function.identity(), (left, right) -> right));
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar el catálogo de criaturas", ex);
        }
    }

    private CreatureTemplate readTemplate(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            RawCreatureTemplate raw = new com.fasterxml.jackson.databind.ObjectMapper().readValue(inputStream, RawCreatureTemplate.class);
            return map(raw);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer la criatura desde " + resource.getFilename(), ex);
        }
    }

    private CreatureTemplate map(RawCreatureTemplate raw) {
        return CreatureTemplate.builder()
                .id(raw.id())
                .name(raw.nombre())
                .summonLevel(raw.nivelSummon())
                .alignment(parseAlignment(raw.alineamiento()))
                .size(parseCreatureSize(raw.tamano()))
                .creatureType(raw.tipo())
                .subtypes(safeList(raw.subtipos()))
                .allowedTemplates(safeList(raw.plantillasPermitidas()).stream()
                        .map(this::parseTemplateType)
                        .toList())
                .initiative(raw.iniciativa())
                .senses(safeList(raw.sentidos()))
                .perception(raw.percepcion())
                .abilities(AbilityScores.builder()
                        .strength(raw.atributos().fuerza())
                        .dexterity(raw.atributos().destreza())
                        .constitution(raw.atributos().constitucion())
                        .intelligence(raw.atributos().inteligencia())
                        .wisdom(raw.atributos().sabiduria())
                        .charisma(raw.atributos().carisma())
                        .build())
                .armorClass(ArmorClass.builder()
                        .normal(raw.ca().normal())
                        .touch(raw.ca().toque())
                        .flatFooted(raw.ca().desprevenido())
                        .detail(raw.ca().detalle())
                        .build())
                .hitPoints(HitPointsDefinition.builder()
                        .maximum(raw.pg().maximos())
                        .formula(raw.pg().formula())
                        .hitDice(parseHitDice(raw.pg().formula()))
                        .build())
                .savingThrows(SavingThrows.builder()
                        .fortitude(raw.salvaciones().fortaleza())
                        .reflex(raw.salvaciones().reflejos())
                        .will(raw.salvaciones().voluntad())
                        .fortitudeAbility(SavingThrowAbility.CONSTITUTION)
                        .build())
                .speeds(safeList(raw.velocidades()).stream()
                        .map(speed -> Speed.builder()
                                .type(parseSpeedType(speed.tipo()))
                                .valueFeet(speed.valor())
                                .build())
                        .toList())
                .attacks(safeList(raw.ataques()).stream()
                        .map(this::mapAttack)
                        .toList())
                .space(raw.espacio())
                .reach(raw.alcance())
                .specialAttacks(safeList(raw.ataquesEspeciales()))
                .specialDefenses(safeList(raw.defensasEspeciales()).stream()
                        .map(defense -> SpecialDefense.builder()
                                .type(parseSpecialDefenseType(defense.tipo()))
                                .value(defense.valor())
                                .notes(defense.notas())
                                .build())
                        .toList())
                .tacticalNotes(safeList(raw.notasTacticas()))
                .shortAbilities(safeList(raw.habilidadesResumidas()).stream()
                        .map(item -> AbilitySummary.builder()
                                .name(item.nombre())
                                .summary(item.resumen())
                                .build())
                        .toList())
                .expandedAbilities(safeList(raw.habilidadesCompletas()).stream()
                        .map(item -> AbilityDetail.builder()
                                .name(item.nombre())
                                .text(item.texto())
                                .build())
                        .toList())
                .fullStatBlock(raw.fullStatBlock())
                .build();
    }

    private Attack mapAttack(RawAttack rawAttack) {
        AttackType attackType = parseAttackType(rawAttack.attackType());
        AttackAbility attackAbility = attackType == AttackType.RANGED ? AttackAbility.DEXTERITY : AttackAbility.STRENGTH;
        DamageAbility damageAbility = attackType == AttackType.RANGED ? DamageAbility.DEXTERITY : DamageAbility.STRENGTH;

        return Attack.builder()
                .id(rawAttack.id())
                .name(rawAttack.name())
                .attackBonus(rawAttack.attackBonus())
                .attackAbility(attackAbility)
                .quantity(rawAttack.quantity())
                .attackType(attackType)
                .damageComponents(safeList(rawAttack.damageComponents()).stream()
                        .map(component -> DamageComponent.builder()
                                .formula(component.formula())
                                .damageType(parseDamageType(component.damageType()))
                                .multipliesOnCritical(component.multipliesOnCritical())
                                .damageAbility(damageAbility)
                                .damageAbilityMultiplier(damageAbility == DamageAbility.NONE ? 0.0 : 1.0)
                                .build())
                        .toList())
                .critical(rawAttack.critical() == null ? null : CriticalProfile.builder()
                        .threatRangeStart(rawAttack.critical().threatRangeStart())
                        .multiplier(rawAttack.critical().multiplier())
                        .build())
                .notes(safeList(rawAttack.notes()))
                .build();
    }

    private Alignment parseAlignment(String value) {
        return Alignment.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private CreatureSize parseCreatureSize(String value) {
        return CreatureSize.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private SummonTemplateType parseTemplateType(String value) {
        return SummonTemplateType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private SpeedType parseSpeedType(String value) {
        return SpeedType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private AttackType parseAttackType(String value) {
        return AttackType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private DamageType parseDamageType(String value) {
        return DamageType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private SpecialDefenseType parseSpecialDefenseType(String value) {
        return SpecialDefenseType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private HitDice parseHitDice(String formula) {
        if (formula == null) {
            return null;
        }

        String normalized = formula.replace(" ", "");
        int dIndex = normalized.toLowerCase(Locale.ROOT).indexOf('d');
        if (dIndex <= 0) {
            return null;
        }

        try {
            int count = Integer.parseInt(normalized.substring(0, dIndex));
            int plusIndex = normalized.indexOf('+', dIndex);
            int dieSize = plusIndex >= 0
                    ? Integer.parseInt(normalized.substring(dIndex + 1, plusIndex))
                    : Integer.parseInt(normalized.substring(dIndex + 1));
            return HitDice.builder()
                    .count(count)
                    .dieSize(dieSize)
                    .build();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : List.copyOf(values);
    }

    private record RawCreatureTemplate(
            String id,
            @JsonProperty("nombre") String nombre,
            @JsonProperty("nivelSummon") int nivelSummon,
            @JsonProperty("alineamiento") String alineamiento,
            @JsonProperty("tamano") String tamano,
            @JsonProperty("tipo") String tipo,
            @JsonProperty("subtipos") List<String> subtipos,
            @JsonProperty("plantillasPermitidas") List<String> plantillasPermitidas,
            @JsonProperty("iniciativa") int iniciativa,
            @JsonProperty("sentidos") List<String> sentidos,
            @JsonProperty("percepcion") int percepcion,
            @JsonProperty("atributos") RawAbilities atributos,
            @JsonProperty("ca") RawArmorClass ca,
            @JsonProperty("pg") RawHitPoints pg,
            @JsonProperty("salvaciones") RawSavingThrows salvaciones,
            @JsonProperty("velocidades") List<RawSpeed> velocidades,
            @JsonProperty("ataques") List<RawAttack> ataques,
            @JsonProperty("espacio") String espacio,
            @JsonProperty("alcance") String alcance,
            @JsonProperty("ataquesEspeciales") List<String> ataquesEspeciales,
            @JsonProperty("defensasEspeciales") List<RawSpecialDefense> defensasEspeciales,
            @JsonProperty("notasTacticas") List<String> notasTacticas,
            @JsonProperty("habilidadesResumidas") List<RawAbilitySummary> habilidadesResumidas,
            @JsonProperty("habilidadesCompletas") List<RawAbilityDetail> habilidadesCompletas,
            @JsonProperty("fullStatBlock") String fullStatBlock
    ) {
    }

    private record RawAbilities(
            @JsonProperty("fuerza") int fuerza,
            @JsonProperty("destreza") int destreza,
            @JsonProperty("constitucion") int constitucion,
            @JsonProperty("inteligencia") int inteligencia,
            @JsonProperty("sabiduria") int sabiduria,
            @JsonProperty("carisma") int carisma
    ) {
    }

    private record RawArmorClass(
            @JsonProperty("normal") int normal,
            @JsonProperty("toque") int toque,
            @JsonProperty("desprevenido") int desprevenido,
            @JsonProperty("detalle") String detalle
    ) {
    }

    private record RawHitPoints(
            @JsonProperty("maximos") int maximos,
            @JsonProperty("formula") String formula
    ) {
    }

    private record RawSavingThrows(
            @JsonProperty("fortaleza") int fortaleza,
            @JsonProperty("reflejos") int reflejos,
            @JsonProperty("voluntad") int voluntad
    ) {
    }

    private record RawSpeed(
            @JsonProperty("tipo") String tipo,
            @JsonProperty("valor") int valor
    ) {
    }

    private record RawCritical(
            @JsonProperty("threatRangeStart") int threatRangeStart,
            @JsonProperty("multiplier") int multiplier
    ) {
    }

    private record RawDamageComponent(
            @JsonProperty("formula") String formula,
            @JsonProperty("damageType") String damageType,
            @JsonProperty("multipliesOnCritical") boolean multipliesOnCritical
    ) {
    }

    private record RawAttack(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("attackBonus") int attackBonus,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("attackType") String attackType,
            @JsonProperty("damageComponents") List<RawDamageComponent> damageComponents,
            @JsonProperty("critical") RawCritical critical,
            @JsonProperty("notes") List<String> notes
    ) {
    }

    private record RawSpecialDefense(
            @JsonProperty("tipo") String tipo,
            @JsonProperty("valor") String valor,
            @JsonProperty("notas") String notas
    ) {
    }

    private record RawAbilitySummary(
            @JsonProperty("nombre") String nombre,
            @JsonProperty("resumen") String resumen
    ) {
    }

    private record RawAbilityDetail(
            @JsonProperty("nombre") String nombre,
            @JsonProperty("texto") String texto
    ) {
    }
}
