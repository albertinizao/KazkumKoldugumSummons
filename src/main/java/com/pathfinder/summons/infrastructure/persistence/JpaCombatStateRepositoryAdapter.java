package com.pathfinder.summons.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.pathfinder.summons.domain.model.CombatState;
import com.pathfinder.summons.domain.repository.CombatStateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class JpaCombatStateRepositoryAdapter implements CombatStateRepository {

    private static final Logger log = LoggerFactory.getLogger(JpaCombatStateRepositoryAdapter.class);
    private static final long SINGLETON_ID = 1L;

    private final CombatStateJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new JavaTimeModule());

    public JpaCombatStateRepositoryAdapter(CombatStateJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CombatState getCombatState() {
        return repository.findById(SINGLETON_ID)
                .map(CombatStateEntity::getPayload)
                .map(this::deserializeSafely)
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

    private CombatState deserializeSafely(String payload) {
        try {
            return objectMapper.readValue(payload, CombatState.class);
        } catch (JsonProcessingException ex) {
            log.warn("Se encontró un estado de combate persistido incompatible; se eliminará y se recreará.", ex);
            repository.deleteById(SINGLETON_ID);
            return null;
        }
    }
}
