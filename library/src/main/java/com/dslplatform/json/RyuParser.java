package com.dslplatform.json;

/**
 * <p>
 * Java port of the s2d/s2f core of the Ryu algorithm (Ulf Adams, see
 * https://www.ryu-lang.org / github.com/ulfjack/ryu). Unlike a fixed-precision
 * approximation this implementation always produces exactly the same result as
 * {@link Double#parseDouble(String)} and {@link Float#parseFloat(String)}
 * (round-half-to-even), for any number of digits, and it performs no
 * allocations: numbers that fit in a single {@code long} are handled with 64-bit
 * arithmetic only, longer ones use a per-thread scratch buffer.
 */
final class RyuParser {

	/** floor(log2(10) * 2^40). */
	private static final long LOG2_10_F40 = 3652498566964L;

	/** Largest n such that 5^n fits in a positive long. */
	private static final int MAX_POW5_LONG = 27;


	private static final ThreadLocal<long[]> SCRATCH = new ThreadLocal<long[]>() {
		@Override
		protected long[] initialValue() {
			return new long[512];
		}
	};

	private static final ThreadLocal<Parsed> PARSED = new ThreadLocal<Parsed>() {
		@Override
		protected Parsed initialValue() {
			return new Parsed();
		}
	};

	/** 5^n for n = 0..tableSize-1, little-endian base 2^64. Grown on demand. */
	private static volatile Pow5Table POW5_TABLE = buildPow5Table(900);

	/** 5^n for n = 0..MAX_POW5_LONG (single-word values). */
	private static final long[] POW5_LONG = new long[MAX_POW5_LONG + 1];

	static {
		for (int i = 0; i <= MAX_POW5_LONG; i++) {
			POW5_LONG[i] = POW5_TABLE.values[i][0];
		}
	}

	private static final class Pow5Table {
		final long[][] values;
		final int[] words;
		final int[] bitLen;

		Pow5Table(final long[][] values, final int[] words, final int[] bitLen) {
			this.values = values;
			this.words = words;
			this.bitLen = bitLen;
		}
	}

	private static synchronized void ensurePow5(final int n) {
		if (n < POW5_TABLE.values.length) return;
		final int newSize = Math.max(n + 1, POW5_TABLE.values.length * 2);
		POW5_TABLE = buildPow5Table(newSize);
	}

	private static Pow5Table buildPow5Table(final int size) {
		final long[][] values = new long[size][];
		final int[] words = new int[size];
		final int[] bitLen = new int[size];
		long[] cur = {1};
		for (int i = 0; i < size; i++) {
			values[i] = cur;
			words[i] = cur.length;
			bitLen[i] = bitLength(cur, 0, cur.length);
			if (i + 1 < size) cur = mulBy5(cur);
		}
		return new Pow5Table(values, words, bitLen);
	}

	private static long[] mulBy5(final long[] a) {
		final int n = a.length;
		final long[] out = new long[n + 1];
		long carry = 0;
		for (int i = 0; i < n; i++) {
			final long lo = a[i] * 5;
			final long hi = mulHi(a[i], 5);
			final long sum = lo + carry;
			out[i] = sum;
			carry = hi + (Long.compareUnsigned(sum, lo) < 0 ? 1L : 0L);
		}
		if (carry != 0) out[n] = carry;
		int m = out.length;
		while (m > 1 && out[m - 1] == 0) m--;
		return m == out.length ? out : java.util.Arrays.copyOf(out, m);
	}

	/** Parsed significand and exponent of a number literal. */
	private static final class Parsed {
		long mLong;      // M when bigWords == 0
		int bigWords;    // word count of M in scratch[0..bigWords) (0 => use mLong)
		int totalDigits; // digit characters consumed
		int firstNonZero;// index (in the digit stream) of the first non-zero digit, -1 if none
		int exp;         // decimal exponent E = exponentPart - fractionDigitCount
		boolean negative;

		void reset() {
			mLong = 0;
			bigWords = 0;
			totalDigits = 0;
			firstNonZero = -1;
			exp = 0;
			negative = false;
		}

		boolean isZero() {
			return firstNonZero < 0;
		}

		int sigDigits() {
			return totalDigits - firstNonZero;
		}
	}


	static double parseDouble(final JsonReader reader, final byte[] buf, final int start, final int end) throws ParsingException {
		return toDouble(scan(reader, buf, start, end));
	}

	static float parseFloat(final JsonReader reader, final byte[] buf, final int start, final int end) throws ParsingException {
		return toFloat(scan(reader, buf, start, end));
	}


