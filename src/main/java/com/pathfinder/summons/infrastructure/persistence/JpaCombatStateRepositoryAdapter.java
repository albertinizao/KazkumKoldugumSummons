package com.pathfinder.summons.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.repository.CombatStateRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCombatStateRepositoryAdapter implements CombatStateRepository {

    private static final long SINGLETON_ID = 1L;

    private final CombatStateJpaRepository repository;
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    public JpaCombatStateRepositoryAdapter(CombatStateJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CombatState getCombatState() {
        return repository.findById(SINGLETON_ID)
                .map(CombatStateEntity::getPayload)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveCombatState(CombatState combatState) {
        try {
            String payload = objectMapper.writeValueAsString(combatState);
            repository.save(new CombatStateEntity(SINGLETON_ID, payload));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el estado de combate", ex);
        }
    }

    @Override
    public void clear() {
        repository.deleteById(SINGLETON_ID);
    }

    private CombatState deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, CombatState.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo leer el estado de combate", ex);
        }
    }
}
