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

package io.takamaka.code.dao;

import static io.takamaka.code.lang.Takamaka.require;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;

import io.takamaka.code.dao.SharedEntity.Offer;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Payable;
import io.takamaka.code.lang.PayableContract;
import io.takamaka.code.lang.StringSupport;
import io.takamaka.code.math.BigIntegerSupport;

/**
 * A shared entity where each shareholder cannot own more than a given percent of all shares.
 *
 * @param <S> the type of the shareholders
 * @param <O> the type of the offers of sale of shares for this entity
 */
public class SharedEntityWithCappedShares<S extends PayableContract, O extends Offer<S>> extends SimpleSharedEntity<S, O> {

	/**
     * The maximal percent of shares that a single shareholder can own.
     */
    private final int percentLimit;

    /**
     * The maximal number of shares that a single shareholder can own.
     */
    private final BigInteger limit;

    /**
	 * Creates a shared entity with the given set of shareholders and respective shares.
	 *
	 * @param shareholders the initial shareholders; if there are repetitions, their shares are merged
	 * @param shares the initial shares of each initial shareholder. This must be as many as {@code shareholders}
	 * @param percentLimit the maximal percent of shares that a single shareholder can own
	 */
	public SharedEntityWithCappedShares(S[] shareholders, BigInteger[] shares, int percentLimit) {
		super(shareholders, shares);

		this.percentLimit = percentLimit;
		require(percentLimit <= 100 && percentLimit > 0, "invalid share limit: it must be between 1 and 100 inclusive");
        BigInteger totalShares = getShares().values().reduce(ZERO, BigIntegerSupport::add);
        this.limit = BigIntegerSupport.divide(BigIntegerSupport.multiply(totalShares, BigInteger.valueOf(percentLimit)), BigInteger.valueOf(100));
        boolean sharesAreNotOverLimit = getShares().values().allMatch(_shares -> BigIntegerSupport.compareTo(_shares, limit) <= 0);
		require(sharesAreNotOverLimit, () -> StringSupport.concat("a shareholder cannot hold more than ", percentLimit, "% of shares"));
	}

    /**
     * Creates a shared entity with one shareholder.
     *
     * @param shareholder  the initial shareholder
     * @param share        the initial share of the initial shareholder
     * @param percentLimit the maximal percent of shares that a single shareholder can own
     */
	public SharedEntityWithCappedShares(S shareholder, BigInteger share, int percentLimit) {
    	super(shareholder, share);

		this.percentLimit = percentLimit;
		require(percentLimit <= 100 && percentLimit > 0, "invalid share limit: it must be between 1 and 100 inclusive");
        BigInteger totalShares = getShares().values().reduce(ZERO, BigIntegerSupport::add);
        this.limit = BigIntegerSupport.divide(BigIntegerSupport.multiply(totalShares, BigInteger.valueOf(percentLimit)), BigInteger.valueOf(100));
        boolean sharesAreNotOverLimit = getShares().values().allMatch(_shares -> BigIntegerSupport.compareTo(_shares, limit) <= 0);
		require(sharesAreNotOverLimit, () -> StringSupport.concat("a shareholder cannot hold more than ", percentLimit, "% of shares"));
    }

    /**
     * Creates a shared entity with two shareholders.
     *
     * @param shareholder1 the first initial shareholder
     * @param shareholder2 the second initial shareholder
     * @param share1       the initial share of the first shareholder
     * @param share2       the initial share of the second shareholder
     * @param percentLimit the maximal percent of shares that a single shareholder can own
     */
	public SharedEntityWithCappedShares(S shareholder1, S shareholder2, BigInteger share1, BigInteger share2, int percentLimit) {
    	super(shareholder1, shareholder2, share1, share2);

		this.percentLimit = percentLimit;
		require(percentLimit <= 100 && percentLimit > 0, "invalid share limit: it must be between 1 and 100 inclusive");
        BigInteger totalShares = getShares().values().reduce(ZERO, BigIntegerSupport::add);
        this.limit = BigIntegerSupport.divide(BigIntegerSupport.multiply(totalShares, BigInteger.valueOf(percentLimit)), BigInteger.valueOf(100));
        boolean sharesAreNotOverLimit = getShares().values().allMatch(_shares -> BigIntegerSupport.compareTo(_shares, limit) <= 0);
		require(sharesAreNotOverLimit, () -> StringSupport.concat("a shareholder cannot hold more than ", percentLimit, "% of shares"));
    }