	private static Parsed scan(final JsonReader reader, final byte[] buf, final int start, final int end0) throws ParsingException {
		int end = end0;
		while (end > start && isWs(buf[end - 1])) end--;
		final Parsed p = PARSED.get();
		p.reset();
		if (end == start) {
			// no digits at all: empty or whitespace-only span
			NumberConverter.numberException(reader, start, end0, end0 != start ? "Unknown digit" : "Digit not found", end0 != start ? ' ' : null);
		}
		int i = start;
		if (buf[i] == '-') {
			p.negative = true;
			i++;
		} else if (buf[i] == '+') {
			NumberConverter.numberException(reader, start, end0, "Unknown digit", '+');
		}
		if (end == i) NumberConverter.numberException(reader, start, end0, "Digit not found", null);
		long mLong = 0;
		int bigWords = 0;
		boolean leadingZero = false;
		final int digitsStart = i;
		byte ch = ' ';
		// ---- integer part ----
		for (; i < end; i++) {
			ch = buf[i];
			if (ch == '.' || ch == 'e' || ch == 'E') break;
			final int ind = ch - 48;
			if (ind < 0 || ind > 9) {
				if (leadingZero && i > digitsStart + 1) NumberConverter.numberException(reader, start, end0, "Leading zero is not allowed", null);
				NumberConverter.numberException(reader, start, end0, "Unknown digit", (char) ch);
			}
			if (p.totalDigits == 0 && ind == 0) leadingZero = true;
			addDigit(p, ind, mLong, bigWords);
			mLong = p.mLong;
			bigWords = p.bigWords;
		}
		if (i == digitsStart) NumberConverter.numberException(reader, start, end0, "Digit not found", null);
		if (leadingZero && ch != '.' && i > digitsStart + 1) NumberConverter.numberException(reader, start, end0, "Leading zero is not allowed", null);
		int expPart = 0;
		int fracCount = 0;
		if (i < end && ch == '.') {
			i++;
			if (i == end) NumberConverter.numberException(reader, start, end0, "Number ends with a dot", null);
			for (; i < end; i++) {
				ch = buf[i];
				if (ch == 'e' || ch == 'E') break;
				final int ind = ch - 48;
				if (ind < 0 || ind > 9) {
					if (leadingZero && i > digitsStart + 1) NumberConverter.numberException(reader, start, end0, "Leading zero is not allowed", null);
					NumberConverter.numberException(reader, start, end0, "Unknown digit", (char) ch);
				}
				addDigit(p, ind, mLong, bigWords);
				mLong = p.mLong;
				bigWords = p.bigWords;
				fracCount++;
			}
		}
		if (i < end) { // ch == 'e' || ch == 'E'
			i++;
			boolean expNeg = false;
			if (i < end && (buf[i] == '-' || buf[i] == '+')) {
				expNeg = buf[i] == '-';
				i++;
			}
			final int expDigitsStart = i;
			boolean saturated = false;
			while (i < end) {
				final int ind = buf[i] - 48;
				if (ind < 0 || ind > 9) NumberConverter.numberException(reader, start, end0, "Unknown digit", (char) buf[i]);
				if (!saturated) {
					expPart = expPart * 10 + ind;
					if (expPart > 10000) {
						expPart = 10000;
						saturated = true;
					}
				}
				i++;
			}
			if (i == expDigitsStart) NumberConverter.numberException(reader, start, end0, "Digit not found", null);
			p.exp = (expNeg ? -expPart : expPart) - fracCount;
		} else {
			p.exp = -fracCount;
		}
		trimMantissa(p);
		return p;
	}

	/** Drop trailing zero words: promotion in addDigit is triggered by signed overflow, so
	 * values in [2^63, 2^64) may carry a high word of zero. */
	private static void trimMantissa(final Parsed p) {
		if (p.bigWords > 1) {
			final long[] s = scratch();
			while (p.bigWords > 1 && s[p.bigWords - 1] == 0) p.bigWords--;
		}
	}

