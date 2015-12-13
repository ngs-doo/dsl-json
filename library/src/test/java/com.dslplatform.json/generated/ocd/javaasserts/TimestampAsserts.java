package com.dslplatform.json.generated.ocd.javaasserts;

import org.junit.Assert;

public class TimestampAsserts {
	static void assertSingleEquals(final String message, final org.joda.time.DateTime expected, final org.joda.time.DateTime actual, final org.joda.time.Duration delta) {
		// TODO: Chronologies will not be compared if milliseconds were an exact match
		final boolean EXACT_MILLIS_GOOD_ENOUGH = true;

		if (delta == org.joda.time.Duration.ZERO) {
			if (expected == actual || expected.equals(actual)) return;

			final StringBuilder failMsg = new StringBuilder(message)
					.append("expected was: \"")
					.append(expected)
					.append("\", but actual was: \"")
					.append(actual)
					.append("\" [WARNING - not using a delta duration, comparing two instants directly]");

			if (expected != null && actual != null) {
				if (expected.getMillis() == actual.getMillis()) {
					if (EXACT_MILLIS_GOOD_ENOUGH) return;

					if (org.joda.time.field.FieldUtils.equals(expected.getChronology(), actual.getChronology())) return;
					failMsg.append("; Chronologies: ")
							.append(expected.getChronology()).append(" vs. ")
							.append(actual.getChronology());
				} else {
					failMsg.append("; Millis: ").append(expected.getMillis())
							.append(" vs. ").append(actual.getMillis());
				}
			}

			if (expected.getChronology() != null && actual.getChronology() != null) {
				if (expected.getChronology().getZone().getOffset(0) == actual.getChronology().getZone().getOffset(0)) return;
				failMsg.append("; Chronology offsets: ")
						.append(expected.getChronology().getZone().getOffset(0))
						.append(" vs. ")
						.append(actual.getChronology().getZone().getOffset(0));
			} else {
				failMsg.append("; One of the chronologies is null. ");
			}

			Assert.fail(failMsg.toString());
		}

		if (expected.isBefore(actual) && expected.plus(delta).isAfter(actual)) return;
		Assert.fail(message + "expected was \"" + expected + "\", but actual was \"" + actual + "\" (using delta duration of \"" + delta + "\")");
	}

	static void assertOneEquals(final String message, final org.joda.time.DateTime expected, final org.joda.time.DateTime actual, final org.joda.time.Duration delta) {
		if (expected == null) Assert.fail(message + "expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		if (expected == actual) return;
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual, delta);
	}

	public static void assertOneEquals(final org.joda.time.DateTime expected, final org.joda.time.DateTime actual, final org.joda.time.Duration delta) {
		assertOneEquals("OneTimestamp mismatch: ", expected, actual, delta);
	}

