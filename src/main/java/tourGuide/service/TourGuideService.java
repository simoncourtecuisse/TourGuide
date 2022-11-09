package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserPreferencesDTO;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    public final Tracker tracker;
    //    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final GpsUtilService gpsUtilService;
    private final TripPricer tripPricer = new TripPricer();
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();
    boolean testMode = true;
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    /**
     * TourGuideService, enable or note test mode and initializing users
     *
     * @param gpsUtilService the GpsUtil API
     * @param rewardsService the RewardsService API
     */
    public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this, gpsUtilService, rewardsService);
        addShutDownHook();
    }

    /**
     * Get the user rewards
     *
     * @param user the user
     * @return rewards of the user
     */
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    /**
     * Get location of user
     *
     * @param user the user to locate
     * @return the location of the user
     */
    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ?
                user.getLastVisitedLocation() :
                gpsUtilService.getUserLocation(user).join();
        return visitedLocation;
    }

    /**
     * Get user
     *
     * @param userName the user to get
     * @return the user
     */
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    /**
     * Get all users
     *
     * @return all users
     */
    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Add a user
     *
     * @param user to add
     */
    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    /**
     * Get the trip deals, with the username, duration of trip and numbers of adults & children
     *
     * @param user the user
     * @return the trip deals
     */
    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();

        List<Provider> providers = tripPricer.getPrice(
                tripPricerApiKey,
                user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(),
                cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    /**
     * Get user preferences
     *
     * @param username the user to get
     * @return the user preferences
     */
    public UserPreferences getUserPreferences(String username) {
        return getUser(username).getUserPreferences();
    }

    /**
     * Update user preferences
     *
     * @param userPreferencesDTO the user preferences to get
     * @return the user preferences
     */
    public UserPreferences updateUserPreferences(UserPreferencesDTO userPreferencesDTO) {
        User user = getUser(userPreferencesDTO.getUsername());
        user.setUserPreferences(new UserPreferences(userPreferencesDTO));

        return user.getUserPreferences();
    }

    /**
     * Track user location with thread and calculate rewards
     *
     * @param user to track
     * @return the visited location by user
     */
    public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
        return gpsUtilService.getUserLocation(user)
                .thenApply((visitedLocation -> {
                    user.addToVisitedLocations(visitedLocation);
                    rewardsService.calculateRewards(user);
                    return visitedLocation;
                }));
    }

    /**
     * Get the near attraction
     *
     * @param user the user
     * @return the near attractions
     */
    public List<Attraction> getNearByAttractions(User user) {
        List<Attraction> nearbyAttractions = gpsUtilService.getAttractions();
        VisitedLocation visitedLocation = getUserLocation(user);
        for (Attraction attraction : gpsUtilService.getAttractions()) {
            if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }

        return nearbyAttractions;
    }

    /**
     * Get five near attractions of user
     *
     * @param visitedLocation the visited location
     * @return five near attractions
     */
    public List<Attraction> getFiveAttractions(VisitedLocation visitedLocation) {
        List<Attraction> attractions = gpsUtilService.getAttractions();

        return attractions.stream()
                .sorted(Comparator.comparing(attraction -> rewardsService.getDistance(visitedLocation.location, attraction)))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Get all locations of all users
     *
     * @return all the locations
     */
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
        IntStream.range(0, 3).forEach(i -> {
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
