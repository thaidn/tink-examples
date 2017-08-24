// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// //////////////////////////////////////////////////////////////////////////////

package com.timestamper;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Date;

/**
 * This class represents the response to a verification request of a (document,token) pair by a
 * client. This is meant to be serialized.
 */
class TokenAttestation implements ApiReplies {

  /** Did this token verify successfully ? */
  boolean attested;
  /** When was this token created ? */
  Date timestamp;

  /**
   * Private constructor to avoid building invalid attestations. Users should go through checkClaim
   * to get instances of this class.
   */
  private TokenAttestation(boolean attested, Date timestamp) {
    this.timestamp = timestamp;
    this.attested = attested;
  }

  /**
   * Check the given token against the provided content. Creates a TokenAttestation that contains
   * the token creation date if valid.
   */
  static TokenAttestation checkClaim(WitnessingToken claimedToken, String claimedContent) {
    byte[] claimedSerializedPlaintext = Utils.gson.toJson(claimedContent).getBytes();
    byte[] rawToken = Base64.getDecoder().decode(claimedToken.token.getBytes());

    try {
      // Try to decrypt the token, using the claimed content as AAD.
      byte[] decryptedToken = CryptoService.decrypt(rawToken, claimedSerializedPlaintext);
      // Decryption is successfull: plaintext is a JSON serialization of the creation timestamp.
      Date decryptedDate = Utils.gson.fromJson(new String(decryptedToken), Date.class);
      // If we come here, the token is considered valid.
      return new TokenAttestation(true, decryptedDate);
    } catch (GeneralSecurityException e) {
      // Exceptions are thrown when decryption fails. This could be due to a wrong key, malformed
      // tokens, or just trying to validate a token against the wrong content. In these cases, we
      // just reply that verification failed.
      e.printStackTrace();
      return new TokenAttestation(false, null);
    }
  }

  public String toJson() {
    return Utils.gson.toJson(this);
  }
}
