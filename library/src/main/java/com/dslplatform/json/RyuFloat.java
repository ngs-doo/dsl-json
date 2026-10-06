// Copyright 2018 Ulf Adams
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// Modified by Rikard Pavelic

package com.dslplatform.json;

import java.math.BigInteger;

final class RyuFloat {
  private static final int FLOAT_MANTISSA_BITS = 23;
  private static final int FLOAT_MANTISSA_MASK = (1 << FLOAT_MANTISSA_BITS) - 1;

  private static final int FLOAT_EXPONENT_BITS = 8;
  private static final int FLOAT_EXPONENT_MASK = (1 << FLOAT_EXPONENT_BITS) - 1;
  private static final int FLOAT_EXPONENT_BIAS = (1 << (FLOAT_EXPONENT_BITS - 1)) - 1;

  private static final long LOG10_2_DENOMINATOR = 10000000L;
  private static final long LOG10_2_NUMERATOR = (long) (LOG10_2_DENOMINATOR * Math.log10(2));

  private static final long LOG10_5_DENOMINATOR = 10000000L;
  private static final long LOG10_5_NUMERATOR = (long) (LOG10_5_DENOMINATOR * Math.log10(5));

  private static final long LOG2_5_DENOMINATOR = 10000000L;
  private static final long LOG2_5_NUMERATOR = (long) (LOG2_5_DENOMINATOR * (Math.log(5)/Math.log(2)));

  private static final int POS_TABLE_SIZE = 47;
  private static final int INV_TABLE_SIZE = 31;

  private static final int POW5_BITCOUNT = 61;
  private static final int POW5_HALF_BITCOUNT = 31;
  private static final int[][] POW5_SPLIT = new int[POS_TABLE_SIZE][2];

  private static final int POW5_INV_BITCOUNT = 59;
  private static final int POW5_INV_HALF_BITCOUNT = 31;
  private static final int[][] POW5_INV_SPLIT = new int[INV_TABLE_SIZE][2];

  static {
    BigInteger mask = BigInteger.valueOf(1).shiftLeft(POW5_HALF_BITCOUNT).subtract(BigInteger.ONE);
    BigInteger maskInv = BigInteger.valueOf(1).shiftLeft(POW5_INV_HALF_BITCOUNT).subtract(BigInteger.ONE);
    for (int i = 0; i < POW5_SPLIT.length; i++) {
      BigInteger pow = BigInteger.valueOf(5).pow(i);
      int pow5len = pow.bitLength();
      POW5_SPLIT[i][0] = pow.shiftRight(pow5len - POW5_BITCOUNT + POW5_HALF_BITCOUNT).intValueExact();
      POW5_SPLIT[i][1] = pow.shiftRight(pow5len - POW5_BITCOUNT).and(mask).intValueExact();

      if (i < POW5_INV_SPLIT.length) {
        int j = pow5len - 1 + POW5_INV_BITCOUNT;
        BigInteger inv = BigInteger.ONE.shiftLeft(j).divide(pow).add(BigInteger.ONE);
        POW5_INV_SPLIT[i][0] = inv.shiftRight(POW5_INV_HALF_BITCOUNT).intValueExact();
        POW5_INV_SPLIT[i][1] = inv.and(maskInv).intValueExact();
      }
    }
  }

