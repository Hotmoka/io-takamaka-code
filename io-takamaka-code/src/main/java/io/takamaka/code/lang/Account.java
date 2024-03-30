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

package io.takamaka.code.lang;

import java.math.BigInteger;

/**
 * A contract that can be used to pay for a transaction.
 */
public interface Account {

	/**
	 * Yields the current nonce of this account. If this account is used for paying
	 * a non-view transaction, the nonce in the request of the transaction must match
	 * this value, otherwise the transaction will be rejected.
	 * This value will be incremented at the end of any non-view transaction
	 * (also for unsuccessful transactions).
	 * 
	 * @return the current nonce of this account
	 */
	@View BigInteger nonce();

	/**
	 * Yields the balance of this account.
	 * 
	 * @return the balance
	 */
	@View BigInteger balance();

	/**
	 * Yields the <i>red</i> balance of this account.
	 * 
	 * @return the red balance
	 */
	@View BigInteger balanceRed();

	/**
	 * Yields the public key of this account.
	 * 
	 * @return the public key
	 */
	@View String publicKey();
}