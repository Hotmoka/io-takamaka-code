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

package io.takamaka.code.governance;

import static io.takamaka.code.lang.Takamaka.require;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;

import io.takamaka.code.dao.Poll;
import io.takamaka.code.dao.PollWithTimeWindow;
import io.takamaka.code.dao.SharedEntity.Offer;
import io.takamaka.code.dao.SimplePoll;
import io.takamaka.code.dao.SimpleSharedEntity;
import io.takamaka.code.lang.Account;
import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Payable;
import io.takamaka.code.lang.PayableContract;
import io.takamaka.code.lang.StringSupport;
import io.takamaka.code.lang.View;
import io.takamaka.code.math.BigIntegerSupport;
import io.takamaka.code.util.SnapshottableStorageMap;
import io.takamaka.code.util.SnapshottableStorageSet;
import io.takamaka.code.util.SnapshottableStorageTreeMap;
import io.takamaka.code.util.SnapshottableStorageTreeSet;
import io.takamaka.code.util.StorageSetView;

/**
 * A partial implementation of the validators.
 * 
 * @param <V> the type of the validator contracts
 */
public abstract class AbstractValidators<V extends Validator> extends SimpleSharedEntity<V, Offer<V>> implements Validators<V> {

	/**
	 * The manifest of the node having these validators.
	 */
	private final Manifest<V> manifest;

	/**
	 * The earnings of each validators, that have not yet been sent to the validators.
	 * They are not given immediately to the validators,
	 * but rather stored in this map and given only if a validator sells all its shares.
	 */
	private final SnapshottableStorageMap<V, BigInteger> stakes = new SnapshottableStorageTreeMap<>();

	/**
	 * The amount of coins to pay for starting a new poll among the validators.
	 */
	private final BigInteger ticketForNewPoll;

	/**
	 * The initial circulating supply of coins in the node.
	 */
	private final BigInteger initialSupply;

	/**
	 * The current circulating supply of coins in the node. This increases
	 * with time if inflation is not zero, since the gas used for the transactions
	 * gets inflated by inflation and distributed to the validators. This is
	 * between {@link #initialSupply} and {@link #finalSupply}.
	 */
	private BigInteger currentSupply;

	/**
	 * The final circulating supply of coins in the node, that will be reached
	 * eventually, if inflation is not zero.
	 */
	private final BigInteger finalSupply;

	/**
	 * The height after which coins are not minted anymore and the current
	 * supply reaches the final supply.
	 */
	private final BigInteger heightAtFinalSupply;

	/**
	 * The number of transactions validated up to now.
	 * Note that this is updated at each reward.
	 */
	private BigInteger numberOfTransactions;

	/**
	 * The number of rewards that have been sent to the validators.
	 * If the node is a blockchain, this is typically the height of the blockchain.
	 */
	private BigInteger height;

	/**
	 * The polls created among the validators of this manifest, that have not been closed yet.
	 * Some of these polls might be over.
	 */
	private final SnapshottableStorageSet<Poll<V>> polls = new SnapshottableStorageTreeSet<>();

	/**
	 * A snapshot of the current value of {@link #polls}.
	 */
	private StorageSetView<Poll<V>> snapshotOfPolls;

	/**
	 * A numerical constant, useful for percent calculations or gas limit.
	 */
	protected final BigInteger _100_000_000 = BigInteger.valueOf(100_000_000L);

