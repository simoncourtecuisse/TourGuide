package tourGuide;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TestRewardsService {

    @BeforeEach
    public void init() {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void userGetRewards() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());

        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        rewardsService.calculateRewards(user).join();
        List<UserReward> userRewards = user.getUserRewards();
        List<User> users = new ArrayList<>();
        users.add(user);
        assertEquals(1, userRewards.size());
    }

    @Test
    public void isWithinAttractionProximity() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
        Attraction attraction = gpsUtilService.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    //@Ignore // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        CompletableFuture<?>[] completableFutures = tourGuideService.getAllUsers().stream()
                .map(rewardsService::calculateRewards)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
        rewardsService.setDefaultProximityBuffer();

        List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));

        assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());
    }
}
