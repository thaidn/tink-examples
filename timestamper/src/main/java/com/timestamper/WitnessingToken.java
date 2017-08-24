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
 * This class represents a (claimed) witnessing token. Our server builds them on request, and they
 * can also be checked (see TokenAttestation). This is meant to be serialized.
 */
class WitnessingToken implements ApiReplies {

  /** The encrypted token, base64-encoded. */
  String token;

  /**
   * Private constructor to avoid building invalid tokens. Users should go through buildNew to get
   * instances of this class.
   */
  private WitnessingToken(String token) {
    this.token = token;
  }

  /**
   * Create a new token for the given content, dated from now.
   *
   * @param content The content to build a token for.
   */
  static WitnessingToken buildNew(String content) {
    Date timestamp = new Date();
    try {
      // The token consists of the timestamp encrypted, and the content as the additionnal
      // authenticated data (AAD). This way, when checking a token, we can decrypt the timestamp,
      // and the token safely authenticates the content without being bloated by it.
      byte[] rawToken =
          CryptoService.encrypt(
              Utils.gson.toJson(timestamp).getBytes(), Utils.gson.toJson(content).getBytes());
      return new WitnessingToken(Base64.getEncoder().encodeToString(rawToken));
    } catch (GeneralSecurityException e) {
      // We should be able to encrypt: if we can't, it means something is seriously wrong, so we
      // just give up in that case.
      e.printStackTrace();
      throw new RuntimeException("Couldn't encrypt?");
    }
  }

  public String toJson() {
    return Utils.gson.toJson(this);
  }
}