	/** M = M * 10 + ind, promoting to the big-word representation on overflow. */
	private static void addDigit(final Parsed p, final int ind, long mLong, int bigWords) {
		if (bigWords == 0) {
			if (mLong <= (Long.MAX_VALUE - ind) / 10) {
				p.mLong = mLong * 10 + ind;
			} else {
				final long[] s = ensureCapacity(scratch(), 2);
				final long lo = mLong * 10;
				long hi = mulHi(mLong, 10);
				final long sum = lo + ind;
				if (Long.compareUnsigned(sum, lo) < 0) hi++;
				s[0] = sum;
				s[1] = hi; // mLong > Long.MAX_VALUE / 10 => hi >= 1
				p.bigWords = 2;
				p.mLong = 0;
			}
		} else {
			final long[] s = ensureCapacity(scratch(), bigWords + 1);
			long carry = ind;
			for (int w = 0; w < bigWords; w++) {
				final long word = s[w];
				final long lo = word * 10;
				final long hi = mulHi(word, 10);
				final long sum = lo + carry;
				s[w] = sum;
				carry = hi + (Long.compareUnsigned(sum, lo) < 0 ? 1L : 0L);
			}
			if (carry != 0) s[bigWords++] = carry;
			p.bigWords = bigWords;
		}
		p.totalDigits++;
		if (p.firstNonZero < 0 && ind != 0) p.firstNonZero = p.totalDigits - 1;
	}

	private static boolean isWs(final byte b) {
		return (b >= 9 && b <= 13) || b == 32 || b == -96 || (b >= -31 && b <= -29);
	}


	private static double toDouble(final Parsed p) {
		if (p.isZero()) return p.negative ? -0.0 : 0.0;
		final int E = p.exp;
		final int sig = p.sigDigits();
		// range exits: V = M * 10^E with M having `sig` significant digits, M >= 1
		if (E + sig - 1 >= 309) return p.negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		if (E + sig <= -325) return p.negative ? -0.0 : 0.0;

		final int nM = p.bigWords == 0 ? 1 : p.bigWords;
		final long[] s0 = scratch();
		if (p.bigWords == 0) s0[0] = p.mLong;

		final int aExp = E > 0 ? E : 0;
		final int bExp = E < 0 ? -E : 0;
		ensurePow5(Math.max(aExp, bExp));
		final Pow5Table tab = POW5_TABLE;
		final int n5max = Math.max(tab.words[aExp], tab.words[bExp]);
		// pre-allocate scratch for the whole computation so references stay valid
		final long[] s = ensureCapacity(scratch(), 5 * nM + 7 * n5max + 128);

		// ---- find e such that 2^e <= V < 2^(e+1) ----
		final int blM = bitLength(s, 0, nM);
		int e = blM - 1 + (int) (((long) E * LOG2_10_F40) >> 40);
		while (gePow2(p, s, nM, e + 1)) e++;
		while (!gePow2(p, s, nM, e)) e--;

		final boolean subnormal = e < -1022;
		final int T = subnormal ? E + 1074 : E + 52 - e;

		// ---- R = V / ulp = M * 5^E * 2^T = A / D, round to nearest (ties to even) ----
		long q = fastDivide(p, s, T, aExp, bExp);
		if (q < 0) q = slowDivide(s, nM, T, aExp, bExp, subnormal ? 52 : 53);

		long bits;
		if (subnormal) {
			bits = q; // q <= 2^52; q == 2^52 encodes the smallest normal correctly
		} else {
			if (q == (1L << 53)) {
				e++;
				q = 1L << 52;
			}
			if (e > 1023) return p.negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
			bits = ((long) (e + 1023) << 52) | (q & 0xFFFFFFFFFFFFFL);
		}
		final double result = Double.longBitsToDouble(bits);
		return p.negative ? -result : result;
	}

	private static float toFloat(final Parsed p) {
		if (p.isZero()) return p.negative ? -0f : 0f;
		final int E = p.exp;
		final int sig = p.sigDigits();
		// range exits: V = M * 10^E with M having `sig` significant digits, M >= 1
		if (E + sig - 1 >= 39) return p.negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		if (E + sig <= -47) return p.negative ? -0f : 0f;

		final int nM = p.bigWords == 0 ? 1 : p.bigWords;
		final long[] s0 = scratch();
		if (p.bigWords == 0) s0[0] = p.mLong;

		final int aExp = E > 0 ? E : 0;
		final int bExp = E < 0 ? -E : 0;
		ensurePow5(Math.max(aExp, bExp));
		final Pow5Table tab = POW5_TABLE;
		final int n5max = Math.max(tab.words[aExp], tab.words[bExp]);
		// pre-allocate scratch for the whole computation so references stay valid
		final long[] s = ensureCapacity(scratch(), 5 * nM + 7 * n5max + 128);

		// ---- find e such that 2^e <= V < 2^(e+1) ----
		final int blM = bitLength(s, 0, nM);
		int e = blM - 1 + (int) (((long) E * LOG2_10_F40) >> 40);
		while (gePow2(p, s, nM, e + 1)) e++;
		while (!gePow2(p, s, nM, e)) e--;

		final boolean subnormal = e < -126;
		final int T = subnormal ? E + 149 : E + 23 - e;

		long q = fastDivide(p, s, T, aExp, bExp);
		if (q < 0) q = slowDivide(s, nM, T, aExp, bExp, subnormal ? 23 : 24);

		int bits;
		if (subnormal) {
			bits = (int) q; // q <= 2^23; q == 2^23 encodes the smallest normal correctly
		} else {
			if (q == (1L << 24)) {
				e++;
				q = 1L << 23;
			}
			if (e > 127) return p.negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
			bits = ((e + 127) << 23) | (int) (q & 0x7FFFFFL);
		}
		final float result = Float.intBitsToFloat(bits);
		return p.negative ? -result : result;
	}

