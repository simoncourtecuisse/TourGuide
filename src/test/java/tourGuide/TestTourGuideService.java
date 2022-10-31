package tourGuide;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserPreferencesDTO;
import tripPricer.Provider;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        User retrievedUser = tourGuideService.getUser(user.getUserName());
        User retrievedUser2 = tourGuideService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrievedUser);
        assertEquals(user2, retrievedUser2);
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

    @Test
    public void getNearbyAttractions() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.trackUserLocation(user).join();

        List<Attraction> attractionsList = tourGuideService.getFiveAttractions(user.getLastVisitedLocation());
        System.out.println(attractionsList);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractionsList.size());
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
        assertEquals(100, allCurrentLocations.size());
    }

    @Test
    public void getUpdateUserPreference() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        tourGuideService.addUser(user);

        UserPreferencesDTO oldPreferences = new UserPreferencesDTO(user.getUserName(), 1, 1, 1, 1, 1, 1, 1);
        user.setUserPreferences(new UserPreferences(oldPreferences));

        UserPreferences update = new UserPreferences();
        update.setAttractionProximity(2);
        update.setTripDuration(2);
        update.setTicketQuantity(2);
        update.setNumberOfAdults(2);
        update.setNumberOfChildren(2);
        UserPreferencesDTO updatedPreferences = new UserPreferencesDTO(user.getUserName(), update);

        tourGuideService.updateUserPreferences(updatedPreferences);

        assertEquals(user.getUserPreferences().getNumberOfChildren(), 2);
    }

}
