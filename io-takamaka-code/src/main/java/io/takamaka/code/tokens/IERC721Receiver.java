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

/**
 * Contracts that implement this interface can be used for safe transfers of tokens.
 * The rationale is that they are aware of the ERC721 protocol, to prevent tokens from being forever locked.
 */
public interface IERC721Receiver {

	/**
     * Callback invoked when a token gets safely transferred to this receiver.
     * 
     * @param ledger the ERC721 ledger where the token belongs to
     * @param operator the operator that performs the transfer. This is {@code null}
     *                 if the transfer occurs because the token has been freshly minted for {@code to}
     * @param from the contract owning the token
     * @param tokenId the identifier of the token being transferred
     */
	void onReceive(IERC721 ledger, Contract operator, Contract from, BigInteger tokenId);
}