	/** Is V = M * 10^E >= 2^k? (M >= 1, stored in s[0..nM)) */
	private static boolean gePow2(final Parsed p, final long[] s, final int nM, final int k) {
		final int E = p.exp;
		final int t = k - E; // V >= 2^k  <=>  M * 5^E >= 2^t
		ensurePow5(Math.max(E, -E));
		final Pow5Table tab = POW5_TABLE;
		if (E >= 0) {
			if (t <= 0) return true; // M * 5^E >= 1 >= 2^t
			final int blM = bitLength(s, 0, nM);
			final int bl5 = tab.bitLen[E];
			// M * 5^E has a bit length in [blM + bl5 - 1, blM + bl5]
			if (blM + bl5 - 1 >= t + 1) return true;
			if (blM + bl5 <= t) return false;
			// ambiguous: the product bit length is either t or t+1
			final int offP = nM + tab.words[E];
			ensureCapacity(s, offP + nM + tab.words[E]);
			final int nP = mul(s, 0, nM, tab.values[E], tab.words[E], offP);
			return bitLength(s, offP, nP) == t + 1;
		}
		// E < 0: V >= 2^k  <=>  M >= 5^(-E) * 2^t
		final int bExp = -E;
		if (t >= 0) {
			final int blR = t + tab.bitLen[bExp]; // exact bit length of 5^b << t (5^b is odd)
			final int blM = bitLength(s, 0, nM);
			if (blM > blR) return true;
			if (blM < blR) return false;
			final int offR = nM + tab.words[bExp] + 1;
			ensureCapacity(s, offR + tab.words[bExp] + 1);
			final int nR = shiftLeft(tab.values[bExp], 0, tab.words[bExp], t, offR);
			return cmp(s, 0, nM, s, offR, nR) >= 0;
		}
		// M * 2^(-t) >= 5^b
		final int sh = -t;
		final int blL = bitLength(s, 0, nM) + sh; // exact (left shift keeps all bits)
		if (blL > tab.bitLen[bExp]) return true;
		if (blL < tab.bitLen[bExp]) return false;
		final int offL = nM + tab.words[bExp] + 1;
		ensureCapacity(s, offL + nM + (sh >>> 6) + 2);
		final int nL = shiftLeft(s, 0, nM, sh, offL);
		return cmp(s, offL, nL, tab.values[bExp], 0, tab.words[bExp]) >= 0;
	}

	/** @return rounded quotient A/D, or -1 if the operands don't fit in 64 bits. */
	private static long fastDivide(final Parsed p, final long[] s, final int T, final int aExp, final int bExp) {
		if (p.bigWords != 0 || aExp > MAX_POW5_LONG || bExp > MAX_POW5_LONG) return -1;
		final long pow5a = POW5_LONG[aExp];
		if (p.mLong > Long.MAX_VALUE / pow5a) return -1;
		final long N = p.mLong * pow5a;
		if (T >= 0) {
			if (T >= 63 || N > Long.MAX_VALUE >> T) return -1;
			return divRound(N << T, POW5_LONG[bExp]);
		}
		final int sh = -T;
		final long pow5b = POW5_LONG[bExp];
		if (sh >= 64 || pow5b > Long.MAX_VALUE >> sh) return -1;
		return divRound(N, pow5b << sh);
	}

