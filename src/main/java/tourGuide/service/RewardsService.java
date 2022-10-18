package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    private final GpsUtilService gpsUtilService;
    private final RewardCentral rewardsCentral;
    // proximity in miles
    private int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    public RewardsService(GpsUtilService gpsUtilService, RewardCentral rewardCentral) {
        this.gpsUtilService = gpsUtilService;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

//								***** throws ConcurrentModificationException *****
//	public void calculateRewards(User user) {
//		List<VisitedLocation> userLocations = user.getVisitedLocations();
//		List<Attraction> attractions = gpsUtil.getAttractions();
//
//		for(VisitedLocation visitedLocation : userLocations) {
//			for(Attraction attraction : attractions) {
//				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
//					if(nearAttraction(visitedLocation, attraction)) {
//						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
//					}
//				}
//			}
//		}
//	}

//    public void calculateRewards(User user) {
//        List<VisitedLocation> userLocations = user.getVisitedLocations();
//        List<Attraction> attractions = gpsUtil.getAttractions();
//
//        userLocations.stream().forEach(visitedLocation -> {
//            attractions.stream().forEach(attraction -> {
//                if (user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
//                    if (nearAttraction(visitedLocation, attraction)) {
//                        UserReward addedReward = new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user));
//                        user.addUserReward(addedReward);
//                    }
//                }
//            });
//        });
//    }
//public List<UserReward> calculateRewards(User user) {
//    List<VisitedLocation> userLocations = user.getVisitedLocations();
//    List<Attraction> allAttractions = gpsUtilService.getAttractions();
//    List<VisitedLocation> locations = new ArrayList<>(userLocations);
//    List<UserReward> rewards = new ArrayList<>();
//
//    for(Attraction attraction : allAttractions) {
//        for(VisitedLocation userLocation : locations) {
//            if(nearAttraction(userLocation, attraction)) {
//
//                //Si user n'a pas encore eu la recompense :
//                if (user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
//                    UserReward userReward = new UserReward(userLocation, attraction, 0);
//                    user.addUserReward(userReward);
//                    rewards.add(userReward);
//                }
//            }
//        }
//    }
//
//    rewards.stream().map(reward -> CompletableFuture.supplyAsync(() -> getRewardPoints(reward.attraction, user), executorService)
//            .thenAccept((points) -> user.addUserReward(new UserReward(user.getLastVisitedLocation(), reward.attraction, points))));
//    return user.getUserRewards();
//}

    public CompletableFuture<?> calculateRewards(User user) {
        List<Attraction> allAttractions = gpsUtilService.getAttractions();
        List<CompletableFuture<?>> futureList = new ArrayList<>();
        futureList.add(CompletableFuture.runAsync(() ->
                user.getVisitedLocations().forEach(u1 -> {
                    allAttractions.stream()
                            .filter(a -> nearAttraction(u1, a))
                            .forEach(a -> {
                                System.out.println(u1);
                                if (user.getUserRewards().stream().noneMatch(uR -> uR.attraction.attractionName.equals(a.attractionName))) {
                                    //System.out.println(u1);
                                    int points = getRewardPoints(a, user);
                                    System.out.println(points);
                                    user.addUserReward(new UserReward(u1, a , points));
                                }
                            });
                })));
        return CompletableFuture.allOf(futureList.toArray(CompletableFuture[]::new));
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) > attractionProximityRange ? false : true;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
    }

    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }

}
