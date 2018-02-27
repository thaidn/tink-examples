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
import com.google.crypto.tink.KeyManager;
import com.google.crypto.tink.subtle.EncryptThenAuthenticate;
import com.google.crypto.tink.subtle.IndCpaCipher;
import com.google.crypto.tink.subtle.MacJce;
import com.google.crypto.tink.proto.KeyData;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.timestamper.proto.AesCbcHmacKey;
import com.timestamper.proto.AesCbcHmacKeyFormat;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This key manager generates new {@code AesCbcHmacKey} keys and produces new instances of
 * {@code AesCbcHmacCipher}.
 */
public final class AesCbcHmacKeyManager implements KeyManager<Aead> {

  public static final String TYPE_URL = "type.googleapis.com/timestamper.AesCbcHmacKey";
  public static final int VERSION = 1; // to be able to evolve that type of keys without breaking

  public MessageLite newKey(ByteString serializedKeyFormat) throws GeneralSecurityException {
    try {
      AesCbcHmacKeyFormat format = AesCbcHmacKeyFormat.parseFrom(serializedKeyFormat);
      return newKey(format);
    } catch (InvalidProtocolBufferException e) {
      throw new GeneralSecurityException("expected serialized AesCtrKeyFormat proto", e);
    }
  }

  public KeyData newKeyData(ByteString serializedKeyFormat) throws GeneralSecurityException {
    AesCbcHmacKey key = (AesCbcHmacKey) newKey(serializedKeyFormat);
    return KeyData.newBuilder()
        .setTypeUrl(TYPE_URL)
        .setValue(key.toByteString())
        .setKeyMaterialType(KeyData.KeyMaterialType.SYMMETRIC)
        .build();
  }

  public MessageLite newKey(MessageLite keyFormat) throws GeneralSecurityException {
    if (!(keyFormat instanceof AesCbcHmacKeyFormat)) {
      throw new GeneralSecurityException("expected AesCtrKeyFormat proto");
    }

    AesCbcHmacKeyFormat format = (AesCbcHmacKeyFormat) keyFormat;
    if (format.getKeySize() != 16 && format.getKeySize() != 24 && format.getKeySize() != 32) {
      throw new GeneralSecurityException("No support for this key size, must be 16, 24 or 32.");
    }

    byte aesKey[] = new byte[format.getKeySize()];
    random.nextBytes(aesKey);
    byte macKey[] = new byte[16];
    random.nextBytes(macKey);
    return AesCbcHmacKey.newBuilder()
        .setVersion(VERSION)
        .setParams(format.getParams())
        .setAesKeyValue(ByteString.copyFrom(aesKey))
        .setHmacKeyValue(ByteString.copyFrom(macKey))
        .build();
  }

  @Override
  public Aead getPrimitive(ByteString serializedKey) throws GeneralSecurityException {
    try {
      AesCbcHmacKey keyProto = AesCbcHmacKey.parseFrom(serializedKey);
      return getPrimitive(keyProto);
    } catch (InvalidProtocolBufferException e) {
      throw new GeneralSecurityException("expected serialized AesCbcHmacKey proto", e);
    }
  }

  @Override
  public Aead getPrimitive(MessageLite key) throws GeneralSecurityException {
    if (!(key instanceof AesCbcHmacKey)) {
      throw new GeneralSecurityException("expected AesCtrHmacAeadKey proto");
    }
    AesCbcHmacKey keyProto = (AesCbcHmacKey) key;

    // That's where we build the actual primitive: here, I'm using a combinator from Tink,
    // to turn a IND-CPA cipher (AES-CBC) and a MAC (HMAC-SHA1) into an AEAD.
    return new EncryptThenAuthenticate(
        (IndCpaCipher) new CbcCipher(keyProto.getAesKeyValue().toByteArray()),
        new MacJce(
            "HMACSHA1", new SecretKeySpec(keyProto.getHmacKeyValue().toByteArray(), "HMAC"), 12),
        12);
  }

  @Override
  public boolean doesSupport(String typeUrl) {
    return typeUrl.equals(TYPE_URL);
  }

  @Override
  public String getKeyType() {
    return TYPE_URL;
  }

  @Override
  public int getVersion() {
    return VERSION;
  }

  @Override
  public MessageLite jsonToKey(final byte[] jsonKey) throws GeneralSecurityException {
    throw new GeneralSecurityException("Not Implemented");
  }

  @Override
  public MessageLite jsonToKeyFormat(final byte[] jsonKeyFormat) throws GeneralSecurityException {
    throw new GeneralSecurityException("Not Implemented");
  }

  @Override
  public byte[] keyToJson(ByteString serializedKey) throws GeneralSecurityException {
    throw new GeneralSecurityException("Not Implemented");
  }

  @Override
  public byte[] keyFormatToJson(ByteString serializedKeyFormat) throws GeneralSecurityException {
    throw new GeneralSecurityException("Not Implemented");
  }

  // We still need the CBC code, but luckily it's part of the stock JCA.
  private class CbcCipher implements IndCpaCipher {
    static final int IV_SIZE_IN_BYTES = 16;
    static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final SecretKeySpec key;

    CbcCipher(final byte[] aesKey) {
      key = new SecretKeySpec(aesKey, "AES");
    }

    public byte[] encrypt(final byte[] plaintext) throws GeneralSecurityException {
      if (plaintext.length > Integer.MAX_VALUE - IV_SIZE_IN_BYTES) {
        throw new GeneralSecurityException("plaintext too long");
      }
      // We're doing more byte buffer operations than needed to avoid having to compute
      // the padding length.
      byte[] ciphertextBuffer =
          new byte[IV_SIZE_IN_BYTES + plaintext.length + 128]; // more space for padding
      byte[] iv = new byte[IV_SIZE_IN_BYTES];
      random.nextBytes(iv);

      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(iv));
      int bytesOfCiphertext = cipher.doFinal(plaintext, 0, plaintext.length, ciphertextBuffer, 0);
      byte[] ciphertext = new byte[bytesOfCiphertext + IV_SIZE_IN_BYTES];
      System.arraycopy(iv, 0, ciphertext, 0, IV_SIZE_IN_BYTES);
      System.arraycopy(ciphertextBuffer, 0, ciphertext, IV_SIZE_IN_BYTES, bytesOfCiphertext);
      return ciphertext;
    }

    public byte[] decrypt(final byte[] ciphertext) throws GeneralSecurityException {
      byte[] iv = new byte[IV_SIZE_IN_BYTES];
      System.arraycopy(ciphertext, 0, iv, 0, IV_SIZE_IN_BYTES);
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(iv));
      return cipher.doFinal(ciphertext, IV_SIZE_IN_BYTES, ciphertext.length - IV_SIZE_IN_BYTES);
    }
  }

  private static final SecureRandom random = new SecureRandom();
}
