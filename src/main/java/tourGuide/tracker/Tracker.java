package tourGuide.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tourGuide.service.GpsUtilService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Tracker extends Thread {
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TourGuideService tourGuideService;
    private final GpsUtilService gpsUtilService;
    private final RewardsService rewardsService;
    private Logger logger = LoggerFactory.getLogger(Tracker.class);
    private boolean stop = false;

    public Tracker(TourGuideService tourGuideService, GpsUtilService gpsUtilService, RewardsService rewardsService) {
        this.tourGuideService = tourGuideService;
        this.gpsUtilService = gpsUtilService;
        this.rewardsService = rewardsService;

//        executorService.submit(this);
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }

    //    @Override
//    public void run() {
//        StopWatch stopWatch = new StopWatch();
//        while (true) {
//            if (Thread.currentThread().isInterrupted() || stop) {
//                logger.debug("Tracker stopping");
//                break;
//            }
//
//            stopWatch.start();
//            CompletableFuture<?>[] completableFuture = tourGuideService.getAllUsers().stream()
//					.map(this::trackUserLocation)
//                    .toArray(CompletableFuture[]::new);
//            CompletableFuture.allOf(completableFuture).join();
//
//
//            stopWatch.stop();
//            logger.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
//            stopWatch.reset();
//            try {
//                logger.debug("Tracker sleeping");
//                TimeUnit.SECONDS.sleep(trackingPollingInterval);
//            } catch (InterruptedException e) {
//                break;
//            }
//        }
    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                logger.debug("Tracker stopping");
                break;
            }

            CompletableFuture<?>[] futures = tourGuideService.getAllUsers().stream()
                    .map(this::trackUserLocation)
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join(); // waiting so that Tracker does not keep asking CompletableFutures

            try {
                logger.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch (InterruptedException e) {

                break;
            }
        }

    }


    public CompletableFuture<?> trackUserLocation(User user) {
        return CompletableFuture.supplyAsync(() -> gpsUtilService.getUserLocation(user))
                .thenAccept(user::addToVisitedLocations)
                .thenRunAsync(() -> rewardsService.calculateRewards(user));
    }
}
