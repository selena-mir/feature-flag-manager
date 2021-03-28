package com.sperasoft.featureFlagsManager;

import com.sperasoft.featureFlagsManager.model.FeatureFlag;
import com.sperasoft.featureFlagsManager.model.Region;
import java.util.List;

public interface FeatureFlagsService {
    List<FeatureFlag> getFlags();

    List<FeatureFlag> saveFlags(List<FeatureFlag> flags);

    List<Region> getRegions();
}
