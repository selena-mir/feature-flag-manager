package com.sperasoft.featureFlagsManager.rest;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeaturesDto {
    private List<String> regions;
    private List<FeatureFlagsDto> features;
}