  public static int writeFloat(final float value, final byte[] buffer, final int position) {
    // Step 1: Decode the floating point number, and unify normalized and subnormal cases.
    int bits = Float.floatToIntBits(value);
    if (bits == 0) {
      buffer[position] = '0';
      buffer[position + 1] = '.';
      buffer[position + 2] = '0';
      return 3;
    }
    if (bits == 0x80000000) {
      buffer[position] = '-';
      buffer[position + 1] = '0';
      buffer[position + 2] = '.';
      buffer[position + 3] = '0';
      return 4;
    }

    // Otherwise extract the mantissa and exponent bits and run the full algorithm.
    int ieeeExponent = (bits >> FLOAT_MANTISSA_BITS) & FLOAT_EXPONENT_MASK;
    int ieeeMantissa = bits & FLOAT_MANTISSA_MASK;
    // By default, the correct mantissa starts with a 1, except for denormal numbers.
    int e2;
    int m2;
    if (ieeeExponent == 0) {
      e2 = 1 - FLOAT_EXPONENT_BIAS - FLOAT_MANTISSA_BITS;
      m2 = ieeeMantissa;
    } else {
      e2 = ieeeExponent - FLOAT_EXPONENT_BIAS - FLOAT_MANTISSA_BITS;
      m2 = ieeeMantissa | (1 << FLOAT_MANTISSA_BITS);
    }

    boolean sign = bits < 0;
    // Step 2: Determine the interval of legal decimal representations.
    boolean even = (m2 & 1) == 0;
    int mv = 4 * m2;
    int mp = 4 * m2 + 2;
    int mm = 4 * m2 - ((m2 != (1L << FLOAT_MANTISSA_BITS)) || (ieeeExponent <= 1) ? 2 : 1);
    e2 -= 2;

    // Step 3: Convert to a decimal power base using 128-bit arithmetic.
    // -151 = 1 - 127 - 23 - 2 <= e_2 - 2 <= 254 - 127 - 23 - 2 = 102
    int dp, dv, dm;
    int e10;
    boolean dpIsTrailingZeros, dvIsTrailingZeros, dmIsTrailingZeros;
    int lastRemovedDigit = 0;
    if (e2 >= 0) {
      // Compute m * 2^e_2 / 10^q = m * 2^(e_2 - q) / 5^q
      int q = (int) (e2 * LOG10_2_NUMERATOR / LOG10_2_DENOMINATOR);
      int k = POW5_INV_BITCOUNT + pow5bits(q) - 1;
      int i = -e2 + q + k;
      dv = (int) mulPow5InvDivPow2(mv, q, i);
      dp = (int) mulPow5InvDivPow2(mp, q, i);
      dm = (int) mulPow5InvDivPow2(mm, q, i);
      if (q != 0 && ((dp - 1) / 10 <= dm / 10)) {
        // We need to know one removed digit even if we are not going to loop below. We could use
        // q = X - 1 above, except that would require 33 bits for the result, and we've found that
        // 32-bit arithmetic is faster even on 64-bit machines.
        int l = POW5_INV_BITCOUNT + pow5bits(q - 1) - 1;
        lastRemovedDigit = (int) (mulPow5InvDivPow2(mv, q - 1, -e2 + q - 1 + l) % 10);
      }
      e10 = q;
      dpIsTrailingZeros = pow5Factor(mp) >= q;
      dvIsTrailingZeros = pow5Factor(mv) >= q;
      dmIsTrailingZeros = pow5Factor(mm) >= q;
    } else {
      // Compute m * 5^(-e_2) / 10^q = m * 5^(-e_2 - q) / 2^q
      int q = (int) (-e2 * LOG10_5_NUMERATOR / LOG10_5_DENOMINATOR);
      int i = -e2 - q;
      int k = pow5bits(i) - POW5_BITCOUNT;
      int j = q - k;
      dv = (int) mulPow5divPow2(mv, i, j);
      dp = (int) mulPow5divPow2(mp, i, j);
      dm = (int) mulPow5divPow2(mm, i, j);
      if (q != 0 && ((dp - 1) / 10 <= dm / 10)) {
        j = q - 1 - (pow5bits(i + 1) - POW5_BITCOUNT);
        lastRemovedDigit = (int) (mulPow5divPow2(mv, i + 1, j) % 10);
      }
      e10 = q + e2; // Note: e2 and e10 are both negative here.
      dpIsTrailingZeros = 1 >= q;
      dvIsTrailingZeros = (q < FLOAT_MANTISSA_BITS + 3) && (mv & ((1 << (q - 1)) - 1)) == 0;
      dmIsTrailingZeros = (mm % 2 == 1 ? 0 : 1) >= q;
    }
    // Step 4: Find the shortest decimal representation in the interval of legal representations.
    //
    // We do some extra work here in order to follow Float/Double.toString semantics. In particular,
    // that requires printing in scientific format if and only if the exponent is between -3 and 7,
    // and it requires printing at least two decimal digits.
    //
    // Above, we moved the decimal dot all the way to the right, so now we need to count digits to
    // figure out the correct exponent for scientific notation.
    int dplength = decimalLength(dp);
    int exp = e10 + dplength - 1;

    // Float.toString semantics requires using scientific notation if and only if outside this range.
    boolean scientificNotation = !((exp >= -3) && (exp < 7));

    int removed = 0;
    if (dpIsTrailingZeros && !even) {
      dp--;
    }

    while (dp / 10 > dm / 10) {
      if ((dp < 100) && scientificNotation) {
        break;
      }
      dmIsTrailingZeros &= dm % 10 == 0;
      dp /= 10;
      lastRemovedDigit = dv % 10;
      dv /= 10;
      dm /= 10;
      removed++;
    }
    if (dmIsTrailingZeros && even) {
      while (dm % 10 == 0) {
        if ((dp < 100) && scientificNotation) {
          break;
        }
        dp /= 10;
        lastRemovedDigit = dv % 10;
        dv /= 10;
        dm /= 10;
        removed++;
      }
    }

    if (dvIsTrailingZeros && (lastRemovedDigit == 5) && (dv % 2 == 0)) {
      // Round down not up if the number ends in X50000 and the number is even.
      lastRemovedDigit = 4;
    }
    int output = dv +
        ((dv == dm && !(dmIsTrailingZeros && even)) || (lastRemovedDigit >= 5) ? 1 : 0);
    int olength = dplength - removed;

    // Step 5: Print the decimal representation.
    int index = position;
    if (sign) {
      buffer[index++] = '-';
    }

    if (scientificNotation) {
      for (int i = 0; i < olength - 1; i++) {
        int c = output % 10; output /= 10;
        buffer[index + olength - i] = (byte) ('0' + c);
      }
      buffer[index] = (byte) ('0' + output % 10);
      buffer[index + 1] = '.';
      index += olength + 1;
      if (olength == 1) {
        buffer[index++] = '0';
      }

      buffer[index++] = 'E';
      if (exp < 0) {
        buffer[index++] = '-';
        exp = -exp;
      }
      if (exp >= 10) {
        buffer[index++] = (byte) ('0' + exp / 10);
      }
      buffer[index++] = (byte) ('0' + exp % 10);
    } else {
      // Otherwise follow the Java spec for values in the interval [1E-3, 1E7).
      if (exp < 0) {
        buffer[index++] = '0';
        buffer[index++] = '.';
        for (int i = -1; i > exp; i--) {
          buffer[index++] = '0';
        }
        int current = index;
        for (int i = 0; i < olength; i++) {
          buffer[current + olength - i - 1] = (byte) ('0' + output % 10);
          output /= 10;
          index++;
        }
      } else if (exp + 1 >= olength) {
        for (int i = 0; i < olength; i++) {
          buffer[index + olength - i - 1] = (byte) ('0' + output % 10);
          output /= 10;
        }
        index += olength;
        for (int i = olength; i < exp + 1; i++) {
          buffer[index++] = '0';
        }
        buffer[index++] = '.';
        buffer[index++] = '0';
      } else {
        // Decimal dot is somewhere between the digits.
        int current = index + 1;
        for (int i = 0; i < olength; i++) {
          if (olength - i - 1 == exp) {
            buffer[current + olength - i - 1] = '.';
            current--;
          }
          buffer[current + olength - i - 1] = (byte) ('0' + output % 10);
          output /= 10;
        }
        index += olength + 1;
      }
    }
    return index - position;
  }