	/**
	 * Creates the validators initialized with the given accounts.
	 * 
	 * @param manifest the manifest of the node
	 * @param validators the initial accounts
	 * @param powers the initial powers of the initial accounts; each refers
	 *               to the corresponding element of {@code validators}, hence
	 *               {@code validators} and {powers} have the same length
	 * @param ticketForNewPoll the amount of coins to pay for starting a new poll among the validators;
	 *                         both {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action)} and
	 *                         {@link #newPoll(BigInteger, io.takamaka.code.dao.SimplePoll.Action, long, long)}
	 *                         require to pay this amount for starting a poll
	 * @param finalSupply the final supply of coins that will be reached, eventually
	 * @param heightAtFinalSupply the height after which coins are not minted anymore and the current
	 *                            supply reaches the final supply
	 */
	protected AbstractValidators(Manifest<V> manifest, V[] validators, BigInteger[] powers, BigInteger ticketForNewPoll, BigInteger finalSupply, BigInteger heightAtFinalSupply) {

		super(validators, powers);

		require(ticketForNewPoll != null, "the ticket for new poll must be non-null");
		require(ticketForNewPoll.signum() >= 0, "the ticket for new poll must be non-negative");

		this.manifest = manifest;
		Account gamete = manifest.getGamete();
		this.currentSupply = gamete.balance(); // initially, all coins are inside the gamete
		this.initialSupply = currentSupply;
		this.finalSupply = finalSupply;
		this.heightAtFinalSupply = heightAtFinalSupply;
		this.ticketForNewPoll = ticketForNewPoll;
		this.numberOfTransactions = ZERO;
		this.height = ZERO;
		this.snapshotOfPolls = polls.snapshot();

		for (V validator: validators)
			stakes.put(validator, BigInteger.ZERO);
	}

	@Override
	public final @View BigInteger getStake(V validator) {
		return stakes.getOrDefault(validator, BigInteger.ZERO);
	}

	@Override
	public final @View BigInteger getInitialSupply() {
		return initialSupply;
	}

	@Override
	public final @View BigInteger getCurrentSupply() {
		return currentSupply;
	}

	@Override
	public final @View BigInteger getFinalSupply() {
		return finalSupply;
	}

	@Override
	public final @View BigInteger getHeightAtFinalSupply() {
		return heightAtFinalSupply;
	}

	@Override
	public final @View BigInteger getTicketForNewPoll() {
		return ticketForNewPoll;
	}

	@Override
	public final @View StorageSetView<Poll<V>> getPolls() {
		return snapshotOfPolls;
	}

	@Override
	public final @View BigInteger getHeight() {
		return height;
	}

	@Override
	public final @View BigInteger getNumberOfTransactions() {
		return numberOfTransactions;
	}

	/**
	 * Place a shares sale offer for this entity. This method checks
	 * the offer, adds it to the current offers and issues an event.
	 * 
	 * @param amount the ticket payed to place the offer; implementations may allow zero for this
	 * @param seller the seller of the shares; this must coincide with the caller of the constructor
	 * @param sharesOnSale the shares on sale, positive
	 * @param cost the cost, non-negative
	 * @param duration the duration of validity of the offer, in milliseconds from now, always non-negative
	 * @return the offer that has been placed
	 */
	public @FromContract(PayableContract.class) @Payable Offer<V> place(BigInteger amount, V seller, BigInteger sharesOnSale, BigInteger cost, long duration) {
		var offer = new Offer<>(seller, sharesOnSale, cost, duration);
    	place(amount, offer);
    	return offer;
	}

	/**
	 * Place a shares sale offer for this entity. This method checks
	 * the offer, adds it to the current offers and issues an event.
	 * 
	 * @param amount the ticket payed to place the offer; implementations may allow zero for this
	 * @param seller the seller of the shares; this must coincide with the caller of the constructor
	 * @param sharesOnSale the shares on sale, positive
	 * @param cost the cost, non-negative
	 * @param duration the duration of validity of the offer, in milliseconds from now, always non-negative
	 * @param buyer the only buyer allowed for this offer
	 * @return the offer that has been placed
	 */
	public @FromContract(PayableContract.class) @Payable Offer<V> place(BigInteger amount, V seller, BigInteger sharesOnSale, BigInteger cost, long duration, V buyer) {
		var offer = new Offer<>(seller, sharesOnSale, cost, duration, buyer);
		place(amount, offer);
		return offer;
	}

	@Override
	@Payable @FromContract
	public final SimplePoll<V> newPoll(BigInteger amount, SimplePoll.Action action) {
		require(BigIntegerSupport.compareTo(amount, ticketForNewPoll) >= 0, () -> StringSupport.concat("a new poll costs ", ticketForNewPoll, " coins"));
		checkThatItCanStartPoll(caller());
	
		var poll = new SimplePoll<V>(this, action) {
	
			@Override
			public void close() {
				super.close();
				removePoll(this);
			}
		};
	
		addPoll(poll);
	
		return poll;
	}

