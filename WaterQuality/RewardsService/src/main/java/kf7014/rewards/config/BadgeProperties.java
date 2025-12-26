package kf7014.rewards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "rewards")
public class BadgeProperties {

    private List<BadgeRule> badges = new ArrayList<>();

    public List<BadgeRule> getBadges() {
        return badges;
    }

    public void setBadges(List<BadgeRule> badges) {
        this.badges = badges;
    }

    public List<BadgeRule> getBadgesSortedDesc() {
        return badges.stream()
                .sorted(Comparator.comparingInt(BadgeRule::getThreshold).reversed())
                .toList();
    }

    public static class BadgeRule {
        private String name;
        private int threshold;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getThreshold() {
            return threshold;
        }

        public void setThreshold(int threshold) {
            this.threshold = threshold;
        }
    }
}


