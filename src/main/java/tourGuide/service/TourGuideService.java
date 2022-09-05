package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.NearByAttraction;
import tourGuide.model.UserNearByAttractions;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

		if(testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
				user.getLastVisitedLocation() :
				trackUserLocation(user);
		return visitedLocation;
	}

	public User getUser(String userName) {

		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
		List<Attraction> nearbyAttractions = new ArrayList<>();
		for(Attraction attraction : gpsUtil.getAttractions()) {
			if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
				nearbyAttractions.add(attraction);
			}
		}

		return nearbyAttractions;
	}

//	public List<Attraction> getNearFiveAttractions(VisitedLocation visitedLocation, User user) {
//		visitedLocation = user.getLastVisitedLocation();
//		Location location = visitedLocation.location;
//		List<Attraction> attractions = new ArrayList<>();
//		List<Attraction> nearFiveAttractions = getNearByAttractions(visitedLocation);
//
//		for (Attraction attraction: gpsUtil.getAttractions()) {
//			nearFiveAttractions
//
//		}
//
//		return nearFiveAttractions;
//	}

	public UserNearByAttractions getNearFiveAttractions(User user) {
		UserNearByAttractions nearByAttractions = new UserNearByAttractions();
		nearByAttractions.setNearByAttractions(new ArrayList<>());

		VisitedLocation visitedLocation = getUserLocation(user);
		nearByAttractions.setUserLocation(visitedLocation.location);

		Map<Double, Attraction> nearByAttractionsMap = new TreeMap<>();

		List<Attraction> attractions = gpsUtil.getAttractions();
		for (Attraction attraction : attractions) {
			nearByAttractionsMap.put(rewardsService.getDistance(attraction, getUserLocation(user).location), attraction);
		}

		List<Attraction> closest5Attractions = nearByAttractionsMap
				.entrySet()
				.stream()
				.limit(5)
				.collect(
						ArrayList::new, (attraction, e) -> attraction.add(e.getValue()), ArrayList::addAll
				);

		closest5Attractions.forEach(
				attraction -> {
					NearByAttraction nearByAttraction = new NearByAttraction();
					nearByAttraction.setAttractionName(attraction.attractionName);
					nearByAttraction.setAttractionLocation(attraction.latitude, attraction.longitude);
					nearByAttraction.setDistance(rewardsService.getDistance(attraction, visitedLocation.location));
					nearByAttraction.setRewardPoints(rewardsService.getRewardPoints(attraction, user));
					nearByAttractions.getNearByAttractions().add(nearByAttraction);
				}
		);
		return nearByAttractions;
	}

	public UserNearByAttractions getNearTenAttractions(User user) {
		UserNearByAttractions nearByAttractions = new UserNearByAttractions();
		nearByAttractions.setNearByAttractions(new ArrayList<>());

		VisitedLocation visitedLocation = getUserLocation(user);
		nearByAttractions.setUserLocation(visitedLocation.location);

		Map<Double, Attraction> nearByAttractionsMap = new TreeMap<>();

		List<Attraction> attractions = gpsUtil.getAttractions();
		for (Attraction attraction : attractions) {
			nearByAttractionsMap.put(rewardsService.getDistance(attraction, getUserLocation(user).location), attraction);
		}

		List<Attraction> closest10Attractions = nearByAttractionsMap
				.entrySet()
				.stream()
				.limit(10)
				.collect(
						ArrayList::new, (attraction, e) -> attraction.add(e.getValue()), ArrayList::addAll
				);

		closest10Attractions.forEach(
				attraction -> {
					NearByAttraction nearByAttraction = new NearByAttraction();
					nearByAttraction.setAttractionName(attraction.attractionName);
					nearByAttraction.setAttractionLocation(attraction.latitude, attraction.longitude);
					nearByAttraction.setDistance(rewardsService.getDistance(attraction, visitedLocation.location));
					nearByAttraction.setRewardPoints(rewardsService.getRewardPoints(attraction, user));
					nearByAttractions.getNearByAttractions().add(nearByAttraction);
				}
		);
		return nearByAttractions;
	}


	public HashMap<String, Location> getAllCurrentLocations() {
		HashMap<String, Location> allCurrentLocations = new HashMap<>();
		getAllUsers().forEach(user -> allCurrentLocations.put(user.getLastVisitedLocation().userId.toString(), user.getLastVisitedLocation().location));
		System.out.println(getAllUsers());
		return allCurrentLocations;
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 *
	 * Methods Below: For Internal Testing
	 *
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
