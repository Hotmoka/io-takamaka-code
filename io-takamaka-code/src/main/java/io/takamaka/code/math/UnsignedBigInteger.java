/*
Copyright 2021 Fausto Spoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.takamaka.code.math;

import static io.takamaka.code.lang.Takamaka.require;

import java.math.BigInteger;

import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;

/**
 * This class represents an unsigned big integer.
 * It is a valid alternative to the {@code uint256} type that exists in Solidity.
 * It implements most common constructors of {@link java.math.BigInteger} and some useful functions.
 */
@Exported
public class UnsignedBigInteger extends Storage implements Comparable<UnsignedBigInteger>{

	/**
     * Stored value (guaranteed to be non-negative)
     */
    private final BigInteger value;

    /**
     * Creates an unsigned big integer from a non-negative {@link java.math.BigInteger}.
     *
     * @param value {@link java.math.BigInteger}
     */
    public UnsignedBigInteger(BigInteger value) {
        require(value.signum() >= 0, "Illegal value: negative value"); // valio.takamaka.code.lang.RequirementViolationException >= 0
        this.value = value;
    }

    /**
     * Creates 0 as an unsigned big integer.
     */
    public UnsignedBigInteger() {
        this.value = BigInteger.ZERO;
    }

    /**
     * Creates an unsigned big integer from an integer.
     *
     * @param value the integer (must be non-negative)
     */
    public UnsignedBigInteger(int value) {
        this(BigInteger.valueOf(value));
    }

    /**
     * Creates an unsigned big integer from a long integer.
     *
     * @param value the long integer (must be non-negative)
     */
    public UnsignedBigInteger(long value) {
        this(BigInteger.valueOf(value));
    }

    /**
     * Creates an unsigned big integer from a string.
     *
     * @param value the string, that must represent a non-negative integer
     */
    public UnsignedBigInteger(String value) {
        this(new BigInteger(value));
    }

    /**
     * Creates an unsigned big integer from a string and a radix.
     *
     * @param value the string, that must represent a non-negative integer in {@code radix} base
     * @param radix the coding base of {@code val}, such as 2 for binary
     */
    public UnsignedBigInteger(String value, int radix) {
        this(new BigInteger(value, radix));
    }

    /**
     * A constructor for internal use, that does not verify that {@code val} is non-negative.
     *
     * @param value the value to allocate (it is assumed that it is non-negative)
     * @param _dummy disambiguation parameter against the other public constructor
     */
    private UnsignedBigInteger(BigInteger value, boolean _dummy){
        this.value = value;
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} + {@code other}.
     *
     * @param other value that will be added to this unsigned big integer
     * @return the addition {@code this} + {@code other}
     */
    public UnsignedBigInteger add(UnsignedBigInteger other) {
        return new UnsignedBigInteger(value.add(other.value), true);
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} + 1.
     *
     * @return the addition {@code this} + 1
     */
    public UnsignedBigInteger next() {
    	return new UnsignedBigInteger(value.add(BigInteger.ONE), true);
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} - {@code other}.
     *
     * @param other value that will be subtracted from this unsigned big integer
     * @param errorMessage the message of the requirement exception generated if {@code other} is greater than {@code this}
     * @return the difference {@code this} - {@code other}
     * @throws io.takamaka.code.lang.RequirementViolationException if {@code other} is greater than {@code this}
     */
    public UnsignedBigInteger subtract(UnsignedBigInteger other, String errorMessage) {
        BigInteger diff = value.subtract(other.value);
        require(diff.signum() >= 0, errorMessage); // this.value >= other.value
        return new UnsignedBigInteger(diff, true);
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} - 1.
     *
     * @return the subtraction {@code this} - 1
     */
    public UnsignedBigInteger previous() {
    	return previous("Illegal operation: subtraction underflow");
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} - 1.
     *
     * @param errorMessage the message of the requirement exception generated if {@code this} is zero
     * @return the subtraction {@code this} - 1
     */
    public UnsignedBigInteger previous(String errorMessage) {
    	BigInteger diff = value.subtract(BigInteger.ONE);
        require(diff.signum() >= 0, errorMessage); // this.value >= 1
        return new UnsignedBigInteger(diff, true);
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} - {@code other}.
     *
     * @param other value that will be subtracted from this unsigned big integer
     * @return the difference {@code this} - {@code other}
     * @throws io.takamaka.code.lang.RequirementViolationException if {@code other} is greater than {@code this}
     */
    public UnsignedBigInteger subtract(UnsignedBigInteger other) {
        return subtract(other, "Illegal operation: subtraction underflow");
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} * {@code other}.
     *
     * @param other value that will be multiplied to this unsigned big integer
     * @return the multiplication {@code this} * {@code other}
     */
    public UnsignedBigInteger multiply(UnsignedBigInteger other) {
        return new UnsignedBigInteger(value.multiply(other.value), true);
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} / {@code other}.
     *
     * @param other the divisor
     * @param errorMessage the message of the requirement exception generated if {@code other} is zero
     * @return the division {@code this} / {@code other}
     * @throws io.takamaka.code.lang.RequirementViolationException if {@code other} is zero
     */
    public UnsignedBigInteger divide(UnsignedBigInteger other, String errorMessage) {
        require(other.value.signum() != 0, errorMessage); // other.val > 0
        return new UnsignedBigInteger(value.divide(other.value), true);
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} / {@code other}.
     *
     * @param other the divisor
     * @return the division {@code this} / {@code other}
     * @throws io.takamaka.code.lang.RequirementViolationException if {@code other} is zero
     */
    public UnsignedBigInteger divide(UnsignedBigInteger other) {
        return divide(other, "Illegal operation: division by zero");
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} mod {@code divisor} (integer remainder).
     *
     * @param divisor the divisor
     * @param errorMessage the message of the requirement exception generated if {@code divisor} is zero
     * @return the remainder {@code this} mod {@code divisor}
     * @throws io.takamaka.code.lang.RequirementViolationException if {@code divisor} is zero
     */
    public UnsignedBigInteger mod(UnsignedBigInteger divisor, String errorMessage) {
        require(divisor.value.signum() != 0, errorMessage); // other.val > 0
        return new UnsignedBigInteger(value.mod(divisor.value), true);
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} mod {@code divisor} (integer remainder).
     *
     * @param divisor the divisor
     * @return the remainder {@code this} mod {@code divisor}
     * @throws io.takamaka.code.lang.RequirementViolationException if {@code divisor} is zero
     */
    public UnsignedBigInteger mod(UnsignedBigInteger divisor) {
        return mod(divisor, "Illegal operation: modulo by zero");
    }

