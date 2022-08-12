package tourGuide.model;

import gpsUtil.location.Location;

import java.util.List;

/**
 * @author SimonC.
 */
public class UserNearByAttractions {

    private Location userLocation;
    private List<NearByAttraction> nearByAttractions;

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public List<NearByAttraction> getNearByAttractions() {
        return nearByAttractions;
    }

    public void setNearByAttractions(List<NearByAttraction> nearByAttractions) {
        this.nearByAttractions = nearByAttractions;
    }

    @Override
    public String toString() {
        return "UserNearByAttractions{" +
                "userLocation=" + userLocation +
                ", nearByAttractions=" + nearByAttractions +
                '}';
    }
}
