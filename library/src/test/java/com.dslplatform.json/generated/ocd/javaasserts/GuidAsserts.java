package com.dslplatform.json.generated.ocd.javaasserts;

import org.junit.Assert;

public class GuidAsserts {
	static void assertSingleEquals(final String message, final java.util.UUID expected, final java.util.UUID actual) {
		if (expected.equals(actual)) return;
		Assert.fail(message + "expected was \"" + expected + "\", but actual was \"" + actual + "\"");
	}

	static void assertOneEquals(final String message, final java.util.UUID expected, final java.util.UUID actual) {
		if (expected == null) Assert.fail(message + "expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		if (expected == actual) return;
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertOneEquals(final java.util.UUID expected, final java.util.UUID actual) {
		assertOneEquals("OneGuid mismatch: ", expected, actual);
	}

	private static void assertNullableEquals(final String message, final java.util.UUID expected, final java.util.UUID actual) {
		if (expected == actual) return;
		if (expected == null) Assert.fail(message + "expected was <null>, but actual was \"" + actual + "\"");
		if (actual == null) Assert.fail(message + "expected was \"" + expected + "\", but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertNullableEquals(final java.util.UUID expected, final java.util.UUID actual) {
		assertNullableEquals("NullableGuid mismatch: ", expected, actual);
	}

	private static void assertArrayOfOneEquals(final String message, final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfOneEquals(final String message, final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
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

	public static void assertOneArrayOfOneEquals(final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		assertOneArrayOfOneEquals("OneArrayOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfOneEquals(final String message, final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfOneEquals(final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		assertNullableArrayOfOneEquals("NullableArrayOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertArrayOfNullableEquals(final String message, final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfNullableEquals(final String message, final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneArrayOfNullableEquals(final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		assertOneArrayOfNullableEquals("OneArrayOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfNullableEquals(final String message, final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfNullableEquals(final java.util.UUID[] expecteds, final java.util.UUID[] actuals) {
		assertNullableArrayOfNullableEquals("NullableArrayOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertListOfOneEquals(final String message, final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfOneEquals(final String message, final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		int i = 0;
		for (final java.util.UUID expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfOneEquals(final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		assertOneListOfOneEquals("OneListOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfOneEquals(final String message, final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		assertNullableListOfOneEquals("NullableListOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertListOfNullableEquals(final String message, final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfNullableEquals(final String message, final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		assertOneListOfNullableEquals("OneListOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfNullableEquals(final String message, final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<java.util.UUID> expecteds, final java.util.List<java.util.UUID> actuals) {
		assertNullableListOfNullableEquals("NullableListOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfOneEquals(final String message, final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		if (actuals.contains(null)) {
			Assert.fail(message + "actuals contained a <null> element");
		}

		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		for (final java.util.UUID expected : expecteds) {
			if (!actuals.contains(expected)) {
				Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
			}
		}
	}

	private static void assertOneSetOfOneEquals(final String message, final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		if (expecteds.contains(null)) {
			Assert.fail(message + "expecteds contained a <null> element - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		assertOneSetOfOneEquals("OneSetOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfOneEquals(final String message, final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		assertNullableSetOfOneEquals("NullableSetOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfNullableEquals(final String message, final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		for (final java.util.UUID expected : expecteds) {
			if (!actuals.contains(expected)) {
				Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
			}
		}
	}

	private static void assertOneSetOfNullableEquals(final String message, final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		assertOneSetOfNullableEquals("OneSetOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfNullableEquals(final String message, final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<java.util.UUID> expecteds, final java.util.Set<java.util.UUID> actuals) {
		assertNullableSetOfNullableEquals("NullableSetOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfOneEquals(final String message, final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfOneEquals(final String message, final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		int i = 0;
		for (final java.util.UUID expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		assertOneQueueOfOneEquals("OneQueueOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfOneEquals(final String message, final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		assertNullableQueueOfOneEquals("NullableQueueOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfNullableEquals(final String message, final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfNullableEquals(final String message, final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		assertOneQueueOfNullableEquals("OneQueueOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfNullableEquals(final String message, final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<java.util.UUID> expecteds, final java.util.Queue<java.util.UUID> actuals) {
		assertNullableQueueOfNullableEquals("NullableQueueOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		int i = 0;
		for (final java.util.UUID expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		assertOneLinkedListOfOneEquals("OneLinkedListOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfOneEquals(final String message, final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		assertNullableLinkedListOfOneEquals("NullableLinkedListOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		assertOneLinkedListOfNullableEquals("OneLinkedListOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfNullableEquals(final String message, final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<java.util.UUID> expecteds, final java.util.LinkedList<java.util.UUID> actuals) {
		assertNullableLinkedListOfNullableEquals("NullableLinkedListOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfOneEquals(final String message, final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfOneEquals(final String message, final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		int i = 0;
		for (final java.util.UUID expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		assertOneStackOfOneEquals("OneStackOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfOneEquals(final String message, final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		assertNullableStackOfOneEquals("NullableStackOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfNullableEquals(final String message, final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfNullableEquals(final String message, final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		assertOneStackOfNullableEquals("OneStackOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfNullableEquals(final String message, final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<java.util.UUID> expecteds, final java.util.Stack<java.util.UUID> actuals) {
		assertNullableStackOfNullableEquals("NullableStackOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfOneEquals(final String message, final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfOneEquals(final String message, final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		int i = 0;
		for (final java.util.UUID expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		assertOneVectorOfOneEquals("OneVectorOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfOneEquals(final String message, final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		assertNullableVectorOfOneEquals("NullableVectorOfOneGuid mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfNullableEquals(final String message, final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<java.util.UUID> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<java.util.UUID> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final java.util.UUID expected = expectedsIterator.next();
			final java.util.UUID actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfNullableEquals(final String message, final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		assertOneVectorOfNullableEquals("OneVectorOfNullableGuid mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfNullableEquals(final String message, final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<java.util.UUID> expecteds, final java.util.Vector<java.util.UUID> actuals) {
		assertNullableVectorOfNullableEquals("NullableVectorOfNullableGuid mismatch: ", expecteds, actuals);
	}
}
