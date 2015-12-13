package com.dslplatform.json.generated.ocd.javaasserts;

import org.junit.Assert;

public class BinaryAsserts {
	static void assertSingleEquals(final String message, final byte[] expected, final byte[] actual) {
		if (expected.length != actual.length) {
			Assert.fail(message + "expected was a Binary of " + expected.length + " bytes, but actual was a Binary of " + actual.length + " bytes");
		}

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				Assert.fail(message + "Binary differs at index " + i + ": expected was \"" + expected[i] + "\", but actual was \"" + actual[i] + "\"");
			}
		}
	}

	static void assertOneEquals(final String message, final byte[] expected, final byte[] actual) {
		if (expected == null) Assert.fail(message + "expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		if (expected == actual) return;
		if (actual == null) Assert.fail(message + "expected was a Binary of " + expected.length + " bytes, but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertOneEquals(final byte[] expected, final byte[] actual) {
		assertOneEquals("OneBinary mismatch: ", expected, actual);
	}

	private static void assertNullableEquals(final String message, final byte[] expected, final byte[] actual) {
		if (expected == actual) return;
		if (expected == null) Assert.fail(message + "expected was <null>, but actual was a Binary of " + actual.length + " bytes");
		if (actual == null) Assert.fail(message + "expected was a Binary of " + expected.length + " bytes, but actual was <null>");
		assertSingleEquals(message, expected, actual);
	}

	public static void assertNullableEquals(final byte[] expected, final byte[] actual) {
		assertNullableEquals("NullableBinary mismatch: ", expected, actual);
	}

	private static void assertArrayOfOneEquals(final String message, final byte[][] expecteds, final byte[][] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfOneEquals(final String message, final byte[][] expecteds, final byte[][] actuals) {
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

	public static void assertOneArrayOfOneEquals(final byte[][] expecteds, final byte[][] actuals) {
		assertOneArrayOfOneEquals("OneArrayOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfOneEquals(final String message, final byte[][] expecteds, final byte[][] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfOneEquals(final byte[][] expecteds, final byte[][] actuals) {
		assertNullableArrayOfOneEquals("NullableArrayOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertArrayOfNullableEquals(final String message, final byte[][] expecteds, final byte[][] actuals) {
		if (expecteds.length != actuals.length) {
			Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was an array of length " + actuals.length);
		}

		for (int i = 0; i < expecteds.length; i++) {
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expecteds[i], actuals[i]);
		}
	}

	private static void assertOneArrayOfNullableEquals(final String message, final byte[][] expecteds, final byte[][] actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneArrayOfNullableEquals(final byte[][] expecteds, final byte[][] actuals) {
		assertOneArrayOfNullableEquals("OneArrayOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableArrayOfNullableEquals(final String message, final byte[][] expecteds, final byte[][] actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was an array of length " + actuals.length);
		if (actuals == null) Assert.fail(message + " expecteds was an array of length " + expecteds.length + ", but actuals was <null>");
		assertArrayOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableArrayOfNullableEquals(final byte[][] expecteds, final byte[][] actuals) {
		assertNullableArrayOfNullableEquals("NullableArrayOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertListOfOneEquals(final String message, final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfOneEquals(final String message, final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		int i = 0;
		for (final byte[] expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfOneEquals(final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		assertOneListOfOneEquals("OneListOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfOneEquals(final String message, final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfOneEquals(final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		assertNullableListOfOneEquals("NullableListOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertListOfNullableEquals(final String message, final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a list of size " + expectedsSize + ", but actuals was a list of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneListOfNullableEquals(final String message, final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneListOfNullableEquals(final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		assertOneListOfNullableEquals("OneListOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableListOfNullableEquals(final String message, final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a list of size " + expecteds.size() + ", but actuals was <null>");
		assertListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableListOfNullableEquals(final java.util.List<byte[]> expecteds, final java.util.List<byte[]> actuals) {
		assertNullableListOfNullableEquals("NullableListOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfOneEquals(final String message, final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		if (actuals.contains(null)) {
			Assert.fail(message + "actuals contained a <null> element");
		}

		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final byte[] expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final byte[] actual : actuals) {
				try {
					assertOneEquals(expected, actual);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfOneEquals(final String message, final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		if (expecteds.contains(null)) {
			Assert.fail(message + "expecteds contained a <null> element - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfOneEquals(final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		assertOneSetOfOneEquals("OneSetOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfOneEquals(final String message, final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfOneEquals(final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		assertNullableSetOfOneEquals("NullableSetOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertSetOfNullableEquals(final String message, final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a set of size " + expectedsSize + ", but actuals was a set of size " + actualsSize);
		}

		expectedsLoop: for (final byte[] expected : expecteds) {
			if (actuals.contains(expected)) continue;
			for (final byte[] actual : actuals) {
				try {
					assertNullableEquals(expected, actual);
					continue expectedsLoop;
				}
				catch (final AssertionError e) {}
			}
			Assert.fail(message + "actuals did not contain the expecteds element \"" + expected + "\"");
		}
	}

	private static void assertOneSetOfNullableEquals(final String message, final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneSetOfNullableEquals(final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		assertOneSetOfNullableEquals("OneSetOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableSetOfNullableEquals(final String message, final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a set of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a set of size " + expecteds.size() + ", but actuals was <null>");
		assertSetOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableSetOfNullableEquals(final java.util.Set<byte[]> expecteds, final java.util.Set<byte[]> actuals) {
		assertNullableSetOfNullableEquals("NullableSetOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfOneEquals(final String message, final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfOneEquals(final String message, final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		int i = 0;
		for (final byte[] expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfOneEquals(final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		assertOneQueueOfOneEquals("OneQueueOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfOneEquals(final String message, final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfOneEquals(final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		assertNullableQueueOfOneEquals("NullableQueueOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertQueueOfNullableEquals(final String message, final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a queue of size " + expectedsSize + ", but actuals was a queue of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneQueueOfNullableEquals(final String message, final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneQueueOfNullableEquals(final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		assertOneQueueOfNullableEquals("OneQueueOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableQueueOfNullableEquals(final String message, final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a queue of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a queue of size " + expecteds.size() + ", but actuals was <null>");
		assertQueueOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableQueueOfNullableEquals(final java.util.Queue<byte[]> expecteds, final java.util.Queue<byte[]> actuals) {
		assertNullableQueueOfNullableEquals("NullableQueueOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfOneEquals(final String message, final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfOneEquals(final String message, final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		int i = 0;
		for (final byte[] expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfOneEquals(final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		assertOneLinkedListOfOneEquals("OneLinkedListOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfOneEquals(final String message, final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfOneEquals(final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		assertNullableLinkedListOfOneEquals("NullableLinkedListOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertLinkedListOfNullableEquals(final String message, final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a linked list of size " + expectedsSize + ", but actuals was a linked list of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneLinkedListOfNullableEquals(final String message, final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneLinkedListOfNullableEquals(final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		assertOneLinkedListOfNullableEquals("OneLinkedListOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableLinkedListOfNullableEquals(final String message, final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a linked list of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a linked list of size " + expecteds.size() + ", but actuals was <null>");
		assertLinkedListOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableLinkedListOfNullableEquals(final java.util.LinkedList<byte[]> expecteds, final java.util.LinkedList<byte[]> actuals) {
		assertNullableLinkedListOfNullableEquals("NullableLinkedListOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfOneEquals(final String message, final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfOneEquals(final String message, final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		int i = 0;
		for (final byte[] expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfOneEquals(final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		assertOneStackOfOneEquals("OneStackOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfOneEquals(final String message, final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfOneEquals(final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		assertNullableStackOfOneEquals("NullableStackOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertStackOfNullableEquals(final String message, final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a stack of size " + expectedsSize + ", but actuals was a stack of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneStackOfNullableEquals(final String message, final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneStackOfNullableEquals(final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		assertOneStackOfNullableEquals("OneStackOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableStackOfNullableEquals(final String message, final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a stack of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a stack of size " + expecteds.size() + ", but actuals was <null>");
		assertStackOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableStackOfNullableEquals(final java.util.Stack<byte[]> expecteds, final java.util.Stack<byte[]> actuals) {
		assertNullableStackOfNullableEquals("NullableStackOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfOneEquals(final String message, final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertOneEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfOneEquals(final String message, final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		int i = 0;
		for (final byte[] expected : expecteds) {
			if (expected == null) {
				Assert.fail(message + "element mismatch occurred at index " + i + ": expected was <null> - WARNING: This is a preconditions failure in expected, this assertion will never succeed!");
			}
			i++;
		}
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfOneEquals(final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		assertOneVectorOfOneEquals("OneVectorOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfOneEquals(final String message, final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfOneEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfOneEquals(final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		assertNullableVectorOfOneEquals("NullableVectorOfOneBinary mismatch: ", expecteds, actuals);
	}

	private static void assertVectorOfNullableEquals(final String message, final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		final int expectedsSize = expecteds.size();
		final int actualsSize = actuals.size();
		if (expectedsSize != actualsSize) {
			Assert.fail(message + "expecteds was a vector of size " + expectedsSize + ", but actuals was a vector of size " + actualsSize);
		}

		final java.util.Iterator<byte[]> expectedsIterator = expecteds.iterator();
		final java.util.Iterator<byte[]> actualsIterator = actuals.iterator();
		for (int i = 0; i < expectedsSize; i++) {
			final byte[] expected = expectedsIterator.next();
			final byte[] actual = actualsIterator.next();
			assertNullableEquals(message + "element mismatch occurred at index " + i + ": ", expected, actual);
		}
	}

	private static void assertOneVectorOfNullableEquals(final String message, final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		if (expecteds == null) Assert.fail(message + "expecteds was <null> - WARNING: This is a preconditions failure in expecteds, this assertion will never succeed!");
		if (expecteds == actuals) return;
		if (actuals == null) Assert.fail(message + "expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertOneVectorOfNullableEquals(final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		assertOneVectorOfNullableEquals("OneVectorOfNullableBinary mismatch: ", expecteds, actuals);
	}

	private static void assertNullableVectorOfNullableEquals(final String message, final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		if (expecteds == actuals) return;
		if (expecteds == null) Assert.fail(message + "expecteds was <null>, but actuals was a vector of size " + actuals.size());
		if (actuals == null) Assert.fail(message + " expecteds was a vector of size " + expecteds.size() + ", but actuals was <null>");
		assertVectorOfNullableEquals(message, expecteds, actuals);
	}

	public static void assertNullableVectorOfNullableEquals(final java.util.Vector<byte[]> expecteds, final java.util.Vector<byte[]> actuals) {
		assertNullableVectorOfNullableEquals("NullableVectorOfNullableBinary mismatch: ", expecteds, actuals);
	}
}
