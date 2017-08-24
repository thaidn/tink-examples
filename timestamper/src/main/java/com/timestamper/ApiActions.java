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

/**
 * An interface for the API endpoints that can be called. Each implementing class contains the data
 * they expect from the query: this lets us serialize and deserialize API calls easily.
 */
interface ApiActions {

  /** Return the right type for your API response there. */
  ApiReplies execute();

  /** The client wants to build a token. We'll return a WitnessingToken. */
  static class CreateNewToken implements ApiActions {
    String content;

    CreateNewToken(String content) {
      this.content = content;
    }

    static CreateNewToken fromJson(String json) {
      return Utils.gson.fromJson(json, CreateNewToken.class);
    }

    public WitnessingToken execute() {
      return WitnessingToken.buildNew(content);
    }
  }

  /** The client wants to verify a token. We'll return a TokenAttestation. */
  static class AttestTokenIsValid implements ApiActions {
    String content;
    WitnessingToken token;

    AttestTokenIsValid(String content, WitnessingToken token) {
      this.content = content;
      this.token = token;
    }

    static AttestTokenIsValid fromJson(String json) {
      return Utils.gson.fromJson(json, AttestTokenIsValid.class);
    }

    public TokenAttestation execute() {
      return TokenAttestation.checkClaim(token, content);
    }
  }

  /** The client wants to rotate the current key. Returns a boolean. */
  static class RotateKey implements ApiActions {
    boolean keepOlderKeysAsSecondary;

    RotateKey(boolean keepOlderKeysAsSecondary) {
      this.keepOlderKeysAsSecondary = keepOlderKeysAsSecondary;
    }

    static RotateKey fromJson(String json) {
      return Utils.gson.fromJson(json, RotateKey.class);
    }

    public ApiReplies.BooleanApiReply execute() {
      return CryptoService.rotate(keepOlderKeysAsSecondary);
    }
  }
}
