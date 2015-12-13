package com.dslplatform.json.generated.ocd.javaasserts;

import org.junit.Assert;

public class DecimalWithScaleOf9Asserts {
	static void assertSingleEquals(final String message, final java.math.BigDecimal expected, final java.math.BigDecimal actual) {
		try {
			expected.setScale(9);
		}
		catch (final ArithmeticException e) {
			Assert.fail(message + "expected was a DecimalWithScaleOf9, but its scale was " + expected.scale() + " - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}

		try {
			expected.setScale(9);
		}
		catch (final ArithmeticException e) {
			Assert.fail(message + "actual was a DecimalWithScaleOf9, but its scale was " + actual.scale());
		}

		if (expected == actual || expected.compareTo(actual) == 0) return;
		Assert.fail(message + "expected was \"" + expected + "\", but actual was \"" + actual + "\"");
	}

	static void assertOneEquals(final String message, final java.math.BigDecimal expected, final java.math.BigDecimal actual) {
		if (expected == null) Assert.fail(message + "expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		if (expected == actual) return;
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertOneEquals(final java.math.BigDecimal expected, final java.math.BigDecimal actual) {
		assertOneEquals("OneDecimalWithScaleOf9 mismatch: ", expected, actual);
	}

	private static void assertNullableEquals(final String message, final java.math.BigDecimal expected, final java.math.BigDecimal actual) {
		if (expected == actual) return;
		if (expected == null) Assert.fail(message + "expected was <null>, but actual was \"" + actual + "\"");
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertNullableEquals(final java.math.BigDecimal expected, final java.math.BigDecimal actual) {
		assertNullableEquals("NullableDecimalWithScaleOf9 mismatch: ", expected, actual);
	}

	private static void assertArrayOfOneEquals(final String message, final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfOneEquals(final String message, final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		for (int i = 0; i < expecteds.length; i ++) {
			if (expecteds[i] == null) {
				Assert.fail(message + "expecteds contained a <null> element at index " + i + " - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
			}
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneArrayOfOneEquals(final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		assertOneArrayOfOneEquals("OneArrayOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfOneEquals(final String message, final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfOneEquals(final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		assertNullableArrayOfOneEquals("NullableArrayOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertArrayOfNullableEquals(final String message, final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfNullableEquals(final String message, final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneArrayOfNullableEquals(final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		assertOneArrayOfNullableEquals("OneArrayOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfNullableEquals(final String message, final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfNullableEquals(final java.math.BigDecimal[] expecteds, final java.math.BigDecimal[] actuals) {
		assertNullableArrayOfNullableEquals("NullableArrayOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertListOfOneEquals(final String message, final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfOneEquals(final String message, final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		int i = 0;
		for (final java.math.BigDecimal expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfOneEquals(final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		assertOneListOfOneEquals("OneListOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfOneEquals(final String message, final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		assertNullableListOfOneEquals("NullableListOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertListOfNullableEquals(final String message, final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfNullableEquals(final String message, final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		assertOneListOfNullableEquals("OneListOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfNullableEquals(final String message, final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<java.math.BigDecimal> expecteds, final java.util.List<java.math.BigDecimal> actuals) {
		assertNullableListOfNullableEquals("NullableListOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfOneEquals(final String message, final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		if (actuals.contains(null)) {
			Assert.fail(message + "actuals contained a <null> element");
		}

		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final java.math.BigDecimal expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final java.math.BigDecimal actual : actuals) {
				try {
					assertOneEquals(expected, actual);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfOneEquals(final String message, final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		if (expecteds.contains(null)) {
			Assert.fail(message + "expecteds contained a <null> element - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		assertOneSetOfOneEquals("OneSetOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfOneEquals(final String message, final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		assertNullableSetOfOneEquals("NullableSetOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfNullableEquals(final String message, final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final java.math.BigDecimal expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final java.math.BigDecimal actual : actuals) {
				try {
					assertNullableEquals(expected, actual);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfNullableEquals(final String message, final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		assertOneSetOfNullableEquals("OneSetOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfNullableEquals(final String message, final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<java.math.BigDecimal> expecteds, final java.util.Set<java.math.BigDecimal> actuals) {
		assertNullableSetOfNullableEquals("NullableSetOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfOneEquals(final String message, final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfOneEquals(final String message, final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		int i = 0;
		for (final java.math.BigDecimal expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		assertOneQueueOfOneEquals("OneQueueOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfOneEquals(final String message, final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		assertNullableQueueOfOneEquals("NullableQueueOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfNullableEquals(final String message, final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfNullableEquals(final String message, final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		assertOneQueueOfNullableEquals("OneQueueOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfNullableEquals(final String message, final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<java.math.BigDecimal> expecteds, final java.util.Queue<java.math.BigDecimal> actuals) {
		assertNullableQueueOfNullableEquals("NullableQueueOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		int i = 0;
		for (final java.math.BigDecimal expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		assertOneLinkedListOfOneEquals("OneLinkedListOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		assertNullableLinkedListOfOneEquals("NullableLinkedListOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		assertOneLinkedListOfNullableEquals("OneLinkedListOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<java.math.BigDecimal> expecteds, final java.util.LinkedList<java.math.BigDecimal> actuals) {
		assertNullableLinkedListOfNullableEquals("NullableLinkedListOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfOneEquals(final String message, final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfOneEquals(final String message, final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		int i = 0;
		for (final java.math.BigDecimal expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		assertOneStackOfOneEquals("OneStackOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfOneEquals(final String message, final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		assertNullableStackOfOneEquals("NullableStackOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfNullableEquals(final String message, final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfNullableEquals(final String message, final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		assertOneStackOfNullableEquals("OneStackOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfNullableEquals(final String message, final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<java.math.BigDecimal> expecteds, final java.util.Stack<java.math.BigDecimal> actuals) {
		assertNullableStackOfNullableEquals("NullableStackOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfOneEquals(final String message, final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfOneEquals(final String message, final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		int i = 0;
		for (final java.math.BigDecimal expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		assertOneVectorOfOneEquals("OneVectorOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfOneEquals(final String message, final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		assertNullableVectorOfOneEquals("NullableVectorOfOneDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfNullableEquals(final String message, final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<java.math.BigDecimal> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.math.BigDecimal> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.math.BigDecimal expected = expectedsIterator.next();
			final java.math.BigDecimal actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfNullableEquals(final String message, final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		assertOneVectorOfNullableEquals("OneVectorOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfNullableEquals(final String message, final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<java.math.BigDecimal> expecteds, final java.util.Vector<java.math.BigDecimal> actuals) {
		assertNullableVectorOfNullableEquals("NullableVectorOfNullableDecimalWithScaleOf9 mismatch: ", expecteds, actuals);
	}
}
