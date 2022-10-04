package tourGuide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;

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
public class TestPerformance {

	@BeforeEach
	public void init() {
		Locale.setDefault(Locale.US);
	}

	/*
	 * A note on performance improvements:
	 *
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *
	 *
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent.
	 *
	 *     These are performance metrics that we are trying to hit:
	 *
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	//@Ignore
//	@Test
//	public void highVolumeTrackLocation() {
//		GpsUtilService gpsUtilService = new GpsUtilService();
//		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
//		// Users should be incremented up to 100,000, and test finishes within 15 minutes
//		InternalTestHelper.setInternalUserNumber(1000);
//		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
//
//		List<User> allUsers = new ArrayList<>();
//		allUsers = tourGuideService.getAllUsers();
//
//		StopWatch stopWatch = new StopWatch();
//		stopWatch.start();
//		for(User user : allUsers) {
//			tourGuideService.trackUserLocation(user);
//		}
//		stopWatch.stop();
//		tourGuideService.tracker.stopTracking();
//
//		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
//		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
//	}

	@Test
	public void highVolumeTrackLocation() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		CompletableFuture<?>[] completableFutures = allUsers.stream()
						.map(tourGuideService.tracker::trackUserLocation)
								.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(completableFutures).join();

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

//	@Test
//	public void highVolumeTrackLocation() {
//		GpsUtilService gpsUtilService = new GpsUtilService();
//		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
//		// Users should be incremented up to 100,000, and test finishes within 15 minutes
//		InternalTestHelper.setInternalUserNumber(10000000);
//		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);
//
//		List<User> allUsers = new ArrayList<>();
//		allUsers = tourGuideService.getAllUsers();
//
//		StopWatch stopWatch = new StopWatch();
//		stopWatch.start();
////		for(User user : allUsers) {
////			tourGuideService.trackUserLocation(user);
////		}
//
//		ExecutorService executorService = Executors.newFixedThreadPool(100);
//		allUsers.stream().map(user -> CompletableFuture.supplyAsync(() -> tourGuideService.trackUserLocation(user), executorService));
//
//		stopWatch.stop();
//		tourGuideService.tracker.stopTracking();
//
//		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
//		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
//	}

	@Ignore
	@Test
	public void highVolumeGetRewards() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		ExecutorService executorService = Executors.newFixedThreadPool(100);
		Attraction attraction = gpsUtilService.getAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));


//		allUsers.forEach(u -> rewardsService.calculateRewards(u));
//		List<CompletableFuture<List<UserReward>>> getRewards = allUsers.stream().map(user -> CompletableFuture.supplyAsync(() -> rewardsService.calculateRewards(user), executorService)).collect(Collectors.toList());
//		getRewards.stream().map(CompletableFuture::join).collect(Collectors.toList());

		for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

}
