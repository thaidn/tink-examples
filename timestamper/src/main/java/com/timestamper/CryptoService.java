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

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.KeysetManager;
import com.google.crypto.tink.KeysetReaders;
import com.google.crypto.tink.KeysetWriters;
import com.google.crypto.tink.Registry;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.aead.AesGcmKeyManager;
import com.google.crypto.tink.config.Config;
import com.google.crypto.tink.proto.AesGcmKeyFormat;
import com.google.crypto.tink.proto.KeyStatusType;
import com.google.crypto.tink.proto.KeyTemplate;
import com.google.crypto.tink.proto.Keyset;
import com.timestamper.proto.AesCbcHmacKeyFormat;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

class CryptoService {

  private static KeyTemplate keyTemplate;
  private static KeysetHandle keysetHandle;
  private static Aead aead;
  private static File keyset;

  // Read the protobuf representing the key template we're going to use
  private static final KeyTemplate AES_GCM_KEY_TEMPLATE = AeadKeyTemplates.AES128_GCM;

  private static final KeyTemplate AES_CBC_HMAC_KEY_TEMPLATE =
      KeyTemplate.newBuilder()
          .setTypeUrl(AesCbcHmacKeyManager.TYPE_URL)
          .setValue(AesCbcHmacKeyFormat.newBuilder().setKeySize(16).build().toByteString())
          .build();

  static void init(File inputKeyset, String keyType)
      throws GeneralSecurityException, IOException {
    // Register the 1.0.0 key types with the run time.
    Config.register(Config.TINK_1_0_0);
    Registry.registerKeyManager(
        AesCbcHmacKeyManager.TYPE_URL, new AesCbcHmacKeyManager());

    if (keyType.toLowerCase().equals("aes-gcm")) {
      keyTemplate = AES_GCM_KEY_TEMPLATE;
    } else if (keyType.toLowerCase().equals("aes-cbc-hmac")) {
      keyTemplate = AES_CBC_HMAC_KEY_TEMPLATE;
    } else {
      throw new GeneralSecurityException("invalid key type: " + keyType);
    }

    keyset = inputKeyset;
    if (keyset.exists()) {
      // Read the cleartext keyset from disk.
      // Tink also supports reading/writing encrypted keysets, see
      // https://github.com/google/tink/blob/master/doc/JAVA-HOWTO.md#loading-existing-keysets.
      keysetHandle = CleartextKeysetHandle.read(KeysetReaders.withFile(keyset));
    } else {
      // Generate a fresh keyset and write it to disk.
      keysetHandle = KeysetHandle.generateNew(keyTemplate);
      CleartextKeysetHandle.write(keysetHandle, KeysetWriters.withFile(keyset));
    }

    aead = AeadFactory.getPrimitive(keysetHandle);
  }

  static byte[] encrypt(final byte[] plaintext, final byte[] aad) throws GeneralSecurityException {
    return aead.encrypt(plaintext, aad);
  }

  static byte[] decrypt(final byte[] ciphertext, final byte[] aad) throws GeneralSecurityException {
    return aead.decrypt(ciphertext, aad);
  }

  static ApiReplies.BooleanApiReply rotate(boolean keepOlderKeysAsSecondary) {
    try {
      KeysetHandle rotatedKeysetHandle = KeysetManager
            .withKeysetHandle(keysetHandle)
            .rotate(keyTemplate)
            .getKeysetHandle();

      if (!keepOlderKeysAsSecondary) {
        // TODO(thaidn): add an API to KeysetManager that disables/deletes all but the
        // primary key.
      }
      keysetHandle = rotatedKeysetHandle;
      // Persist to disk.
      CleartextKeysetHandle.write(keysetHandle, KeysetWriters.withFile(keyset));
      // Reload the Aead primitive.
      aead = AeadFactory.getPrimitive(keysetHandle);
      return new ApiReplies.BooleanApiReply(true);
    } catch (GeneralSecurityException | IOException e) {
      return new ApiReplies.BooleanApiReply(false);
    }
  }
}
