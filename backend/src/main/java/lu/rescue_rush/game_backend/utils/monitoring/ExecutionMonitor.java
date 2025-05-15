package lu.rescue_rush.game_backend.utils.monitoring;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import lu.pcy113.pclib.PCUtils;

import lu.rescue_rush.game_backend.R2ApiMain;
import lu.rescue_rush.game_backend.db.TableProxy;
import lu.rescue_rush.game_backend.db.data.monitor.execution.ExecutionMonitorData;

public class ExecutionMonitor {

	public static class MonitorData {
		private String key;
		// ns
		private long start, end;
		// ms
		private double duration;

		public MonitorData(String key, long start) {
			this.key = key;
			this.start = start;
		}
	}

	private static Logger LOGGER = Logger.getLogger(ExecutionMonitor.class.getName());

	private static Object dataLock = new Object();

	private static Timestamp sliceStart = Timestamp.from(Instant.now());
	private static Map<String, List<MonitorData>> datas = new HashMap<>();

	public static MonitorData start(String key) {
		return new MonitorData(key, System.nanoTime());
	}

	public static void end(MonitorData pd) {
		pd.end = System.nanoTime();
		pd.duration = PCUtils.round((double) (pd.end - pd.start) / 1_000_000, 3);

		if (R2ApiMain.DEBUG) {
			LOGGER.info(pd.key + ": " + pd.duration + " ms");
		}

		synchronized (dataLock) {
			datas.putIfAbsent(pd.key, new ArrayList<>());
			datas.get(pd.key).add(pd);
		}
	}

	private static ScheduledExecutorService scheduler;

	public static void startTracker() {
		final long start = System.nanoTime();

		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(ExecutionMonitor::periodicSave, 60, 60, TimeUnit.SECONDS);

		EventMonitor.push(EventMonitor.R2API_PERFTRACKER_START, Double.toString(PCUtils.round((double) (System.nanoTime() - start) / 1_000_000, 3)) + " ms");
	}

	public static void stopTracker() {
		final long start = System.nanoTime();

		LOGGER.info("Shutting down scheduler... ");
		scheduler.shutdown();
		try {
			if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				LOGGER.info("Forcing scheduler shutdown... ");
				scheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			scheduler.shutdownNow();
		}
		LOGGER.info(" OK");

		EventMonitor.push(EventMonitor.R2API_PERFTRACKER_STOP, Double.toString(PCUtils.round((double) (System.nanoTime() - start) / 1_000_000, 3)) + " ms");
	}

	public static void forceSave() {
		save_(EventMonitor.R2API_PERFTRACKER_FORCE_SAVE);
	}

	private static void periodicSave() {
		save_(EventMonitor.R2API_PERFTRACKER_SAVE);
	}

	private static void save_(String kk) {
		final long start = System.nanoTime();

		synchronized (dataLock) {
			for (Map.Entry<String, List<MonitorData>> entry : datas.entrySet()) {
				String key = entry.getKey();
				List<MonitorData> list = entry.getValue();

				final int entryCount = list.size();
				final double sum = (double) list.stream().mapToDouble(s -> s.duration).sum();
				final double avg = sum / entryCount;
				final double median = calculateMedian(list.stream().mapToDouble(s -> s.duration).boxed().toList());
				final double deviation = calculateStandardDeviation(list.stream().mapToDouble(s -> s.duration).sorted().boxed().toList());

				TableProxy.PERFS.insertAndReload(new ExecutionMonitorData(key, sliceStart, entryCount, sum, avg, median, deviation)).runAsync();
			}

			datas.clear();
			sliceStart = Timestamp.from(Instant.now());
		}

		EventMonitor.push(kk, Double.toString(PCUtils.round((double) (System.nanoTime() - start) / 1_000_000, 3)) + " ms");
	}

	public static double calculateMedian(List<Double> durations) {
		int size = durations.size();

		if (size % 2 == 0) {
			return (durations.get(size / 2 - 1) + durations.get(size / 2)) / 2.0;
		} else {
			return durations.get(size / 2);
		}
	}

	public static double calculateStandardDeviation(List<Double> durations) {
		int size = durations.size();
		if (size < 2) {
			return 0.0;
		}

		double mean = durations.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

		double variance = durations.stream().mapToDouble(duration -> Math.pow(duration - mean, 2)).sum() / size;

		return Math.sqrt(variance);
	}

	public static <T> T time(String key, Supplier<T> fun) {
		ExecutionMonitor.MonitorData pd = ExecutionMonitor.start(key);

		try {
			T obj = fun.get();

			ExecutionMonitor.end(pd);

			return obj;
		} catch (Exception e) {
			ExecutionMonitor.end(pd);
			throw e;
		}
	}

}
