package com.handi.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeniorUserRelationsId implements Serializable {
    private Integer userId;
    private Integer seniorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeniorUserRelationsId that = (SeniorUserRelationsId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(seniorId, that.seniorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, seniorId);
    }
}