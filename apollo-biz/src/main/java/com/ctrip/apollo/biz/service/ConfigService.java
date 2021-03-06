package com.ctrip.apollo.biz.service;

import com.google.common.collect.Maps;

import com.ctrip.apollo.biz.entity.ReleaseSnapshot;
import com.ctrip.apollo.biz.entity.Version;
import com.ctrip.apollo.biz.repository.ReleaseSnapShotRepository;
import com.ctrip.apollo.biz.repository.VersionRepository;
import com.ctrip.apollo.core.dto.ApolloConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * Config Service
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Service("configService")
public class ConfigService {
  @Autowired
  private VersionRepository versionRepository;
  @Autowired
  private ReleaseSnapShotRepository releaseSnapShotRepository;
  @Autowired
  private ObjectMapper objectMapper;
  private TypeReference<Map<String, Object>> configurationTypeReference =
      new TypeReference<Map<String, Object>>() {
      };

  /**
   * Load configuration from database
   */
  public ApolloConfig loadConfig(String appId, String clusterName, String versionName) {
    Version version = loadVersionByAppIdAndVersionName(appId, versionName);
    if (version == null) {
      return null;
    }

    return loadConfigByVersionAndClusterName(version, clusterName);
  }

  /**
   * Load Version by appId and versionName from database
   */
  public Version loadVersionByAppIdAndVersionName(String appId, String versionName) {
    return versionRepository.findByAppIdAndName(appId, versionName);
  }

  /**
   * Load Config by version and clusterName from database
   */
  public ApolloConfig loadConfigByVersionAndClusterName(Version version, String clusterName) {
    ReleaseSnapshot releaseSnapShot =
        releaseSnapShotRepository
            .findByReleaseIdAndClusterName(version.getReleaseId(), clusterName);
    if (releaseSnapShot == null) {
      return null;
    }

    return assembleConfig(version, releaseSnapShot);
  }

  private ApolloConfig assembleConfig(Version version, ReleaseSnapshot releaseSnapShot) {
    ApolloConfig config =
        new ApolloConfig(version.getAppId(), releaseSnapShot.getClusterName(), version.getName(),
            version.getReleaseId());
    config.setConfigurations(transformConfigurationToMap(releaseSnapShot.getConfigurations()));

    return config;
  }

  Map<String, Object> transformConfigurationToMap(String configurations) {
    try {
      return objectMapper.readValue(configurations, configurationTypeReference);
    } catch (IOException e) {
      e.printStackTrace();
      return Maps.newHashMap();
    }
  }
}
