package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import tourGuide.user.User;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author SimonC.
 */

//@Service
public class GpsUtilService {
    private final GpsUtil gpsUtil;

    public GpsUtilService() {
        this.gpsUtil = new GpsUtil();
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }

    public CompletableFuture<VisitedLocation> getUserLocation(User user) {
        return CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executorService);
    }
}