    /**
     * Returns an unsigned big integer whose value is {@code this} elevated to the power of {@code exponent}.
     * Note that the exponent is an integer rather than an unsigned big integer.
     *
     * @param exponent the exponent
     * @return the power
     */
    public UnsignedBigInteger pow(int exponent) {
        return new UnsignedBigInteger(value.pow(exponent), true);
    }

    /**
     * Returns the maximum between this unsigned big integer and another.
     *
     * @param other the other unsigned big integer
     * @return the maximum
     */
    public UnsignedBigInteger max(UnsignedBigInteger other) {
        return new UnsignedBigInteger(value.max(other.value), true);
    }

    /**
     * Returns the minimum between this unsigned big integer and another.
     *
     * @param other the other unsigned big integer
     * @return the minimum
     */
    public UnsignedBigInteger min(UnsignedBigInteger other) {
        return new UnsignedBigInteger(value.min(other.value), true);
    }

    /**
     * Compares this unsigned big integer with another.
     *
     * @param other the other unsigned big integer
     * @return negative, zero or positive as this is smaller, equal or larger than {@code other}
     */
    @Override
    public @View int compareTo(UnsignedBigInteger other) {
        return value.compareTo(other.value);
    }

    /**
     * Returns the signum function of this unsigned big integer.
     *
     * @return 0 or 1 as the value of this unsigned big integer is zero or positive
     */
    public int signum() {
    	return value.equals(BigInteger.ZERO) ? 0 : 1;
    }

    /**
     * Compares this unsigned big integer with the specified Object for equality.
     *
     * @param other the other object
     * @return true if and only if the specified object is an {@link UnsignedBigInteger} whose value is numerically equal to
     *         the value of this
     */
    @Override
    public @View boolean equals(Object other) {
        return other == this || (other instanceof UnsignedBigInteger && value.equals(((UnsignedBigInteger) other).value));
    }

    @Override
    public @View int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns the {@link java.math.BigInteger} corresponding to this unsigned big integer.
     * Note: it might be unsafe (make safe use of it).
     *
     * @return the {@link java.math.BigInteger}
     */
    public @View BigInteger toBigInteger() {
        return value;
    }

    /**
     * Returns the decimal string representation of this unsigned big integer.
     *
     * @return the representation
     */
    @Override
    public @View String toString() {
        return value.toString();
    }

    /**
     * Returns the string representation of this unsigned big integer in the given radix.
     *
     * @param radix the radix
     * @return the representation
     */
    public @View String toString(int radix) {
        return value.toString(radix);
    }

    /**
     * Returns an unsigned big integer whose value is equal to that of the given long value.
     *
     * @param value the long value
     * @return the unsigned big integer
     */
    public static UnsignedBigInteger valueOf(long value) {
        return new UnsignedBigInteger(BigInteger.valueOf(value)); //The constructor guarantees the check "value >= 0"
    }
}