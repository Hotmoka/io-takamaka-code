package io.takamaka.code.tokens;

import java.math.BigInteger;

/*
Copyright 2021 Filippo Fantinato and Fausto Spoto

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

import io.takamaka.code.lang.Contract;
import io.takamaka.code.lang.RequirementViolationException;
import io.takamaka.code.lang.View;

/**
 * The read operations on the balance and total supply of an ERC721 token.
 */
public interface IERC721View {

	/**
	 * Yields the balance of an owner ie, the number of tokens that it holds.
	 * 
	 * @param owner the owner
	 * @return the number of tokens owned by (@code owner)
	 */
	@View BigInteger balanceOf(Contract owner);

	/**
	 * Yields the owner of a given token.
	 * 
	 * @param tokenId the index of the token
	 * @return the owner of the token with index (@code tokenId)
	 * @throws RequirementViolationException if the token does not exist
	 */
	@View Contract ownerOf(BigInteger tokenId);

	/**
	 * Returns the number of owners of tokens. This includes also owners of
	 * zero tokens, but does not include contracts that have never been owners.
	 * 
	 * @return the number of owners of tokens
	 */
	@View int size();

	/**
	 * Yields the k-th owner of tokens.
	 * This is the k-th contract in the token table. 
	 *
	 * @param k the rank of the owner, between 0 and {@link #size()}
	 * @return the {@code k}-th owner of tokens
	 * @throws IllegalArgumentException if {@code k} is not between 0 and {@code size()-1}
	 */
	@View Contract select(int k);

	/**
	 * Yields an immutable view of this object.
	 * 
	 * @return the immutable view
	 */
	@View IERC721View snapshot();
}