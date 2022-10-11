package tourGuide;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.*;

import gpsUtil.location.Location;
import org.junit.jupiter.api.Test;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.UserNearByAttractions;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tripPricer.Provider;

@SpringBootTest
public class TestTourGuideService {

	@BeforeEach
	public void init() {
		Locale.setDefault(Locale.US);
	}

	@Test
	public void getUserLocation() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user).join();
		tourGuideService.tracker.stopTracking();
		assertEquals(user.getLastVisitedLocation().userId, user.getUserId());

	}

	@Test
	public void addUser() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		User retrivedUser = tourGuideService.getUser(user.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();

		assertEquals(user, retrivedUser);
		assertEquals(user2, retrivedUser2);
	}

	@Test
	public void getAllUsers() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}

	@Test
	public void trackUser() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user).join();

		tourGuideService.tracker.stopTracking();

		assertEquals(user.getUserId(), user.getLastVisitedLocation().userId);
	}

//	@Ignore // Not yet implemented
//	@Test
//	public void getNearbyAttractions() {
//		GpsUtil gpsUtil = new GpsUtil();
//		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
//		InternalTestHelper.setInternalUserNumber(0);
//		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
//
//		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
//		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
//
//		List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
//
//		tourGuideService.tracker.stopTracking();
//
//		assertEquals(5, attractions.size());
//	}

//	@Test
//	public void getNearbyAttractions() {
//		GpsUtil gpsUtil = new GpsUtil();
//		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
//		InternalTestHelper.setInternalUserNumber(0);
//		TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
//
//		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
//		VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
//
//		List<Attraction> attractions = new ArrayList<>();
//		Attraction attraction1 = new Attraction("attraction1", "city1", "state1", 1.0, 1.0);
//		Attraction attraction2 = new Attraction("attraction2", "city2", "state2", 2.0, 2.0);
//		Attraction attraction3 = new Attraction("attraction3", "city3", "state3", 3.0, 3.0);
//		Attraction attraction4 = new Attraction("attraction4", "city4", "state4", 4.0, 4.0);
//		Attraction attraction5 = new Attraction("attraction5", "city5", "state5", 5.0, 5.0);
//		Attraction attraction6 = new Attraction("attraction5", "city5", "state5", 6.0, 6.0);
//		attractions.add(attraction1);
//		attractions.add(attraction2);
//		attractions.add(attraction3);
//		attractions.add(attraction4);
//		attractions.add(attraction5);
//		attractions.add(attraction6);
//
//		UserNearByAttractions nearByAttractions = tourGuideService.getNearFiveAttractions(user);
//		System.out.println(nearByAttractions);
//		tourGuideService.tracker.stopTracking();
//
//		assertEquals(5, nearByAttractions.getNearByAttractions().size());
//	}

	@Test
	public void getNearbyAttractions() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.trackUserLocation(user).join();

		List<Attraction> attractionsList = tourGuideService.getNearByAttractions(user);
		System.out.println(attractionsList);

		tourGuideService.tracker.stopTracking();

		assertEquals(26, attractionsList.size());
	}



	@Test
	public void getTripDeals() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<Provider> providers = tourGuideService.getTripDeals(user);
		System.out.println(providers);

		tourGuideService.tracker.stopTracking();

		assertEquals(5, providers.size());
	}

	@Test
	public void getAllCurrentUsersLocationsTest() {
		GpsUtilService gpsUtilService = new GpsUtilService();
		RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(100);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);


		HashMap<String, Location> allCurrentLocations = tourGuideService.getAllCurrentLocations();

		tourGuideService.tracker.stopTracking();

		assertEquals(100, allCurrentLocations.size());
	}

}
