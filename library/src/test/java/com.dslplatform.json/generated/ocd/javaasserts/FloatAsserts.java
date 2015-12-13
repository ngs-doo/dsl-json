package com.dslplatform.json.generated.ocd.javaasserts;

import org.junit.Assert;

public class FloatAsserts {
	static void assertSingleEquals(final String message, final float expected, final float actual, final int ulps) {
		if (Float.floatToRawIntBits(expected) == Float.floatToRawIntBits(actual)) return;

		if (ulps == 0) {
			Assert.fail(message + "expected was \"" + expected + "\", but actual was \"" + actual + "\" - WARNING: You are comparing the bits of float values - not using an epsilon value!");
		}

		final float epsilon = Math.ulp(expected) * Math.abs(ulps);
		if (expected >= actual - epsilon && expected <= actual + epsilon) return;
		Assert.fail(message + "expected was \"" + expected + "\", but actual was \"" + actual + "\" (using epsilon value of \"" + epsilon + "\")");
	}

	static void assertOneEquals(final String message, final float expected, final float actual, final int ulps) {
		assertSingleEquals(message, expected, actual, ulps);
	}

	public static void assertOneEquals(final float expected, final float actual, final int ulps) {
		assertOneEquals("OneFloat mismatch: ", expected, actual, ulps);
	}

	public static void assertOneEquals(final float expected, final float actual) {
		assertOneEquals(expected, actual, 0);
	}

