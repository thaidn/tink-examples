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

/** Interface that covers all the API replies, which are meant to be serialized. */
interface ApiReplies {
  /** Returns a JSON serialization of this object. */
  String toJson();

  static class BooleanApiReply implements ApiReplies {
    boolean value;

    public BooleanApiReply(boolean value) {
      this.value = value;
    }

    public String toJson() {
      return Utils.gson.toJson(this);
    }
  }
}
