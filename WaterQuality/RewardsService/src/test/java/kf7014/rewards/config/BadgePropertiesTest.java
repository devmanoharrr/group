package kf7014.rewards.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BadgePropertiesTest {

    private BadgeProperties badgeProperties;

    @BeforeEach
    void setUp() {
        badgeProperties = new BadgeProperties();
    }

    @Test
    void getBadges_withEmptyList_returnsEmpty() {
        badgeProperties.setBadges(new ArrayList<>());
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadges();
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getBadges_withSingleBadge_returnsBadge() {
        BadgeProperties.BadgeRule rule = new BadgeProperties.BadgeRule();
        rule.setName("Bronze");
        rule.setThreshold(100);
        
        badgeProperties.setBadges(List.of(rule));
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadges();
        
        assertEquals(1, result.size());
        assertEquals("Bronze", result.get(0).getName());
        assertEquals(100, result.get(0).getThreshold());
    }

    @Test
    void getBadgesSortedDesc_withMultipleBadges_sortsDescending() {
        BadgeProperties.BadgeRule bronze = new BadgeProperties.BadgeRule();
        bronze.setName("Bronze");
        bronze.setThreshold(100);
        
        BadgeProperties.BadgeRule silver = new BadgeProperties.BadgeRule();
        silver.setName("Silver");
        silver.setThreshold(200);
        
        BadgeProperties.BadgeRule gold = new BadgeProperties.BadgeRule();
        gold.setName("Gold");
        gold.setThreshold(500);
        
        badgeProperties.setBadges(List.of(bronze, silver, gold));
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadgesSortedDesc();
        
        assertEquals(3, result.size());
        assertEquals("Gold", result.get(0).getName()); // Highest threshold first
        assertEquals("Silver", result.get(1).getName());
        assertEquals("Bronze", result.get(2).getName());
    }

    @Test
    void getBadgesSortedDesc_withUnsortedBadges_sortsDescending() {
        BadgeProperties.BadgeRule bronze = new BadgeProperties.BadgeRule();
        bronze.setName("Bronze");
        bronze.setThreshold(100);
        
        BadgeProperties.BadgeRule gold = new BadgeProperties.BadgeRule();
        gold.setName("Gold");
        gold.setThreshold(500);
        
        BadgeProperties.BadgeRule silver = new BadgeProperties.BadgeRule();
        silver.setName("Silver");
        silver.setThreshold(200);
        
        // Add in unsorted order
        badgeProperties.setBadges(List.of(bronze, gold, silver));
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadgesSortedDesc();
        
        assertEquals(3, result.size());
        assertEquals("Gold", result.get(0).getName());
        assertEquals("Silver", result.get(1).getName());
        assertEquals("Bronze", result.get(2).getName());
    }

    @Test
    void getBadgesSortedDesc_withEqualThresholds_handlesGracefully() {
        BadgeProperties.BadgeRule rule1 = new BadgeProperties.BadgeRule();
        rule1.setName("Badge1");
        rule1.setThreshold(100);
        
        BadgeProperties.BadgeRule rule2 = new BadgeProperties.BadgeRule();
        rule2.setName("Badge2");
        rule2.setThreshold(100);
        
        badgeProperties.setBadges(List.of(rule1, rule2));
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadgesSortedDesc();
        
        assertEquals(2, result.size());
        assertEquals(100, result.get(0).getThreshold());
        assertEquals(100, result.get(1).getThreshold());
    }

    @Test
    void getBadgesSortedDesc_withZeroThreshold_handlesGracefully() {
        BadgeProperties.BadgeRule rule = new BadgeProperties.BadgeRule();
        rule.setName("Starter");
        rule.setThreshold(0);
        
        badgeProperties.setBadges(List.of(rule));
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadgesSortedDesc();
        
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getThreshold());
    }

    @Test
    void getBadgesSortedDesc_withNegativeThreshold_handlesGracefully() {
        BadgeProperties.BadgeRule rule = new BadgeProperties.BadgeRule();
        rule.setName("Negative");
        rule.setThreshold(-10);
        
        badgeProperties.setBadges(List.of(rule));
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadgesSortedDesc();
        
        assertEquals(1, result.size());
        assertEquals(-10, result.get(0).getThreshold());
    }

    @Test
    void getBadgesSortedDesc_withManyBadges_sortsCorrectly() {
        List<BadgeProperties.BadgeRule> rules = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            BadgeProperties.BadgeRule rule = new BadgeProperties.BadgeRule();
            rule.setName("Badge" + i);
            rule.setThreshold(i * 10);
            rules.add(rule);
        }
        
        badgeProperties.setBadges(rules);
        
        List<BadgeProperties.BadgeRule> result = badgeProperties.getBadgesSortedDesc();
        
        assertEquals(10, result.size());
        assertEquals(100, result.get(0).getThreshold()); // Highest first
        assertEquals(10, result.get(9).getThreshold()); // Lowest last
    }

    @Test
    void badgeRule_gettersAndSetters_workCorrectly() {
        BadgeProperties.BadgeRule rule = new BadgeProperties.BadgeRule();
        
        rule.setName("Test");
        rule.setThreshold(150);
        
        assertEquals("Test", rule.getName());
        assertEquals(150, rule.getThreshold());
    }

    @Test
    void badgeRule_withNullName_handlesGracefully() {
        BadgeProperties.BadgeRule rule = new BadgeProperties.BadgeRule();
        rule.setName(null);
        rule.setThreshold(100);
        
        assertNull(rule.getName());
        assertEquals(100, rule.getThreshold());
    }

    @Test
    void badgeRule_withEmptyName_handlesGracefully() {
        BadgeProperties.BadgeRule rule = new BadgeProperties.BadgeRule();
        rule.setName("");
        rule.setThreshold(100);
        
        assertEquals("", rule.getName());
    }

    @Test
    void setBadges_withNull_handlesGracefully() {
        assertDoesNotThrow(() -> {
            badgeProperties.setBadges(null);
        });
    }

    @Test
    void getBadgesSortedDesc_withNullList_handlesGracefully() {
        badgeProperties.setBadges(null);
        
        assertThrows(NullPointerException.class, () -> {
            badgeProperties.getBadgesSortedDesc();
        });
    }
}

