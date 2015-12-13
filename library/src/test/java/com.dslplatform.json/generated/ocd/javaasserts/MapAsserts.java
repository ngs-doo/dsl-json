package com.dslplatform.json.generated.ocd.javaasserts;

import org.junit.Assert;

public class MapAsserts {
	static void assertSingleEquals(final String message, final java.util.Map<String, String> expected, final java.util.Map<String, String> actual) {
		if (expected.containsKey(null)) {
			Assert.fail(message + "expected contained a <null> key - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}

		if (actual.containsKey(null)) {
			Assert.fail(message + "actual contained a <null> key");
		}

		if (expected.equals(actual)) return;
		Assert.fail(message + "expected was \"" + expected + "\", but actual was \"" + actual + "\"");
	}

	static void assertOneEquals(final String message, final java.util.Map<String, String> expected, final java.util.Map<String, String> actual) {
		if (expected == null) Assert.fail(message + "expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		if (expected == actual) return;
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertOneEquals(final java.util.Map<String, String> expected, final java.util.Map<String, String> actual) {
		assertOneEquals("OneMap mismatch: ", expected, actual);
	}

	private static void assertNullableEquals(final String message, final java.util.Map<String, String> expected, final java.util.Map<String, String> actual) {
		if (expected == actual) return;
		if (expected == null) Assert.fail(message + "expected was <null>, but actual was \"" + actual + "\"");
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertNullableEquals(final java.util.Map<String, String> expected, final java.util.Map<String, String> actual) {
		assertNullableEquals("NullableMap mismatch: ", expected, actual);
	}

	private static void assertArrayOfOneEquals(final String message, final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfOneEquals(final String message, final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
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

	public static void assertOneArrayOfOneEquals(final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		assertOneArrayOfOneEquals("OneArrayOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfOneEquals(final String message, final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfOneEquals(final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		assertNullableArrayOfOneEquals("NullableArrayOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertArrayOfNullableEquals(final String message, final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfNullableEquals(final String message, final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneArrayOfNullableEquals(final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		assertOneArrayOfNullableEquals("OneArrayOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfNullableEquals(final String message, final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfNullableEquals(final java.util.Map<String, String>[] expecteds, final java.util.Map<String, String>[] actuals) {
		assertNullableArrayOfNullableEquals("NullableArrayOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertListOfOneEquals(final String message, final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfOneEquals(final String message, final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		int i = 0;
		for (final java.util.Map<String, String> expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfOneEquals(final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		assertOneListOfOneEquals("OneListOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfOneEquals(final String message, final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		assertNullableListOfOneEquals("NullableListOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertListOfNullableEquals(final String message, final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfNullableEquals(final String message, final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		assertOneListOfNullableEquals("OneListOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfNullableEquals(final String message, final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<java.util.Map<String, String>> expecteds, final java.util.List<java.util.Map<String, String>> actuals) {
		assertNullableListOfNullableEquals("NullableListOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfOneEquals(final String message, final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		if (actuals.contains(null)) {
			Assert.fail(message + "actuals contained a <null> element");
		}

		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		for (final java.util.Map<String, String> expected : expecteds) {
			if (!actuals.contains(expected)) {
				Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
			}
		}
	}

	private static void assertOneSetOfOneEquals(final String message, final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		if (expecteds.contains(null)) {
			Assert.fail(message + "expecteds contained a <null> element - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		assertOneSetOfOneEquals("OneSetOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfOneEquals(final String message, final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		assertNullableSetOfOneEquals("NullableSetOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfNullableEquals(final String message, final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		for (final java.util.Map<String, String> expected : expecteds) {
			if (!actuals.contains(expected)) {
				Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
			}
		}
	}

	private static void assertOneSetOfNullableEquals(final String message, final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		assertOneSetOfNullableEquals("OneSetOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfNullableEquals(final String message, final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<java.util.Map<String, String>> expecteds, final java.util.Set<java.util.Map<String, String>> actuals) {
		assertNullableSetOfNullableEquals("NullableSetOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfOneEquals(final String message, final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfOneEquals(final String message, final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		int i = 0;
		for (final java.util.Map<String, String> expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		assertOneQueueOfOneEquals("OneQueueOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfOneEquals(final String message, final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		assertNullableQueueOfOneEquals("NullableQueueOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfNullableEquals(final String message, final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfNullableEquals(final String message, final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		assertOneQueueOfNullableEquals("OneQueueOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfNullableEquals(final String message, final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<java.util.Map<String, String>> expecteds, final java.util.Queue<java.util.Map<String, String>> actuals) {
		assertNullableQueueOfNullableEquals("NullableQueueOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		int i = 0;
		for (final java.util.Map<String, String> expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		assertOneLinkedListOfOneEquals("OneLinkedListOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		assertNullableLinkedListOfOneEquals("NullableLinkedListOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		assertOneLinkedListOfNullableEquals("OneLinkedListOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<java.util.Map<String, String>> expecteds, final java.util.LinkedList<java.util.Map<String, String>> actuals) {
		assertNullableLinkedListOfNullableEquals("NullableLinkedListOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfOneEquals(final String message, final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfOneEquals(final String message, final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		int i = 0;
		for (final java.util.Map<String, String> expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		assertOneStackOfOneEquals("OneStackOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfOneEquals(final String message, final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		assertNullableStackOfOneEquals("NullableStackOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfNullableEquals(final String message, final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfNullableEquals(final String message, final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		assertOneStackOfNullableEquals("OneStackOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfNullableEquals(final String message, final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<java.util.Map<String, String>> expecteds, final java.util.Stack<java.util.Map<String, String>> actuals) {
		assertNullableStackOfNullableEquals("NullableStackOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfOneEquals(final String message, final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfOneEquals(final String message, final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		int i = 0;
		for (final java.util.Map<String, String> expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		assertOneVectorOfOneEquals("OneVectorOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfOneEquals(final String message, final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		assertNullableVectorOfOneEquals("NullableVectorOfOneMap mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfNullableEquals(final String message, final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<java.util.Map<String, String>> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.Map<String, String>> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.Map<String, String> expected = expectedsIterator.next();
			final java.util.Map<String, String> actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfNullableEquals(final String message, final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		assertOneVectorOfNullableEquals("OneVectorOfNullableMap mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfNullableEquals(final String message, final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<java.util.Map<String, String>> expecteds, final java.util.Vector<java.util.Map<String, String>> actuals) {
		assertNullableVectorOfNullableEquals("NullableVectorOfNullableMap mismatch: ", expecteds, actuals);
	}
}