	public static void assertOneEquals(final org.joda.time.DateTime expected, final org.joda.time.DateTime actual) {
		assertOneEquals(expected, actual, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableEquals(final String message, final org.joda.time.DateTime expected, final org.joda.time.DateTime actual, final org.joda.time.Duration delta) {
		if (expected == actual) return;
		if (expected == null) Assert.fail(message + "expected was <null>, but actual was \"" + actual + "\"");
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual, delta);
	}

	public static void assertNullableEquals(final org.joda.time.DateTime expected, final org.joda.time.DateTime actual, final org.joda.time.Duration delta) {
		assertNullableEquals("NullableTimestamp mismatch: ", expected, actual, delta);
	}

	public static void assertNullableEquals(final org.joda.time.DateTime expected, final org.joda.time.DateTime actual) {
		assertNullableEquals(expected, actual, org.joda.time.Duration.ZERO);
	}

	private static void assertArrayOfOneEquals(final String message, final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i], delta);
		}
	}

	private static void assertOneArrayOfOneEquals(final String message, final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		for (int i = 0; i < expecteds.length; i ++) {
			if (expecteds[i] == null) {
				Assert.fail(message + "expecteds contained a <null> element at index " + i + " - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
			}
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneArrayOfOneEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		assertOneArrayOfOneEquals("OneArrayOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneArrayOfOneEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals) {
		assertOneArrayOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableArrayOfOneEquals(final String message, final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableArrayOfOneEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		assertNullableArrayOfOneEquals("NullableArrayOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableArrayOfOneEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals) {
		assertNullableArrayOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertArrayOfNullableEquals(final String message, final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i], delta);
		}
	}

	private static void assertOneArrayOfNullableEquals(final String message, final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneArrayOfNullableEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		assertOneArrayOfNullableEquals("OneArrayOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneArrayOfNullableEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals) {
		assertOneArrayOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableArrayOfNullableEquals(final String message, final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableArrayOfNullableEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals, final org.joda.time.Duration delta) {
		assertNullableArrayOfNullableEquals("NullableArrayOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableArrayOfNullableEquals(final org.joda.time.DateTime[] expecteds, final org.joda.time.DateTime[] actuals) {
		assertNullableArrayOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertListOfOneEquals(final String message, final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneListOfOneEquals(final String message, final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		int i = 0;
		for (final org.joda.time.DateTime expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneListOfOneEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneListOfOneEquals("OneListOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneListOfOneEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals) {
		assertOneListOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableListOfOneEquals(final String message, final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableListOfOneEquals("NullableListOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals) {
		assertNullableListOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertListOfNullableEquals(final String message, final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneListOfNullableEquals(final String message, final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneListOfNullableEquals("OneListOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals) {
		assertOneListOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableListOfNullableEquals(final String message, final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableListOfNullableEquals("NullableListOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<org.joda.time.DateTime> expecteds, final java.util.List<org.joda.time.DateTime> actuals) {
		assertNullableListOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertSetOfOneEquals(final String message, final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (actuals.contains(null)) {
			Assert.fail(message + "actuals contained a <null> element");
		}

		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final org.joda.time.DateTime expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final org.joda.time.DateTime actual : actuals) {
				try {
					assertOneEquals(expected, actual, delta);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfOneEquals(final String message, final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds.contains(null)) {
			Assert.fail(message + "expecteds contained a <null> element - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneSetOfOneEquals("OneSetOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals) {
		assertOneSetOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableSetOfOneEquals(final String message, final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableSetOfOneEquals("NullableSetOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals) {
		assertNullableSetOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertSetOfNullableEquals(final String message, final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final org.joda.time.DateTime expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final org.joda.time.DateTime actual : actuals) {
				try {
					assertNullableEquals(expected, actual, delta);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfNullableEquals(final String message, final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneSetOfNullableEquals("OneSetOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals) {
		assertOneSetOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableSetOfNullableEquals(final String message, final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableSetOfNullableEquals("NullableSetOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<org.joda.time.DateTime> expecteds, final java.util.Set<org.joda.time.DateTime> actuals) {
		assertNullableSetOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertQueueOfOneEquals(final String message, final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneQueueOfOneEquals(final String message, final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		int i = 0;
		for (final org.joda.time.DateTime expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneQueueOfOneEquals("OneQueueOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals) {
		assertOneQueueOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableQueueOfOneEquals(final String message, final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableQueueOfOneEquals("NullableQueueOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals) {
		assertNullableQueueOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertQueueOfNullableEquals(final String message, final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneQueueOfNullableEquals(final String message, final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneQueueOfNullableEquals("OneQueueOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals) {
		assertOneQueueOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableQueueOfNullableEquals(final String message, final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableQueueOfNullableEquals("NullableQueueOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<org.joda.time.DateTime> expecteds, final java.util.Queue<org.joda.time.DateTime> actuals) {
		assertNullableQueueOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertLinkedListOfOneEquals(final String message, final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneLinkedListOfOneEquals(final String message, final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		int i = 0;
		for (final org.joda.time.DateTime expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneLinkedListOfOneEquals("OneLinkedListOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals) {
		assertOneLinkedListOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableLinkedListOfOneEquals(final String message, final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableLinkedListOfOneEquals("NullableLinkedListOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals) {
		assertNullableLinkedListOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertLinkedListOfNullableEquals(final String message, final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneLinkedListOfNullableEquals(final String message, final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneLinkedListOfNullableEquals("OneLinkedListOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals) {
		assertOneLinkedListOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableLinkedListOfNullableEquals(final String message, final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableLinkedListOfNullableEquals("NullableLinkedListOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<org.joda.time.DateTime> expecteds, final java.util.LinkedList<org.joda.time.DateTime> actuals) {
		assertNullableLinkedListOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertStackOfOneEquals(final String message, final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneStackOfOneEquals(final String message, final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		int i = 0;
		for (final org.joda.time.DateTime expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneStackOfOneEquals("OneStackOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals) {
		assertOneStackOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableStackOfOneEquals(final String message, final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableStackOfOneEquals("NullableStackOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals) {
		assertNullableStackOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertStackOfNullableEquals(final String message, final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneStackOfNullableEquals(final String message, final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneStackOfNullableEquals("OneStackOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals) {
		assertOneStackOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableStackOfNullableEquals(final String message, final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableStackOfNullableEquals("NullableStackOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<org.joda.time.DateTime> expecteds, final java.util.Stack<org.joda.time.DateTime> actuals) {
		assertNullableStackOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertVectorOfOneEquals(final String message, final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneVectorOfOneEquals(final String message, final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		int i = 0;
		for (final org.joda.time.DateTime expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneVectorOfOneEquals("OneVectorOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals) {
		assertOneVectorOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableVectorOfOneEquals(final String message, final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableVectorOfOneEquals("NullableVectorOfOneTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals) {
		assertNullableVectorOfOneEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertVectorOfNullableEquals(final String message, final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<org.joda.time.DateTime> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<org.joda.time.DateTime> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final org.joda.time.DateTime expected = expectedsIterator.next();
			final org.joda.time.DateTime actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual, delta);
		}
	}

	private static void assertOneVectorOfNullableEquals(final String message, final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertOneVectorOfNullableEquals("OneVectorOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals) {
		assertOneVectorOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}

	private static void assertNullableVectorOfNullableEquals(final String message, final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals, delta);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals, final org.joda.time.Duration delta) {
		assertNullableVectorOfNullableEquals("NullableVectorOfNullableTimestamp mismatch: ", expecteds, actuals, delta);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<org.joda.time.DateTime> expecteds, final java.util.Vector<org.joda.time.DateTime> actuals) {
		assertNullableVectorOfNullableEquals(expecteds, actuals, org.joda.time.Duration.ZERO);
	}
}
