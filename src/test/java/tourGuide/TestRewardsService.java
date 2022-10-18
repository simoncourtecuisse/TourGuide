package tourGuide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Ignore;
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

@SpringBootTest
public class TestRewardsService {

	@BeforeEach
	public void init() {
		Locale.setDefault(Locale.US);
	}

//	@Test
//	public void userGetRewards() {
//		GpsUtilService gpsUtilService = new GpsUtilService();
//		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
//
//		InternalTestHelper.setInternalUserNumber(0);
//		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
//
//		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
//		Attraction attraction = gpsUtilService.getAttractions().get(0);
//		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
//		tourGuideService.trackUserLocation(user);
//		List<UserReward> userRewards = user.getUserRewards();
//		tourGuideService.tracker.stopTracking();
//		assertTrue(userRewards.size() == 1);
//	}

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
		tourGuideService.tracker.stopTracking();
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

//		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0)).join();
//		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
//		tourGuideService.tracker.stopTracking();

		CompletableFuture<?>[] completableFutures = tourGuideService.getAllUsers().stream()
				.map(rewardsService::calculateRewards)
				.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(completableFutures).join();

		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtilService.getAttractions().size(), userRewards.size());  /* comparaison entre nbre d'attraction et un nbre de rewards, le sens ? */
	}
}