	/** A / D with round-to-nearest-even, both in [0, 2^63). */
	private static long divRound(final long A, final long D) {
		final long q = A / D;
		final long rem = A % D;
		final long half = D >>> 1;
		if (rem > half || (rem == half && (D & 1) == 0 && (q & 1) == 1)) return q + 1;
		return q;
	}

	/**
	 * Multi-precision evaluation of R = M * 5^E * 2^T, rounded to nearest with ties to even.
	 * The quotient is known to be &lt; 2^maxBits (maxBits = 53/52 for double, 24/23 for float).
	 */
	private static long slowDivide(final long[] s, final int nM, final int T, final int aExp, final int bExp, final int maxBits) {
		ensurePow5(Math.max(aExp, bExp));
		final Pow5Table tab = POW5_TABLE;
		// N = M * 5^aExp
		int offN, nN;
		if (aExp == 0) {
			offN = 0;
			nN = nM;
		} else {
			offN = nM + tab.words[aExp];
			ensureCapacity(s, offN + nM + tab.words[aExp]);
			nN = mul(s, 0, nM, tab.values[aExp], tab.words[aExp], offN);
		}
		// A = (T >= 0) ? N << T : N
		final int offA = offN + nN;
		final int nA;
		if (T >= 0) {
			ensureCapacity(s, offA + nN + 1);
			nA = shiftLeft(s, offN, nN, T, offA);
		} else {
			ensureCapacity(s, offA + nN);
			System.arraycopy(s, offN, s, offA, nN);
			nA = nN;
		}
		// D = 5^bExp << (-T if T < 0)
		final int shD = T < 0 ? -T : 0;
		final int offD = offA + nA;
		final int n5b = tab.words[bExp];
		ensureCapacity(s, offD + n5b + (shD >>> 6) + 2);
		final int nD = shiftLeft(tab.values[bExp], 0, n5b, shD, offD);

		// regions for remainder / temp / 2*rem
		final int nRmax = Math.max(nA, nD);
		final int offR = offD + nD;
		final int offT = offR + nRmax;
		final int off2R = offT + nD + 1;
		ensureCapacity(s, off2R + nA + 1);

		// q = floor(A / D) by binary search with bignum multiply+compare
		long q = 0;
		for (int bit = maxBits - 1; bit >= 0; bit--) {
			final long mid = q | (1L << bit);
			final int nT = mulLong(s, offD, nD, mid, offT);
			if (cmp(s, offT, nT, s, offA, nA) <= 0) q = mid;
		}
		// rem = A - q*D
		final int nT2 = mulLong(s, offD, nD, q, offT);
		sub(s, offA, nA, s, offT, nT2, offR);
		// round: 2*rem vs D (ties to even)
		final int n2R = shiftLeft(s, offR, nA, 1, off2R);
		final int c = cmp(s, off2R, n2R, s, offD, nD);
		if (c > 0 || (c == 0 && (q & 1) == 1)) q++;
		return q;
	}


	/** scratch buffer for the current thread. */
	private static long[] scratch() {
		return SCRATCH.get();
	}

	/** Grows the per-thread scratch to at least `needed` words and returns the (possibly new) array. */
	private static long[] ensureCapacity(long[] cur, final int needed) {
		if (needed <= cur.length) return cur;
		final long[] next = new long[Math.max(needed, cur.length * 2)];
		System.arraycopy(cur, 0, next, 0, cur.length);
		SCRATCH.set(next);
		return next;
	}

	/** Bit length of the big number s[off..off+n) (n >= 1, top word non-zero). */
	private static int bitLength(final long[] s, final int off, final int n) {
		return (n - 1) * 64 + 64 - Long.numberOfLeadingZeros(s[off + n - 1]);
	}

	/** Unsigned compare of two big numbers; regions may not overlap. */
	private static int cmp(final long[] a, final int offA, final int nA, final long[] b, final int offB, final int nB) {
		int la = nA, lb = nB;
		while (la > 1 && a[offA + la - 1] == 0) la--;
		while (lb > 1 && b[offB + lb - 1] == 0) lb--;
		if (la != lb) return la < lb ? -1 : 1;
		for (int i = la - 1; i >= 0; i--) {
			final int c = Long.compareUnsigned(a[offA + i], b[offB + i]);
			if (c != 0) return c;
		}
		return 0;
	}

