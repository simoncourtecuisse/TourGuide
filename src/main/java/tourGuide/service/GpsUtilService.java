package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.user.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This Service manages gpsUtil microservice
 *
 * @author SimonC.
 */

public class GpsUtilService {
    private final GpsUtil gpsUtil;
    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    public GpsUtilService() {
        this.gpsUtil = new GpsUtil();
    }

    public List<Attraction> getAttractions() {
        return gpsUtil.getAttractions();
    }

    public CompletableFuture<VisitedLocation> getUserLocation(User user) {
        return CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executorService);
    }
}
