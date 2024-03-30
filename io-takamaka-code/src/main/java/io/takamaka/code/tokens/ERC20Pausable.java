/*
Copyright 2021 Marco Crosara and Fausto Spoto

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

package io.takamaka.code.tokens;

import static io.takamaka.code.lang.Takamaka.require;

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Pausable;
import io.takamaka.code.lang.View;
import io.takamaka.code.math.UnsignedBigInteger;

/**
 * Implementation inspired by OpenZeppelin's <a href="https://github.com/OpenZeppelin/openzeppelin-contracts/blob/master/contracts/token/ERC20/ERC20Pausable.sol">ERC20Pausable.sol</a>.
 * Extension of {@link ERC20} which implements a mechanism to temporarily block all transfers on the contract, in case of necessity. See {@link Pausable}.
 *
 * Useful for scenarios such as preventing trades until the end of an evaluation period, or having an
 * emergency switch for freezing all token transfers in the event of a large bug.
 */
public abstract class ERC20Pausable extends ERC20 implements Pausable {

	/**
	 * True if and only if the token is paused.
	 */
    private boolean paused;

    /**
     * Sets the values for {@code name} and {@code symbol}, initializes {@code decimals} with a default
     * value of 18. To select a different value for {@code decimals}, use {@link ERC20#setDecimals(short)}.
     * The first two values are immutable: they can only be set once during construction.
     *
     * @param name the name of the token
     * @param symbol the symbol of the token
     */
    public ERC20Pausable(String name, String symbol) {
        super(name, symbol);
        paused = false;
    }

    @Override
    public final @View boolean paused() {
        return paused;
    }

    @Override
    public @FromContract void pause() {
        require(!paused, "the token is already paused");
        paused = true;
        event(new Paused(caller()));
    }

    @Override
    public @FromContract void unpause() {
        require(paused, "the token is not paused at the moment");
        paused = false;
        event(new Unpaused(caller()));
    }

    /**
     * See {@link ERC20#beforeTokenTransfer(Contract, Contract, UnsignedBigInteger)}.
     * Requirement: the contract must not be paused.
     *
     * @param from token transfer source account
     * @param to token transfer recipient account
     * @param amount amount of tokens transferred
     */
    @Override
    protected void beforeTokenTransfer(Contract from, Contract to, UnsignedBigInteger amount) {
    	require(!paused(), "ERC20Pausable: token transfer while paused");
    	super.beforeTokenTransfer(from, to, amount);
    }
}