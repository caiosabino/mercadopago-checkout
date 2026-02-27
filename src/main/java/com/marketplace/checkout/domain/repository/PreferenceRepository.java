package com.marketplace.checkout.domain.repository;

import com.marketplace.checkout.domain.entities.PreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferenceRepository extends JpaRepository<PreferenceEntity, Long> {

}
