package tourGuide;

import java.util.List;

import gpsUtil.location.Attraction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.model.UserNearByAttractions;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserPreferencesDTO;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return JsonStream.serialize(visitedLocation.location);
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
//    @RequestMapping("/getNearbyAttractions")
//    public String getNearbyAttractions(@RequestParam String userName) {
//    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
//    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
//    }
    @RequestMapping("/get5Attractions")
    public List<Attraction> get5Attractions(@RequestParam String userName){
        return tourGuideService.get5Attractions(getUser(userName));
    }

    @RequestMapping("/get10Attractions")
    public List<Attraction> get10Attractions(@RequestParam String userName){
        return tourGuideService.get10Attractions(getUser(userName));
    }

    @RequestMapping("/getNearbyAttractions")
    public UserNearByAttractions getNearbyAttractions(@RequestParam String userName){
        return tourGuideService.getNearFiveAttractions(getUser(userName));
    }

    @RequestMapping("/getTenNearbyAttractions")
    public UserNearByAttractions getTenNearbyAttractions(@RequestParam String userName){
        return tourGuideService.getNearTenAttractions(getUser(userName));
    }

    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) {
        return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
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
    public String getTripDeals(@RequestParam String userName) {
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
        return JsonStream.serialize(providers);
    }

    @PutMapping("/userPreferences")
    public String userPreferences(@RequestBody UserPreferencesDTO newPreferences) {
        return JsonStream.serialize(
                new UserPreferencesDTO(newPreferences.getUsername(), tourGuideService.updateUserPreferences(newPreferences)));
    }

    private User getUser(String userName) {
        return tourGuideService.getUser(userName);
    }


}