    /**
     * Creates a shared entity with three shareholders.
     *
     * @param shareholder1 the first initial shareholder
     * @param shareholder2 the second initial shareholder
     * @param shareholder3 the third initial shareholder
     * @param share1       the initial share of the first shareholder
     * @param share2       the initial share of the second shareholder
     * @param share3       the initial share of the third shareholder
     * @param percentLimit the maximal percent of shares that a single shareholder can own
     */
	public SharedEntityWithCappedShares(S shareholder1, S shareholder2, S shareholder3, BigInteger share1, BigInteger share2, BigInteger share3, int percentLimit) {
    	super(shareholder1, shareholder2, shareholder3, share1, share2, share3);

		this.percentLimit = percentLimit;
		require(percentLimit <= 100 && percentLimit > 0, "invalid share limit: it must be between 1 and 100 inclusive");
        BigInteger totalShares = getShares().values().reduce(ZERO, BigIntegerSupport::add);
        this.limit = BigIntegerSupport.divide(BigIntegerSupport.multiply(totalShares, BigInteger.valueOf(percentLimit)), BigInteger.valueOf(100));
        boolean sharesAreNotOverLimit = getShares().values().allMatch(_shares -> BigIntegerSupport.compareTo(_shares, limit) <= 0);
		require(sharesAreNotOverLimit, () -> StringSupport.concat("a shareholder cannot hold more than ", percentLimit, "% of shares"));
    }

    /**
     * Creates a shared entity with four shareholders.
     *
     * @param shareholder1 the first initial shareholder
     * @param shareholder2 the second initial shareholder
     * @param shareholder3 the third initial shareholder
     * @param shareholder4 the fourth initial shareholder
     * @param share1       the initial share of the first shareholder
     * @param share2       the initial share of the second shareholder
     * @param share3       the initial share of the third shareholder
     * @param share4       the initial share of the fourth shareholder
     * @param percentLimit the maximal percent of shares that a single shareholder can own
     */
	public SharedEntityWithCappedShares(S shareholder1, S shareholder2, S shareholder3, S shareholder4, BigInteger share1, BigInteger share2, BigInteger share3, BigInteger share4, int percentLimit) {
    	super(shareholder1, shareholder2, shareholder3, shareholder4, share1, share2, share3, share4);

		this.percentLimit = percentLimit;
		require(percentLimit <= 100 && percentLimit > 0, "invalid share limit: it must be between 1 and 100 inclusive");
        BigInteger totalShares = getShares().values().reduce(ZERO, BigIntegerSupport::add);
        this.limit = BigIntegerSupport.divide(BigIntegerSupport.multiply(totalShares, BigInteger.valueOf(percentLimit)), BigInteger.valueOf(100));
        boolean sharesAreNotOverLimit = getShares().values().allMatch(_shares -> BigIntegerSupport.compareTo(_shares, limit) <= 0);
		require(sharesAreNotOverLimit, () -> StringSupport.concat("a shareholder cannot hold more than ", percentLimit, "% of shares"));
    }

    @Override
    public @FromContract(PayableContract.class) @Payable void accept(BigInteger amount, S buyer, O offer) {
        super.accept(amount, buyer, offer);
        require(BigIntegerSupport.compareTo(sharesOf(buyer), limit) <= 0, () -> StringSupport.concat("a shareholder cannot hold more than ", percentLimit, "% of shares"));
    }
}