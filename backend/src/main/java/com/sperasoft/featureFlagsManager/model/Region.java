package com.sperasoft.featureFlagsManager.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(of = "name")
public class Region {
    private final String name;
}
