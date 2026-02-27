package com.marketplace.checkout.domain.repository;

import com.marketplace.checkout.domain.entities.Preference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferenceRepository extends JpaRepository<Preference, Long> {

}
