package com.epicplayera10.optimizationutils.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Header;

import java.util.HashMap;
import java.util.Map;

@Header("The data configuration stores persistent data for the plugin.")
@Header("DO NOT MODIFY IT UNLESS YOU KNOW WHAT YOU ARE DOING!")
@Header("")
public class DataConfiguration extends OkaeriConfig {
    public Map<String, Integer> originalRandomTickSpeeds = new HashMap<>();
}
