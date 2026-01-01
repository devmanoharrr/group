package com.bharath.wq.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("upstreams")
public class UpstreamsProperties {
  private String data;
  private String rewards;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getRewards() {
    return rewards;
  }

  public void setRewards(String rewards) {
    this.rewards = rewards;
  }
}