  private static int pow5bits(int e) {
    return e == 0 ? 1 : (int) ((e * LOG2_5_NUMERATOR + LOG2_5_DENOMINATOR - 1)/LOG2_5_DENOMINATOR);
  }

  /**
   * Returns the exponent of the largest power of 5 that divides the given value, i.e., returns
   * i such that value = 5^i * x, where x is an integer.
   */
  private static int pow5Factor(int value) {
    int count = 0;
    while (value > 0) {
      if (value % 5 != 0) {
        return count;
      }
      value /= 5;
      count++;
    }
    throw new IllegalArgumentException("invalid pow5factor value: " + value);
  }

  /**
   * Compute the exact result of [m * 5^(-e_2) / 10^q] = [m * 5^(-e_2 - q) / 2^q]
   * = [m * [5^(p - q)/2^k] / 2^(q - k)] = [m * POW5[i] / 2^j].
   */
  private static long mulPow5divPow2(int m, int i, int j) {
    if (j - POW5_HALF_BITCOUNT < 0) {
      throw new IllegalArgumentException("invalid shift in mulPow5divPow2: j=" + j);
    }
    long bits0 = m * (long) POW5_SPLIT[i][0];
    long bits1 = m * (long) POW5_SPLIT[i][1];
    return (bits0 + (bits1 >> POW5_HALF_BITCOUNT)) >> (j - POW5_HALF_BITCOUNT);
  }

  /**
   * Compute the exact result of [m * 2^p / 10^q] = [m * 2^(p - q) / 5 ^ q]
   * = [m * [2^k / 5^q] / 2^-(p - q - k)] = [m * POW5_INV[q] / 2^j].
   */
  private static long mulPow5InvDivPow2(int m, int q, int j) {
    if (j - POW5_INV_HALF_BITCOUNT < 0) {
      throw new IllegalArgumentException("invalid shift in mulPow5InvDivPow2: j=" + j);
    }
    long bits0 = m * (long) POW5_INV_SPLIT[q][0];
    long bits1 = m * (long) POW5_INV_SPLIT[q][1];
    return (bits0 + (bits1 >> POW5_INV_HALF_BITCOUNT)) >> (j - POW5_INV_HALF_BITCOUNT);
  }

  private static int decimalLength(int v) {
    if (v >= 1000000000L) return 10;
    if (v >= 100000000L) return 9;
    if (v >= 10000000L) return 8;
    if (v >= 1000000L) return 7;
    if (v >= 100000L) return 6;
    if (v >= 10000L) return 5;
    if (v >= 1000L) return 4;
    if (v >= 100L) return 3;
    if (v >= 10L) return 2;
    return 1;
  }
}