	@Override
	@Payable @FromContract
	public final PollWithTimeWindow<V> newPoll(BigInteger amount, SimplePoll.Action action, long start, long duration) {
		require(BigIntegerSupport.compareTo(amount, ticketForNewPoll) >= 0, () -> StringSupport.concat("a new poll costs ", ticketForNewPoll, " coins"));
		checkThatItCanStartPoll(caller());
	
		var poll = new PollWithTimeWindow<V>(this, action, start, duration) {
	
			@Override
			public void close() {
				super.close();
				removePoll(this);
			}
		};
	
		addPoll(poll);
	
		return poll;
	}

	/**
	 * Transforms a string of powers into an array of big integers.
	 * 
	 * @param powersAsStringSequence the string
	 * @return the array
	 */
	protected static BigInteger[] buildPowers(String powersAsStringSequence) {
		String[] array = splitAtSpaces(powersAsStringSequence);
		var result = new BigInteger[array.length];
		int pos = 0;
		for (String s: array)
			result[pos++] = BigIntegerSupport.from(s);

		return result;
	}

	/**
	 * Slits the given string at spaces.
	 * 
	 * @param s the string
	 * @return the array of parts
	 */
	protected static String[] splitAtSpaces(String s) {
		int counter = s.isEmpty() ? 0 : 1;
		for (int i = 0; i < s.length() - 1; i++)
			if (s.charAt(i) == ' ')
				counter++;

		var result = new String[counter];
		int pos;
		int i = 0;
		while ((pos = StringSupport.indexOf(s, ' ')) >= 0) {
			result[i++] = StringSupport.substring(s, 0, pos);
			s = StringSupport.substring(s, pos + 1);
		}

		if (!s.isEmpty())
			result[i++] = s;

		return result;
	}

	/**
	 * Yields the stakes of the validators.
	 * 
	 * @return the stakes of the validators
	 */
	protected SnapshottableStorageMap<V, BigInteger> getStakes() {
		return stakes;
	}

	/**
	 * Yields the manifest of these validators object.
	 * 
	 * @return the manifest of these validators object
	 */
	protected Manifest<V> getManifest() {
		return manifest;
	}

	/**
	 * Updates the parameters of the chain, as consequence of new validations.
	 * 
	 * @param minted the coins minted in the last validation
	 * @param numberOfTransactionsSinceLastReward the number of transactions in the last validation
	 */
	protected void updateParameters(BigInteger minted, BigInteger numberOfTransactionsSinceLastReward) {
		// we increase the number of rewards (ie, the height of the blockchain, if the node is part of a blockchain)
		height = BigIntegerSupport.add(height, ONE);

		// we add to the cumulative number of transactions validated up to now
		numberOfTransactions = BigIntegerSupport.add(numberOfTransactions, numberOfTransactionsSinceLastReward);

		// the total supply is increased by the coins minted since the previous reward
		currentSupply = BigIntegerSupport.add(currentSupply, minted);
	}

	/**
	 * Updates the gas price in the gas station connected to the manifest of this validators object.
	 * 
	 * @param gasConsumed the gas consumed in the last validation
	 */
	protected void updateGasPrice(BigInteger gasConsumed) {
		// the gas station is informed about the amount of gas consumed for CPU, RAM or storage, so that it can update the gas price
		manifest.gasStation.takeNoteOfGasConsumedDuringLastReward(gasConsumed);
	}

	private void addPoll(SimplePoll<V> poll) {
		polls.add(poll);
		snapshotOfPolls = polls.snapshot();
	}

	private void removePoll(SimplePoll<V> poll) {
		polls.remove(poll);
		snapshotOfPolls = polls.snapshot();
	}

	private void checkThatItCanStartPoll(Contract caller) {
		require(isShareholder(caller) || caller == manifest || caller == manifest.versions || caller == manifest.gasStation,
			"only a validator or the same manifest can start a poll among the validators");
	}
}