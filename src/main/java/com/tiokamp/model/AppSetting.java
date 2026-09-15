package com.tiokamp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple key/value store for runtime-editable settings (e.g. the macaroni facit),
 * so an admin can change them in the UI and they persist across restarts.
 */
@Entity
@Table(name = "app_settings")
@Data
@NoArgsConstructor
public class AppSetting {

    @Id
    private String name;

    // "value" is a reserved word in H2/SQL, so map it to a safe column name.
    @Column(name = "setting_value")
    private String value;

    public AppSetting(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
