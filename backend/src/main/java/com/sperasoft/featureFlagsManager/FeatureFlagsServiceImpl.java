package com.sperasoft.featureFlagsManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sperasoft.featureFlagsManager.model.Feature;
import com.sperasoft.featureFlagsManager.model.FeatureFlag;
import com.sperasoft.featureFlagsManager.model.Region;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @NonNull
    private List<FlagDto> getFlagsFromRemoteService() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(SERVICE_URL).openConnection();
            InputStream responseInputStream = connection.getInputStream();
            if (connection.getResponseCode() == HttpStatus.OK.value()) {
                return Arrays.asList(new ObjectMapper().readValue(responseInputStream, FlagDto[].class));
            } else {
                ErrorModel errorModel = new ObjectMapper().readValue(responseInputStream, ErrorModel.class);
                throw new RuntimeException("Features flags data wasn't fetched. " + errorModel.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error during fetching features flags from service.", e.getCause());
        }
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

    @NonNull
    private List<FlagDto> saveFlagsToRemoteService(FlagDto flag) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(SERVICE_URL).openConnection();
            connection.setRequestMethod(HttpMethod.POST.name());
            connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            connection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                new ObjectMapper().writeValue(outputStream, flag);
            }
            InputStream responseInputStream = connection.getInputStream();
            if (connection.getResponseCode() == HttpStatus.OK.value()) {
                return Arrays.asList(new ObjectMapper().readValue(responseInputStream, FlagDto[].class));
            } else {
                ErrorModel errorModel = new ObjectMapper().readValue(responseInputStream, ErrorModel.class);
                throw new RuntimeException("Features flags data wasn't saved. " + errorModel.getMessage());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error during saving features flags.", e.getCause());
        }
    }

    @Override
    public List<Region> getRegions() {
        return new ArrayList<>(regions);
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

    @Setter
    @Getter
    @NoArgsConstructor
    private static class ErrorModel {
        private int code;
        private String message;
    }
}
