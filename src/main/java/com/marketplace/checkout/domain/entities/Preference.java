package com.marketplace.checkout.domain.entities;
import jakarta.persistence.*;

@Table(name = "preference")
@Entity
public class Preference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String surName;

    @Column
    private String preferenceId;

    public Preference() {}

    public Preference(Long id, String name, String surName, String preferenceId) {
        this.id = id;
        this.name = name;
        this.surName = surName;
        this.preferenceId = preferenceId;
    }

    public static PreferenceBuilder builder() {
        return new PreferenceBuilder();
    }

    public static class PreferenceBuilder {
        private Long id;
        private String name;
        private String surName;
        private String preferenceId;

        public PreferenceBuilder id(Long id) { this.id = id; return this; }
        public PreferenceBuilder name(String name) { this.name = name; return this; }
        public PreferenceBuilder surName(String surName) { this.surName = surName; return this; }
        public PreferenceBuilder preferenceId(String preferenceId) { this.preferenceId = preferenceId; return this; }

        public Preference build() {
            return new Preference(id, name, surName, preferenceId);
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurName() { return surName; }
    public String getPreferenceId() { return preferenceId; }
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSurName(String surName) { this.surName = surName; }
    public void setPreferenceId(String preferenceId) { this.preferenceId = preferenceId; }
}

