package com.dslplatform.json;

import com.dslplatform.json.runtime.Settings;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcurrentAnalysisTest {

	public static class BeanModel {
		public int a = 1;
		public String b = "x";
		public Blocker blocker = new Blocker();
	}

	public static class ImmutableModel {
		public final int a;
		public final String b;
		public final Blocker blocker;

		public ImmutableModel(int a, String b, Blocker blocker) {
			this.a = a;
			this.b = b;
			this.blocker = blocker;
		}
	}

	public static class Blocker {
	}

	private static final JsonWriter.WriteObject<Object> BLOCKER_WRITER = new JsonWriter.WriteObject<Object>() {
		@Override
		public void write(JsonWriter writer, Object value) {
			writer.writeNull();
		}
	};

	private static final JsonReader.ReadObject<Object> BLOCKER_READER = new JsonReader.ReadObject<Object>() {
		@Override
		public Object read(JsonReader reader) {
			return null;
		}
	};

	public static class SimpleBean1 {
		public int x = 42;
	}

	public static class SimpleBean2 {
		public String s = "hello";
		public int n = 7;
	}

	public static class SimpleBean3 {
		public double d = 1.5;
		public boolean flag = true;
	}

	public static class SimpleBean4 {
		public long l = 123456789L;
		public String s = "world";
	}

	public static class SimpleImmutable {
		public final int a;
		public final String b;

		public SimpleImmutable(int a, String b) {
			this.a = a;
			this.b = b;
		}
	}

	@Test
	public void objectAnalyzerLazyWaiterIsWokenImmediatelyOnResolution() throws Exception {
		checkLazyWaitLatency(BeanModel.class, new BeanModel());
	}

	@Test
	public void immutableAnalyzerLazyWaiterIsWokenImmediatelyOnResolution() throws Exception {
		checkLazyWaitLatency(ImmutableModel.class, new ImmutableModel(1, "x", new Blocker()));
	}

	private static void checkLazyWaitLatency(final Class<?> modelClass, final Object instance) throws Exception {
		final int iterations = 20;
		final long[] wakeNanos = new long[iterations];
		for (int i = 0; i < iterations; i++) {
			wakeNanos[i] = singleLazyWaitIteration(modelClass, instance);
		}
		final long[] sorted = wakeNanos.clone();
		Arrays.sort(sorted);
		//19th of 20 values: fails only when at least two iterations are slow
		final long p95 = sorted[iterations - 2];
		Assert.assertTrue(
				"p95 lazy-wake latency was " + TimeUnit.NANOSECONDS.toMillis(p95) + "ms, expected < 50ms",
				p95 < TimeUnit.MILLISECONDS.toNanos(50));
		Assert.assertTrue(
				"max lazy-wake latency was " + TimeUnit.NANOSECONDS.toMillis(sorted[iterations - 1]) + "ms, expected < 3s",
				sorted[iterations - 1] < TimeUnit.SECONDS.toNanos(3));
	}

	private static long singleLazyWaitIteration(final Class<?> modelClass, final Object instance) throws Exception {
		final DslJson<Object> json = new DslJson<>(Settings.withRuntime());
		final CountDownLatch releaseBlocker = new CountDownLatch(1);
		json.registerWriterFactory(new DslJson.ConverterFactory<JsonWriter.WriteObject>() {
			@Override
			public JsonWriter.WriteObject tryCreate(Type manifest, DslJson dslJson) {
				if (manifest.equals(Blocker.class)) {
					try {
						releaseBlocker.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return null;
					}
					return BLOCKER_WRITER;
				}
				return null;
			}
		});
		json.registerReaderFactory(new DslJson.ConverterFactory<JsonReader.ReadObject>() {
			@Override
			public JsonReader.ReadObject tryCreate(Type manifest, DslJson dslJson) {
				return Blocker.class.equals(manifest) ? BLOCKER_READER : null;
			}
		});

		final Thread analyzer = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					json.serialize(instance, new ByteArrayOutputStream());
				} catch (Exception ignore) {
				}
			}
		}, "lazy-wait-analyzer");
		analyzer.start();

		final JsonWriter.WriteObject lazy = waitForLazyPlaceholder(json, modelClass);

		Thread.sleep(80 + ThreadLocalRandom.current().nextLong(300));
		releaseBlocker.countDown();
		final long releasedAt = System.nanoTime();

		final JsonWriter writer = new JsonWriter(1024, null);
		lazy.write(writer, instance);
		final long wakeLatencyNanos = System.nanoTime() - releasedAt;

		final String output = new String(writer.toByteArray(), StandardCharsets.UTF_8);
		Assert.assertTrue("unexpected serialization: " + output, output.contains("\"a\":1") && output.contains("\"b\":\"x\""));

		analyzer.join(5000);
		Assert.assertFalse("analysis did not finish", analyzer.isAlive());
		return wakeLatencyNanos;
	}

	@SuppressWarnings("unchecked")
	private static JsonWriter.WriteObject waitForLazyPlaceholder(final DslJson<?> json, final Class<?> modelClass) throws Exception {
		final Field writersField = DslJson.class.getDeclaredField("writers");
		writersField.setAccessible(true);
		final Map<Type, JsonWriter.WriteObject> writers = (Map<Type, JsonWriter.WriteObject>) writersField.get(json);
		for (int i = 0; i < 5000; i++) {
			final JsonWriter.WriteObject writer = writers.get(modelClass);
			if (writer != null && writer.getClass().getName().contains("Lazy")) {
				return writer;
			}
			Thread.sleep(1);
		}
		throw new IllegalStateException("lazy placeholder for " + modelClass.getName() + " was never registered");
	}

	public static class FlakyModel {
		public int a = 1;
		public String b = "x";
		public Flaky flaky = new Flaky();
	}

	public static class Flaky {
	}

	private static final JsonWriter.WriteObject<Object> FLAKY_WRITER = new JsonWriter.WriteObject<Object>() {
		@Override
		public void write(JsonWriter writer, Object value) {
			writer.writeNull();
		}
	};

	private static final JsonReader.ReadObject<Object> FLAKY_READER = new JsonReader.ReadObject<Object>() {
		@Override
		public Object read(JsonReader reader) {
			return null;
		}
	};

	@Test
	public void failedAnalysisIsUnregisteredAndRetried() throws Exception {
		final DslJson<Object> json = new DslJson<>(Settings.withRuntime());
		final AtomicBoolean failedOnce = new AtomicBoolean(false);
		json.registerWriterFactory(new DslJson.ConverterFactory<JsonWriter.WriteObject>() {
			@Override
			public JsonWriter.WriteObject tryCreate(Type manifest, DslJson dslJson) {
				if (manifest.equals(Flaky.class)) {
					if (!failedOnce.getAndSet(true)) {
						throw new ConfigurationException("simulated analysis failure");
					}
					return FLAKY_WRITER;
				}
				return null;
			}
		});
		json.registerReaderFactory(new DslJson.ConverterFactory<JsonReader.ReadObject>() {
			@Override
			public JsonReader.ReadObject tryCreate(Type manifest, DslJson dslJson) {
				return Flaky.class.equals(manifest) ? FLAKY_READER : null;
			}
		});

		final Object instance = new FlakyModel();
		try {
			json.serialize(instance, new ByteArrayOutputStream());
			Assert.fail("first serialization was expected to fail");
		} catch (ConfigurationException expected) {
		}

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		json.serialize(instance, out);
		final String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
		Assert.assertTrue("unexpected serialization: " + output, output.contains("\"a\":1") && output.contains("\"b\":\"x\""));
	}

	@Test
	public void concurrentFirstUseSucceedsUnderContention() throws Exception {
		final DslJson<Object> json = new DslJson<>(Settings.withRuntime());
		final List<Object> instances = Arrays.asList(
				new SimpleBean1(),
				new SimpleBean2(),
				new SimpleBean3(),
				new SimpleBean4(),
				new SimpleImmutable(5, "value"));
		final int threads = 8;
		final CyclicBarrier barrier = new CyclicBarrier(threads);
		final ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			final List<Future<?>> futures = new ArrayList<Future<?>>();
			for (int t = 0; t < threads; t++) {
				futures.add(pool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							barrier.await();
							for (Object instance : instances) {
								final ByteArrayOutputStream out = new ByteArrayOutputStream();
								json.serialize(instance, out);
								final byte[] bytes = out.toByteArray();
								final Object roundTripped = json.deserialize(instance.getClass(), bytes, bytes.length);
								final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
								json.serialize(roundTripped, out2);
								Assert.assertArrayEquals(bytes, out2.toByteArray());
							}
						} catch (Exception ex) {
							throw new RuntimeException(ex);
						}
					}
				}));
			}
			for (Future<?> future : futures) {
				future.get(30, TimeUnit.SECONDS);
			}
		} finally {
			pool.shutdownNow();
		}
	}
}
