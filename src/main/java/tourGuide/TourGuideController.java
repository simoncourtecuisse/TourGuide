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

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public Location getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return visitedLocation.location;
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
    @RequestMapping("/getNearbyAttractions")
    public List<Attraction> getNearbyAttractions(@RequestParam String userName) {
        return tourGuideService.getNearByAttractions(getUser(userName));
    }

    @RequestMapping("/getFiveAttractions")
    public List<Attraction> getFiveAttractions(@RequestParam String userName) {
        return tourGuideService.getFiveAttractions(getUser(userName).getLastVisitedLocation());
    }

    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

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

    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return providers;
    }

    @PutMapping("/userPreferences")
    public String userPreferences(@RequestBody UserPreferencesDTO newPreferences) {
        return JsonStream.serialize(
                new UserPreferencesDTO(newPreferences.getUsername(), tourGuideService.updateUserPreferences(newPreferences)));
    }

    @GetMapping("/getUser")
    private User getUser(@RequestParam String userName) {
        return tourGuideService.getUser(userName);
    }


}