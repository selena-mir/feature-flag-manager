package com.sperasoft.featureFlagsManager.rest;

import com.sperasoft.featureFlagsManager.FeatureFlagsService;
import com.sperasoft.featureFlagsManager.model.FeatureFlag;
import com.sperasoft.featureFlagsManager.model.Region;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(value = {"${frontend.url}"})
@RestController()
@AllArgsConstructor
public class FeatureFlagsController {
    @NonNull
    private final FeatureFlagsService featureFlagsService;

    @RequestMapping("featureFlags/")
    public FeaturesDto getFlags() {
        List<FeatureFlagsDto> flagsDtos = featureFlagsService.getFlags()
                .stream()
                .map(FeatureFlagsDto::new)
                .collect(Collectors.toList());
        return new FeaturesDto(getRegions(), flagsDtos);
    }

    @PostMapping("featureFlags/")
    public FeaturesDto saveFlags(@NonNull @RequestBody List<FeatureFlagsDto> flagsDtos) {
        List<FeatureFlag> flags = flagsDtos.stream()
                .map(FeatureFlagsDto::getFeatureFlagsFromDto)
                .collect(Collectors.toList());
        List<FeatureFlagsDto> resultFlags = featureFlagsService.saveFlags(flags)
                .stream()
                .map(FeatureFlagsDto::new)
                .collect(Collectors.toList());
        return new FeaturesDto(getRegions(), resultFlags);
    }

    @NonNull
    private List<String> getRegions() {
        return featureFlagsService.getRegions().stream().map(Region::getName).collect(Collectors.toList());
    }
}