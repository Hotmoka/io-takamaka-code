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
import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.Exported;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Storage;
import io.takamaka.code.lang.View;
import io.takamaka.code.util.StorageMap;
import io.takamaka.code.util.StorageMapView;
import io.takamaka.code.util.StorageMapView.Entry;
import io.takamaka.code.util.StorageTreeMap;

/**
 * The implementation of a simple poll among a set of voters. Each voter can vote with a number of votes
 * between one and its power. Once the goal for the poll is reached, an action is run.
 * 
 * @param <Voter> the type of the contracts allowed to vote
 */
@Exported
public class SimplePoll<Voter extends Contract> extends Storage implements Poll<Voter> {

	/**
	 * An action that is triggered if the goal of the poll has been reached.
	 */
	@Exported
	public static abstract class Action extends Storage {

		/**
		 * Creates the action.
		 */
		protected Action() {}

		/**
		 * Yields the description of the aim of the poll.
		 * 
		 * @return the description
		 */
		public abstract @View String getDescription();

		/**
		 * The action to run when the goal of the poll has been reached.
		 */
		protected abstract void run();
	}

	/** 
	 * The eligible voters, with the maximal amount of votes they can cast.
	 */
	private final StorageMapView<Voter, BigInteger> eligibleVoters;

	/**
	 * The voters up to now, with the votes that each of them has cast.
	 */
	private final StorageMap<Voter, BigInteger> votersUpToNow = new StorageTreeMap<>();

	/**
	 * The action run if the goal of the poll is reached.
	 */
	private final Action action;

	/**
	 * The maximal amount of votes that can be cast for this poll.
	 */
	private final BigInteger totalVotesCastable;

	/**
	 * A snapshot of the current {@link #votersUpToNow}.
	 */
	private StorageMapView<Voter, BigInteger> snapshotOfVotersUpToNow;

	/**
	 * The votes cast up to now.
	 */
	private BigInteger votesCastUpToNow;

	/**
	 * True if and only if this poll has been closed.
	 */
	private boolean isClosed;

	/**
	 * Creates a simple poll among a set of eligible voters.
	 * 
	 * @param eligibleVoters the eligible voters, with their associated power
	 * @param action the action to run when the poll is closed and the goal has been reached
	 */
	public SimplePoll(SharedEntityView<Voter> eligibleVoters, Action action) {
		require(eligibleVoters != null, "the shareholders cannot be null");
		require(action != null, "the action cannot be null");

		this.eligibleVoters = eligibleVoters.getShares();
		this.totalVotesCastable = this.eligibleVoters.stream().map(Entry::getValue).reduce(ZERO, BigInteger::add);
		this.action = action;
		this.snapshotOfVotersUpToNow = votersUpToNow.snapshot();
		this.votesCastUpToNow = ZERO;
	}

	@Override @View
	public final String getDescription() {
		return action.getDescription();
	}

	@Override @View
	public final StorageMapView<Voter, BigInteger> getEligibleVoters() {
		return eligibleVoters;
	}

	@Override @View
	public final StorageMapView<Voter, BigInteger> getVotersUpToNow() {
		return snapshotOfVotersUpToNow;
	}

	@Override @View
	public final BigInteger getTotalVotesCastable() {
		return totalVotesCastable;
	}

	@Override @View
	public final BigInteger getVotesCastUpToNow() {
		return votesCastUpToNow;
	}

	@Override @FromContract
	public final void vote() {
		vote(eligibleVoters.get(caller()));
	}
	
	@SuppressWarnings("unchecked")
	@Override @FromContract
	public final void vote(BigInteger votes) {
		Contract caller = caller();
		checkIfCanVote(caller, votes);
		// this unsafe cast will always succeed, since checkIfCanVote has verified that the caller is an eligible voter
		votersUpToNow.put((Voter) caller, votes);
		votesCastUpToNow = votesCastUpToNow.add(votes);
		snapshotOfVotersUpToNow = votersUpToNow.snapshot();
	}

	@Override
	public void close() {
		require(!isClosed, "the poll is already closed");
		require(isOver(), "the poll is not over");
		if (goalReached())
			action.run();

		isClosed = true;
	}

	@Override
	@View
	public boolean isOver() {
		return goalReached() || votersUpToNow.size() == eligibleVoters.size();
	}

	/**
	 * Determines if a voter can cast the given amount of votes. It must be
	 * an eligible voter, must not have voted already and must cast a vote in its allowed range.
	 * 
	 * @param voter the voter
	 * @param votes the amount of votes
	 */
	protected void checkIfCanVote(Contract voter, BigInteger votes) {
		BigInteger max = eligibleVoters.get(voter);
		require(max != null, "you are not a shareholder");
		require(!votersUpToNow.containsKey(voter), "you have already voted");
		require(votes != null && votes.signum() >= 0 && votes.compareTo(max) <= 0, () -> "you are only allowed to cast between 0 and " + max + "votes, inclusive");
	}

	/**
	 * Determines if the goal has been reached. By default, this means that
	 * at least 50%+1 of the eligible votes have been cast for this poll. Subclasses may redefine.
	 * 
	 * @return true if and only if the goal has been reached
	 */
	protected boolean goalReached() {
		return totalVotesCastable.compareTo(votesCastUpToNow.multiply(TWO)) < 0;
	}
}