package com.pathfinder.summons.web;

import com.pathfinder.summons.application.CreatureCatalogService;
import com.pathfinder.summons.application.CreatureCatalogService.CreatureCatalogItemSummary;
import com.pathfinder.summons.domain.model.CreatureTemplate;
import com.pathfinder.summons.domain.model.ResolvedCreature;
import com.pathfinder.summons.domain.model.SummonTemplateType;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CreatureCatalogService service;

    public CatalogController(CreatureCatalogService service) {
        this.service = service;
    }

    @GetMapping("/creatures")
    public CreatureCatalogListResponse listCreatures(@RequestParam(required = false) String query,
                                                     @RequestParam(required = false) Integer summonLevel,
                                                     @RequestParam(required = false) Integer maxSummonLevel,
                                                     @RequestParam(required = false) String template,
                                                     @RequestParam(defaultValue = "50") @Min(1) int limit,
                                                     @RequestParam(defaultValue = "0") @Min(0) int offset) {
        SummonTemplateType templateType = parseTemplate(template);
        List<CreatureTemplate> creatures = service.findCreatures(query, summonLevel, maxSummonLevel, templateType, limit, offset);
        return new CreatureCatalogListResponse(
                creatures.stream().map(this::toItem).toList(),
                service.countCreatures(query, summonLevel, maxSummonLevel, templateType)
        );
    }

    @GetMapping("/creatures/{creatureTemplateId}")
    public CreatureTemplate getCreatureTemplate(@PathVariable String creatureTemplateId) {
        try {
            return service.getCreatureTemplate(creatureTemplateId);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/creatures/{creatureTemplateId}/resolved-preview")
    public ResolvedCreature previewResolvedCreature(@PathVariable String creatureTemplateId,
                                                    @RequestParam(required = false) String template) {
        SummonTemplateType templateType = parseTemplate(template);
        try {
            return service.previewResolvedCreature(creatureTemplateId, templateType);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    private CreatureCatalogItem toItem(CreatureTemplate creatureTemplate) {
        CreatureCatalogItemSummary summary = service.toSummary(creatureTemplate);
        return new CreatureCatalogItem(
                creatureTemplate.getId(),
                creatureTemplate.getName(),
                creatureTemplate.getSummonLevel(),
                creatureTemplate.getAlignment(),
                creatureTemplate.getSize(),
                creatureTemplate.getCreatureType(),
                creatureTemplate.getSubtypes(),
                creatureTemplate.getAllowedTemplates(),
                summary
        );
    }

    private SummonTemplateType parseTemplate(String template) {
        if (template == null || template.isBlank()) {
            return null;
        }

        try {
            return SummonTemplateType.valueOf(template.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plantilla inválida: " + template, ex);
        }
    }

    public record CreatureCatalogListResponse(List<CreatureCatalogItem> items, int total) {
    }

    public record CreatureCatalogItem(
            String id,
            String name,
            int summonLevel,
            com.pathfinder.summons.domain.model.Alignment alignment,
            com.pathfinder.summons.domain.model.CreatureSize size,
            String creatureType,
            List<String> subtypes,
            List<SummonTemplateType> allowedTemplates,
            CreatureCatalogItemSummary summary
    ) {
    }
}
