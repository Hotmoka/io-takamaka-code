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

import io.takamaka.code.lang.FromContract;
import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.Event;
import io.takamaka.code.lang.View;
import io.takamaka.code.math.UnsignedBigInteger;

/**
 * Implementation inspired by OpenZeppelin's <a href="https://github.com/OpenZeppelin/openzeppelin-contracts/blob/master/contracts/token/ERC20/IERC20.sol">IERC20.sol</a>
 *
 * Interface of the ERC20 standard as defined in the EIP.
 */
public interface IERC20 extends IERC20View {

    /**
     * Moves {@code amount} tokens from the caller's account to {@code recipient}.
     * Returns a boolean value indicating whether the operation succeeded.
     * Emits a {@link IERC20.Transfer} event.
     *
     * @param recipient recipient of the transfer (this cannot be null)
     * @param amount number of tokens to transfer (this cannot be null)
     * @return true if the operation is successful
     */
    @FromContract boolean transfer(Contract recipient, UnsignedBigInteger amount);

    /**
     * Moves {@code amount} tokens from the caller's account to {@code recipient}.
     * Returns a boolean value indicating whether the operation succeeded.
     * Emits a {@link IERC20.Transfer} event.
     *
     * @param recipient recipient of the transfer (this cannot be null)
     * @param amount number of tokens to transfer (this cannot be negative)
     * @return true if the operation is successful
     */
    @FromContract boolean transfer(Contract recipient, int amount);

    /**
     * Moves {@code amount} tokens from the caller's account to {@code recipient}.
     * Returns a boolean value indicating whether the operation succeeded.
     * Emits a {@link IERC20.Transfer} event.
     *
     * @param recipient recipient of the transfer (this cannot be null)
     * @param amount number of tokens to transfer (this cannot be negative)
     * @return true if the operation is successful
     */
    @FromContract boolean transfer(Contract recipient, long amount);

    /**
     * Returns the remaining number of tokens that {@code spender} will be allowed to spend on behalf of
     * {@code owner} through {@link #transferFrom(Contract, Contract, UnsignedBigInteger)}. This is zero by default.
     * This value changes when {@link #approve(Contract, UnsignedBigInteger)}
     * or {@link #transferFrom(Contract, Contract, UnsignedBigInteger)} are called.
     *
     * @param owner account that allows {@code spender} to spend its tokens
     * @param spender account authorized to spend on behalf of {@code owner}
     * @return the remaining number of tokens that {@code spender} will be allowed to spend on behalf of {@code owner}
     */
    @View UnsignedBigInteger allowance(Contract owner, Contract spender);

    /**
     * Sets {@code amount} as the allowance of {@code spender} over the caller's tokens.
     * Returns a boolean value indicating whether the operation succeeded.
     *
     * IMPORTANT: Beware that changing an allowance with this method brings the risk that someone may use both the old
     * and the new allowance by unfortunate transaction ordering. One possible solution to mitigate this race condition
     * is to first <a href="https://github.com/ethereum/EIPs/issues/20#issuecomment-263524729">reduce the spender's allowance to 0 and set the desired value afterwards</a>.
     *
     * Emits an {@link IERC20.Approval} event.
     *
     * @param spender account authorized to spend on behalf of caller (it cannot be null)
     * @param amount amount of tokens that {@code spender} can spend on behalf of the caller (it cannot be null)
     * @return true if the operation is successful
     */
    @FromContract boolean approve(Contract spender, UnsignedBigInteger amount);

    /**
     * Moves {@code amount} tokens from {@code sender} to {@code recipient} using the allowance mechanism.
     * {@code amount} is then deducted from the caller's allowance.
     * Returns a boolean value indicating whether the operation succeeded.
     * Emits a {@link IERC20.Transfer} event.
     *
     * @param sender origin of the transfer (it cannot be null and must have a balance of at least {@code amount})
     * @param recipient recipient of the transfer (it cannot be null)
     * @param amount number of tokens to transfer (it cannot be null)
     * @return true if the operation is successful
     */
    @FromContract boolean transferFrom(Contract sender, Contract recipient, UnsignedBigInteger amount);

    /**
     * Yields an alias of this token, containing the same information but only with
     * read operations. Changes in this token reflect in changes in the resulting view.
     * 
     * @return the view
     */
    public IERC20View view();

    /**
     * Emitted when {@code value} tokens are moved from account {@code from} to another account {@code to}.
     * Note that {@code value} may be zero.
     */
    class Transfer extends Event {
    	
    	/**
    	 * The origin of the tokens.
    	 */
        public final Contract from;

        /**
         * The destination of the tokens.
         */
        public final Contract to;

        /**
         * The amount of transferred tokens.
         */
        public final UnsignedBigInteger value;

        /**
         * Creates the event object.
         *
         * @param from origin of the tokens transfer
         * @param to recipient of the tokens transfer
         * @param value number of tokens that have been transferred from {@code from} to {@code to}
         */
        @FromContract Transfer(Contract from, Contract to, UnsignedBigInteger value) {
            this.from = from;
            this.to = to;
            this.value = value;
        }
    }

    /**
     * Emitted when the allowance of a {@code spender} for an {@code owner} is set by a call to
     * {@link IERC20#approve(Contract, UnsignedBigInteger)}. {@code value} is the new allowance.
     */
    class Approval extends Event {

    	/**
    	 * The owner.
    	 */
    	public final Contract owner;

    	/**
    	 * The spender.
    	 */
    	public final Contract spender;

    	/**
    	 * The new allowance.
    	 */
    	public final UnsignedBigInteger value;

        /**
         * Creates the event object.
         *
         * @param owner account that authorizes to spend
         * @param spender account authorized to spend on behalf of {@code owner}
         * @param value amount of tokens that {@code spender} can spend on behalf of {@code owner}
         */
        @FromContract Approval(Contract owner, Contract spender, UnsignedBigInteger value) {
            this.owner = owner;
            this.spender = spender;
            this.value = value;
        }
    }
}