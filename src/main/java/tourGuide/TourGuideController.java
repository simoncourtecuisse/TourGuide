package tourGuide;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferencesDTO;
import tourGuide.user.UserReward;
import tripPricer.Provider;

import java.util.List;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @Autowired
    RewardsService rewardsService;

    /**
     * This endpoint is the front page of the api.
     *
     * @return a welcoming message.
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * This endpoint is used to track one user location
     *
     * @param userName username of the tracked user
     * @return a string containing the last longitude and latitude of a user.
     */
    @RequestMapping("/getLocation")
    public Location getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return visitedLocation.location;
    }

    /**
     * This endpoint find the nearby attraction for a user
     *
     * @param userName username of the tracked user
     * @return JSON object that contains: Name of Tourist attraction, longitude and latitude of each of these attractions,
     * the longitude and latitude of the user, the distance in miles between the user and the attraction,
     * and eventually the reward points given
     * to the user if all these attractions are visited.
     */
    @RequestMapping("/getNearbyAttractions")
    public List<Attraction> getNearByAttractions(@RequestParam String userName) {
        return tourGuideService.getNearByAttractions(getUser(userName));
    }

    //  TODO: Change this method to no longer return a List of Attractions.
    //  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
    //  Return a new JSON object that contains:
    // Name of Tourist attraction,
    // Tourist attractions lat/long,
    // The user's location lat/long,
    // The distance in miles between the user's location and each of the attractions.
    // The reward points for visiting each Attraction.
    //    Note: Attraction reward points can be gathered from RewardsCentral
    /**
     * This endpoint find the five closest attraction for a user
     *
     * @param userName username of the tracked user
     * @return JSON object that contains: Name of Tourist attraction, longitude and latitude of each of these attractions,
     * the longitude and latitude of the user,
     * the distance in miles between the user and the attraction, and eventually the reward points given
     * to the user if all these attractions are visited.
     */
    @RequestMapping("/getFiveAttractions")
    public List<Attraction> getFiveAttractions(@RequestParam String userName) {
        return tourGuideService.getFiveAttractions(getUser(userName).getLastVisitedLocation());
    }

    /**
     * This endpoint calculates reward for a specific user.
     *
     * @param userName is the username of the user you want to calculate the reward points
     * @return an integer corresponding of the reward points owned by a user.
     */
    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

    /**
     * This endpoint returns all last visited location for every user in DB.
     *
     * @return a JSON list of all user's last location.
     */
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        // TODO: Get a list of every user's most recent location as JSON
        //- Note: does not use gpsUtil to query for their current location,
        //        but rather gathers the user's current location from their stored location history.
        //
        // Return object should be the just a JSON mapping of userId to Locations similar to:
        //     {
        //        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
        //        ...
        //     }
        System.out.println(JsonStream.serialize(tourGuideService.getAllCurrentLocations()));
        return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    /**
     * This endpoint is used to fetch trip deals from a user, thanks to its reward points and its parameters.
     *
     * @param userName is the username of the user you want to create a trip deal.
     * @return a list of providers with their name, the price they give you their service, and the uuid of the trip deal.
     */
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return providers;
    }

    /**
     * This endpoint is used to update user preferences.
     *
     * @param newPreferences is the body of the user preferences to update.
     * @return the new user preferences.
     */
    @PutMapping("/userPreferences")
    public String userPreferences(@RequestBody UserPreferencesDTO newPreferences) {
        return JsonStream.serialize(
                new UserPreferencesDTO(newPreferences.getUsername(), tourGuideService.updateUserPreferences(newPreferences)));
    }

    /**
     * This endpoint is used to a specific user.
     *
     * @param userName is the username of the user you want to get.
     * @return the user's info.
     */
    @GetMapping("/getUser")
    private User getUser(@RequestParam String userName) {
        return tourGuideService.getUser(userName);
    }


}