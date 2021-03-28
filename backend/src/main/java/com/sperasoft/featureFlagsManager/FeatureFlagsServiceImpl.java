package com.sperasoft.featureFlagsManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.featureFlagsManager.model.Feature;
import com.sperasoft.featureFlagsManager.model.FeatureFlag;
import com.sperasoft.featureFlagsManager.model.Region;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FeatureFlagsServiceImpl implements FeatureFlagsService {
    @Value("${featureflags.service.url}")
    private String SERVICE_URL;
    @Value("${featureflags.regions}")
    private List<Region> regions;

    @Override
    public List<FeatureFlag> getFlags() {
        return getFlagsFromDtos(getFlagsFromRemoteService());
    }

    @SneakyThrows
    private List<FlagDto> getFlagsFromRemoteService() {
        HttpURLConnection connection = (HttpURLConnection) new URL(SERVICE_URL).openConnection();
        connection.setRequestMethod("GET");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = bufferedReader.readLine();
        return Arrays.asList(new ObjectMapper().readValue(response, FlagDto[].class));
    }

    private List<FeatureFlag> getFlagsFromDtos(List<FlagDto> flags) {
        return flags.stream().map(dto -> dto.getDataForRegions(regions)).collect(Collectors.toList());
    }

    @Override
    public List<FeatureFlag> saveFlags(List<FeatureFlag> flags) {
        List<FlagDto> result = new ArrayList<>();
        for (FeatureFlag flag : flags) {
            result = saveFlagsToRemoteService(new FlagDto(flag, regions));
        }
        return getFlagsFromDtos(result);
    }

    @SneakyThrows
    private List<FlagDto> saveFlagsToRemoteService(FlagDto flag) {
        HttpURLConnection connection = (HttpURLConnection) new URL(SERVICE_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            new ObjectMapper().writeValue(os, flag);
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = bufferedReader.readLine();
        return Arrays.asList(new ObjectMapper().readValue(response, FlagDto[].class));
    }

    @Override
    public List<Region> getRegions() {
        return Collections.unmodifiableList(regions);
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class FlagDto {
        String name;
        int value;

        public FeatureFlag getDataForRegions(List<Region> regions) {
            return new FeatureFlag(new Feature(name), getFlagsByRegions(regions));
        }

        private Map<Region, Boolean> getFlagsByRegions(List<Region> regions) {
            boolean[] bitMap = getBitmapFromValue(regions.size());
            Map<Region, Boolean> result = new HashMap<>();
            for (int i = 0; i < regions.size(); i++) {
                result.put(regions.get(i), bitMap[i]);
            }
            return result;
        }

        private boolean[] getBitmapFromValue(int size) {
            boolean[] result = new boolean[size];
            for (int i = 0; i < size; i++) {
                int digit = size - 1 - i;
                result[i] = (value & (1 << digit)) != 0;
            }
            return result;
        }

        public FlagDto(FeatureFlag featureFlag, List<Region> regions) {
            name = featureFlag.getFeature().getName();
            value = getValueFromBitmap(createArrayOfFlags(featureFlag.getFlags(), regions));
        }

        private boolean[] createArrayOfFlags(Map<Region, Boolean> flags, List<Region> regions) {
            boolean[] result = new boolean[regions.size()];
            for (int i = 0; i < regions.size(); i++) {
                result[i] = flags.get(regions.get(i));
            }
            return result;
        }

        private int getValueFromBitmap(boolean[] bitmap) {
            int result = 0;
            for (int i = 0; i < bitmap.length; i++) {
                int digit = bitmap.length - 1 - i;
                result |= (bitmap[i] ? 1 : 0) * (1 << digit);
            }
            return result;
        }
    }
}
