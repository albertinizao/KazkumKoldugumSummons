package com.pathfinder.summons.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "combat_state")
@Getter
@Setter
@NoArgsConstructor
public class CombatStateEntity {

    @Id
    private Long id;

    @Lob
    @Column(nullable = false)
    private String payload;

    public CombatStateEntity(Long id, String payload) {
        this.id = id;
        this.payload = payload;
    }
}
