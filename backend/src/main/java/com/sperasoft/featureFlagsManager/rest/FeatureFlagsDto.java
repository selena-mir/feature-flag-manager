package com.sperasoft.featureFlagsManager.rest;

import com.sperasoft.featureFlagsManager.model.Feature;
import com.sperasoft.featureFlagsManager.model.FeatureFlag;
import com.sperasoft.featureFlagsManager.model.Region;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class FeatureFlagsDto {
    private String featureName;
    private Map<String, Boolean> regionFlags = new HashMap<>();

    public FeatureFlagsDto(@NonNull FeatureFlag featureFlag) {
        featureName = featureFlag.getFeature().getName();
        featureFlag.getFlags().forEach((region, flag) -> regionFlags.put(region.getName(), flag));
    }

    public static FeatureFlag getFeatureFlagsFromDto(FeatureFlagsDto dto) {
        Map<Region, Boolean> flags = dto.regionFlags.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> new Region(entry.getKey()), Map.Entry::getValue));
        return new FeatureFlag(new Feature(dto.featureName), flags);
    }
}
