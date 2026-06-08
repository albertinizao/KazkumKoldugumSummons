package com.pathfinder.summons.web;

import com.pathfinder.summons.application.CombatStateService;
import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.model.GroupAttackRollResponse;
import com.pathfinder.summons.domain.model.GroupSavingThrowsRollResponse;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/combat-state")
public class CombatStateController {

    private final CombatStateService service;

    public CombatStateController(CombatStateService service) {
        this.service = service;
    }

    @GetMapping
    public CombatState getCombatState() {
        return service.getCombatState();
    }

    @PostMapping("/summons")
    public CombatState summon(@Valid @RequestBody SummonCreatureRequest request) {
        if (request.shortcutId() != null && !request.shortcutId().isBlank()) {
            return service.summonFromShortcut(request.shortcutId(), request.source(), request.selectedTemplate());
        }
        if (request.creatureTemplateId() == null || request.creatureTemplateId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "creatureTemplateId es obligatorio");
        }
        return service.summon(request.creatureTemplateId(), request.selectedTemplate());
    }

    @DeleteMapping("/summons")
    public CombatState clearSummons() {
        return service.clearSummons();
    }

    @DeleteMapping("/instances/{instanceId}")
    public CombatState removeInstance(@PathVariable String instanceId) {
        ActiveInstanceReference reference = findInstanceReference(instanceId);
        return service.removeInstance(reference.groupId(), instanceId);
    }

    @PostMapping("/instances/{instanceId}/damage")
    public CombatState damageInstance(@PathVariable String instanceId, @Valid @RequestBody AmountRequest request) {
        ActiveInstanceReference reference = findInstanceReference(instanceId);
        return service.damageInstance(reference.groupId(), instanceId, request.amount());
    }

    @PostMapping("/instances/{instanceId}/heal")
    public CombatState healInstance(@PathVariable String instanceId, @Valid @RequestBody AmountRequest request) {
        ActiveInstanceReference reference = findInstanceReference(instanceId);
        return service.healInstance(reference.groupId(), instanceId, request.amount());
    }

    @DeleteMapping("/last-roll-result")
    public CombatState clearLastRollResult() {
        return service.clearLastRollResult();
    }

    @PostMapping("/groups/{groupId}/roll-attacks")
    public GroupAttackRollResponse rollGroupAttacks(@PathVariable String groupId) {
        return service.rollGroupAttacks(groupId);
    }

    @PostMapping("/groups/{groupId}/roll-saving-throws")
    public GroupSavingThrowsRollResponse rollGroupSavingThrows(@PathVariable String groupId) {
        return service.rollGroupSavingThrows(groupId);
    }

    private ActiveInstanceReference findInstanceReference(String instanceId) {
        CombatState state = service.getCombatState();
        return state.getActiveGroups().stream()
                .flatMap(group -> group.getInstances().stream().map(instance -> new ActiveInstanceReference(group.getId(), instance.getId())))
                .filter(reference -> reference.instanceId().equals(instanceId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la instancia " + instanceId));
    }

    public record SummonCreatureRequest(
            String creatureTemplateId,
            SummonTemplateType selectedTemplate,
            String shortcutId,
            String source
    ) {
    }

    public record AmountRequest(@NotNull @Min(1) Integer amount) {
    }

    private record ActiveInstanceReference(String groupId, String instanceId) {
    }
}