	/** out = a * b at s[offOut..], returns word count. Regions must not overlap. */
	private static int mul(final long[] a, final int offA, final int nA, final long[] b, final int nB, final int offOut) {
		final long[] s = scratch();
		for (int i = 0; i < nA + nB; i++) s[offOut + i] = 0;
		for (int i = 0; i < nA; i++) {
			final long ai = a[offA + i];
			if (ai == 0) continue;
			long carry = 0;
			for (int j = 0; j <= nB; j++) {
				final long bj = j < nB ? b[j] : 0;
				final long lo = ai * bj;
				final long hi = mulHi(ai, bj);
				final int k = offOut + i + j;
				final long sum1 = s[k] + lo;
				final long c1 = Long.compareUnsigned(sum1, lo) < 0 ? 1L : 0L;
				final long sum2 = sum1 + carry;
				final long c2 = Long.compareUnsigned(sum2, sum1) < 0 ? 1L : 0L;
				s[k] = sum2;
				carry = hi + c1 + c2; // proven < 2^64 by the schoolbook carry invariant
			}
		}
		int n = nA + nB;
		while (n > 1 && s[offOut + n - 1] == 0) n--;
		return n;
	}

	/** out = d * q at s[offOut..], returns word count. */
	private static int mulLong(final long[] d, final int offD, final int nD, final long q, final int offOut) {
		final long[] s = scratch();
		if (q == 0) {
			s[offOut] = 0;
			return 1;
		}
		for (int i = 0; i <= nD; i++) s[offOut + i] = 0;
		long carry = 0;
		for (int j = 0; j <= nD; j++) {
			final long dj = j < nD ? d[offD + j] : 0;
			final long lo = dj * q;
			final long hi = mulHi(dj, q);
			final long sum1 = s[offOut + j] + lo;
			final long c1 = Long.compareUnsigned(sum1, lo) < 0 ? 1L : 0L;
			final long sum2 = sum1 + carry;
			final long c2 = Long.compareUnsigned(sum2, sum1) < 0 ? 1L : 0L;
			s[offOut + j] = sum2;
			carry = hi + c1 + c2; // proven < 2^64 by the schoolbook carry invariant
		}
		int n = nD + 1;
		while (n > 1 && s[offOut + n - 1] == 0) n--;
		return n;
	}

	/** out = a - b at s[offOut..] (a >= b), nA words written. */
	private static void sub(final long[] a, final int offA, final int nA, final long[] b, final int offB, final int nB, final int offOut) {
		final long[] s = scratch();
		long borrow = 0;
		for (int i = 0; i < nA; i++) {
			final long ai = a[offA + i];
			final long bi = i < nB ? b[offB + i] : 0;
			final long t = ai - bi;
			// unsigned borrow from (ai - bi) happens iff the wrapped result exceeds ai
			final long nb = Long.compareUnsigned(t, ai) > 0 ? 1L : 0L;
			final long v = t - borrow;
			s[offOut + i] = v;
			// underflow of (t - borrow) happens iff the wrapped result exceeds t
			borrow = nb + (Long.compareUnsigned(v, t) > 0 ? 1L : 0L);
		}
	}

	/** out = a << bits at s[offOut..], returns word count. Regions must not overlap. */
	private static int shiftLeft(final long[] a, final int offA, final int nA, final int bits, final int offOut) {
		final long[] s = scratch();
		if (bits == 0) {
			System.arraycopy(a, offA, s, offOut, nA);
			return nA;
		}
		final int wordShift = bits >>> 6;
		final int bitShift = bits & 63;
		for (int i = 0; i < nA + wordShift + 1; i++) s[offOut + i] = 0;
		for (int i = 0; i < nA; i++) {
			final long v = a[offA + i];
			if (v == 0) continue;
			final int k = offOut + i + wordShift;
			s[k] |= v << bitShift;
			if (bitShift != 0) s[k + 1] |= v >>> (64 - bitShift);
		}
		int n = nA + wordShift + 1;
		while (n > 1 && s[offOut + n - 1] == 0) n--;
		return n;
	}

	/** Unsigned high 64 bits of a * b. */
	private static long mulHi(final long a, final long b) {
		final long ah = a >>> 32;
		final long al = a & 0xFFFFFFFFL;
		final long bh = b >>> 32;
		final long bl = b & 0xFFFFFFFFL;
		final long p = al * bl;
		final long u = ah * bl;
		final long t = u + (p >>> 32);
		final long k1 = Long.compareUnsigned(t, u) < 0 ? 1L : 0L;
		final long m = t + (al * bh);
		final long k2 = Long.compareUnsigned(m, t) < 0 ? 1L : 0L;
		return ah * bh + (m >>> 32) + ((k1 + k2) << 32);
	}
}
