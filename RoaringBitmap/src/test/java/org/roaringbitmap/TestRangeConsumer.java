package org.roaringbitmap;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class TestRangeConsumer implements RelativeRangeConsumer {

  public enum Value {
    UNINITIALISED,
    ABSENT,
    PRESENT
  }

  private Value[] buffer;
  final boolean validateBuffer;

  private TestRangeConsumer(Value[] buffer, boolean validateBuffer) {
    this.buffer = buffer;
    this.validateBuffer = validateBuffer;
  }

  public static TestRangeConsumer ofSize(int size) {
    Value[] buffer = new Value[size];
    Arrays.fill(buffer, Value.UNINITIALISED);
    return new TestRangeConsumer(buffer, false);
  }

  public static TestRangeConsumer validate(Value[] buffer) {
    for (Value b : buffer) {
      assertNotEquals(Value.UNINITIALISED, b, "Provide only fully initialised buffers!");
    }
    return new TestRangeConsumer(buffer, true);
  }

  public Value[] getBuffer() {
    return this.buffer;
  }

  @Override
  public void acceptPresent(final int relativePos) {
    if (validateBuffer) {
      assertEquals(buffer[relativePos], Value.PRESENT, () -> "Mismatch at position " + relativePos);
    } else {
      buffer[relativePos] = Value.PRESENT;
    }
  }

  @Override
  public void acceptAbsent(final int relativePos) {
    if (validateBuffer) {
      assertEquals(buffer[relativePos], Value.ABSENT, () -> "Mismatch at position " + relativePos);
    } else {
      buffer[relativePos] = Value.ABSENT;
    }
  }

  @Override
  public void acceptAllPresent(int relativeFrom, int relativeTo) {
    if (validateBuffer) {
      for (int i = relativeFrom; i < relativeTo; i++) {
        final int finalI = i;
        assertEquals(buffer[i], Value.PRESENT, () -> "Mismatch at position " + finalI);
      }
    } else {
      Arrays.fill(buffer, relativeFrom, relativeTo, Value.PRESENT);
    }
  }

  @Override
  public void acceptAllAbsent(int relativeFrom, int relativeTo) {
    if (validateBuffer) {
      for (int i = relativeFrom; i < relativeTo; i++) {
        final int finalI = i;
        assertEquals(buffer[i], Value.ABSENT, () -> "Mismatch at position " + finalI);
      }
    } else {
      Arrays.fill(buffer, relativeFrom, relativeTo, Value.ABSENT);
    }
  }

  public void assertAllAbsentExcept(char[] presentValues, int offset) {
    int[] shifted = new int[presentValues.length];
    for (int i = 0; i < presentValues.length; i++) {
      shifted[i] = ((int) presentValues[i]) + offset;
    }
    assertAllAbsentExcept(shifted);
  }

  public void assertAllAbsentExcept(int[] presentValues) {
    if (presentValues.length == 0) {
      assertAllAbsent();
      return;
    }
    int expectedValueIndex = 0;
    for (int i = 0; i < buffer.length; i++) {
      final int finalI = i;
      if (expectedValueIndex < presentValues.length) {
        int expectedValue = presentValues[expectedValueIndex];
        if (i != expectedValue) {
          assertEquals(Value.ABSENT, buffer[i], () -> "Mismatch at position " + finalI);
        } else {
          assertEquals(Value.PRESENT, buffer[i], () -> "Mismatch at position " + finalI);
          expectedValueIndex++;
        }
      } else {
        assertEquals(Value.ABSENT, buffer[i], () -> "Mismatch at position " + finalI);
      }
    }
    assertEquals(presentValues.length, expectedValueIndex);
  }

  public void assertAllAbsent() {
    for (int i = 0; i < buffer.length; i++) {
      final int finalI = i;
      assertEquals(Value.ABSENT, buffer[i], () -> "Mismatch at position " + finalI);
    }
  }
}
