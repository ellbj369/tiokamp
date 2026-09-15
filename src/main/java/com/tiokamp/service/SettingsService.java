package com.tiokamp.service;

import com.tiokamp.model.AppSetting;
import com.tiokamp.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private static final String MAKARONI_FACIT = "makaroni-facit";

    private final AppSettingRepository repository;

    // Fallback used until an admin sets the facit in the UI: from
    // application.properties / the APP_MAKARONI_FACIT environment variable.
    @Value("${app.makaroni-facit:#{null}}")
    private Double configFacit;

    /** The macaroni facit set in the admin UI, or the configured fallback, or null. */
    @Transactional(readOnly = true)
    public Double getMakaroniFacit() {
        return repository.findById(MAKARONI_FACIT)
                .map(setting -> parse(setting.getValue()))
                .orElse(configFacit);
    }

    @Transactional
    public void setMakaroniFacit(Double value) {
        if (value == null || value.isNaN() || value.isInfinite() || value < 0) {
            throw new IllegalArgumentException("Facit måste vara ett positivt tal (antal makaroner).");
        }
        AppSetting setting = repository.findById(MAKARONI_FACIT)
                .orElseGet(() -> new AppSetting(MAKARONI_FACIT, null));
        setting.setValue(String.valueOf(value));
        repository.save(setting);
    }

    private Double parse(String value) {
        try {
            return value == null ? null : Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