	private static void assertNullableEquals(final String message, final Float expected, final Float actual, final int ulps) {
		if (expected == actual) return;
		if (expected == null) Assert.fail(message + "expected was <null>, but actual was \"" + actual + "\"");
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual, ulps);
	}

	public static void assertNullableEquals(final Float expected, final Float actual, final int ulps) {
		assertNullableEquals("NullableFloat mismatch: ", expected, actual, ulps);
	}

	public static void assertNullableEquals(final Float expected, final Float actual) {
		assertNullableEquals(expected, actual, 0);
	}

	private static void assertArrayOfOneEquals(final String message, final float[] expecteds, final float[] actuals, final int ulps) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i], ulps);
		}
	}

	private static void assertOneArrayOfOneEquals(final String message, final float[] expecteds, final float[] actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneArrayOfOneEquals(final float[] expecteds, final float[] actuals, final int ulps) {
		assertOneArrayOfOneEquals("OneArrayOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneArrayOfOneEquals(final float[] expecteds, final float[] actuals) {
		assertOneArrayOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertNullableArrayOfOneEquals(final String message, final float[] expecteds, final float[] actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableArrayOfOneEquals(final float[] expecteds, final float[] actuals, final int ulps) {
		assertNullableArrayOfOneEquals("NullableArrayOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableArrayOfOneEquals(final float[] expecteds, final float[] actuals) {
		assertNullableArrayOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertArrayOfNullableEquals(final String message, final Float[] expecteds, final Float[] actuals, final int ulps) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i], ulps);
		}
	}

	private static void assertOneArrayOfNullableEquals(final String message, final Float[] expecteds, final Float[] actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneArrayOfNullableEquals(final Float[] expecteds, final Float[] actuals, final int ulps) {
		assertOneArrayOfNullableEquals("OneArrayOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneArrayOfNullableEquals(final Float[] expecteds, final Float[] actuals) {
		assertOneArrayOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertNullableArrayOfNullableEquals(final String message, final Float[] expecteds, final Float[] actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableArrayOfNullableEquals(final Float[] expecteds, final Float[] actuals, final int ulps) {
		assertNullableArrayOfNullableEquals("NullableArrayOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableArrayOfNullableEquals(final Float[] expecteds, final Float[] actuals) {
		assertNullableArrayOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertListOfOneEquals(final String message, final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			if (actual == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was \"" + expected + "\", but actual was <null>");
			}
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected.floatValue(), actual.floatValue(), ulps);
		}
	}

	private static void assertOneListOfOneEquals(final String message, final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		int i = 0;
		for (final Float expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneListOfOneEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		assertOneListOfOneEquals("OneListOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneListOfOneEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals) {
		assertOneListOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertNullableListOfOneEquals(final String message, final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		assertNullableListOfOneEquals("NullableListOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals) {
		assertNullableListOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertListOfNullableEquals(final String message, final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, ulps);
		}
	}

	private static void assertOneListOfNullableEquals(final String message, final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		assertOneListOfNullableEquals("OneListOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals) {
		assertOneListOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertNullableListOfNullableEquals(final String message, final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals, final int ulps) {
		assertNullableListOfNullableEquals("NullableListOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<Float> expecteds, final java.util.List<Float> actuals) {
		assertNullableListOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertSetOfOneEquals(final String message, final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		if (actuals.contains(null)) {
			Assert.fail(message + "actuals contained a <null> element");
		}

		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final Float expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final Float actual : actuals) {
				try {
					assertOneEquals(expected.floatValue(), actual.floatValue(), ulps);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfOneEquals(final String message, final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		if (expecteds.contains(null)) {
			Assert.fail(message + "expecteds contained a <null> element - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		assertOneSetOfOneEquals("OneSetOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals) {
		assertOneSetOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertNullableSetOfOneEquals(final String message, final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		assertNullableSetOfOneEquals("NullableSetOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals) {
		assertNullableSetOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertSetOfNullableEquals(final String message, final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final Float expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final Float actual : actuals) {
				try {
					assertNullableEquals(expected, actual, ulps);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfNullableEquals(final String message, final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		assertOneSetOfNullableEquals("OneSetOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals) {
		assertOneSetOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertNullableSetOfNullableEquals(final String message, final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals, final int ulps) {
		assertNullableSetOfNullableEquals("NullableSetOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<Float> expecteds, final java.util.Set<Float> actuals) {
		assertNullableSetOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertQueueOfOneEquals(final String message, final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			if (actual == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was \"" + expected + "\", but actual was <null>");
			}
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected.floatValue(), actual.floatValue(), ulps);
		}
	}

	private static void assertOneQueueOfOneEquals(final String message, final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		int i = 0;
		for (final Float expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		assertOneQueueOfOneEquals("OneQueueOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals) {
		assertOneQueueOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertNullableQueueOfOneEquals(final String message, final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		assertNullableQueueOfOneEquals("NullableQueueOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals) {
		assertNullableQueueOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertQueueOfNullableEquals(final String message, final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, ulps);
		}
	}

	private static void assertOneQueueOfNullableEquals(final String message, final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		assertOneQueueOfNullableEquals("OneQueueOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals) {
		assertOneQueueOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertNullableQueueOfNullableEquals(final String message, final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals, final int ulps) {
		assertNullableQueueOfNullableEquals("NullableQueueOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<Float> expecteds, final java.util.Queue<Float> actuals) {
		assertNullableQueueOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertLinkedListOfOneEquals(final String message, final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			if (actual == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was \"" + expected + "\", but actual was <null>");
			}
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected.floatValue(), actual.floatValue(), ulps);
		}
	}

	private static void assertOneLinkedListOfOneEquals(final String message, final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		int i = 0;
		for (final Float expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		assertOneLinkedListOfOneEquals("OneLinkedListOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals) {
		assertOneLinkedListOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertNullableLinkedListOfOneEquals(final String message, final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		assertNullableLinkedListOfOneEquals("NullableLinkedListOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals) {
		assertNullableLinkedListOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertLinkedListOfNullableEquals(final String message, final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, ulps);
		}
	}

	private static void assertOneLinkedListOfNullableEquals(final String message, final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		assertOneLinkedListOfNullableEquals("OneLinkedListOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals) {
		assertOneLinkedListOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertNullableLinkedListOfNullableEquals(final String message, final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals, final int ulps) {
		assertNullableLinkedListOfNullableEquals("NullableLinkedListOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<Float> expecteds, final java.util.LinkedList<Float> actuals) {
		assertNullableLinkedListOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertStackOfOneEquals(final String message, final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			if (actual == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was \"" + expected + "\", but actual was <null>");
			}
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected.floatValue(), actual.floatValue(), ulps);
		}
	}

	private static void assertOneStackOfOneEquals(final String message, final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		int i = 0;
		for (final Float expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		assertOneStackOfOneEquals("OneStackOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals) {
		assertOneStackOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertNullableStackOfOneEquals(final String message, final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		assertNullableStackOfOneEquals("NullableStackOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals) {
		assertNullableStackOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertStackOfNullableEquals(final String message, final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, ulps);
		}
	}

	private static void assertOneStackOfNullableEquals(final String message, final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		assertOneStackOfNullableEquals("OneStackOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals) {
		assertOneStackOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertNullableStackOfNullableEquals(final String message, final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals, final int ulps) {
		assertNullableStackOfNullableEquals("NullableStackOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<Float> expecteds, final java.util.Stack<Float> actuals) {
		assertNullableStackOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertVectorOfOneEquals(final String message, final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			if (actual == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was \"" + expected + "\", but actual was <null>");
			}
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected.floatValue(), actual.floatValue(), ulps);
		}
	}

	private static void assertOneVectorOfOneEquals(final String message, final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		int i = 0;
		for (final Float expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		assertOneVectorOfOneEquals("OneVectorOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals) {
		assertOneVectorOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertNullableVectorOfOneEquals(final String message, final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		assertNullableVectorOfOneEquals("NullableVectorOfOneFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals) {
		assertNullableVectorOfOneEquals(expecteds, actuals, 0);
	}

	private static void assertVectorOfNullableEquals(final String message, final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<Float> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<Float> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final Float expected = expectedsIterator.next();
			final Float actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, ulps);
		}
	}

	private static void assertOneVectorOfNullableEquals(final String message, final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		assertOneVectorOfNullableEquals("OneVectorOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals) {
		assertOneVectorOfNullableEquals(expecteds, actuals, 0);
	}

	private static void assertNullableVectorOfNullableEquals(final String message, final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals, ulps);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals, final int ulps) {
		assertNullableVectorOfNullableEquals("NullableVectorOfNullableFloat mismatch: ", expecteds, actuals, ulps);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<Float> expecteds, final java.util.Vector<Float> actuals) {
		assertNullableVectorOfNullableEquals(expecteds, actuals, 0);
	}
}
