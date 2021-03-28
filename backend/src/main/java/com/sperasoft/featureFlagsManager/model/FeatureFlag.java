package com.sperasoft.featureFlagsManager.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FeatureFlag {
    private Feature feature;
    private Map<Region, Boolean> flags;
}

