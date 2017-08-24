# Tink usage example: Timestamper

This is an example of a full project using
[Tink](https://github.com/google/tink) to provide an application that notarizes
documents through a REST API. The build infrastructure goes through Bazel, and
we also provide a simple AngularJS frontend to make it more visual.

The easiest way to run this is to run

```shell
bazel run :TimestamperServer -- --key-type AES-GCM --keyset keyset.cfg
```

and navigate to https://localhost:8000/ to see the web interface.

This example is contributed by Raphael Jamet.

## Overview

User-provided text is notarized by building and verifying tokens, which are
built with an authenticated encryption. The plaintext here is a timestamp (so
the token embeds that information), and we use the document to notarize as
additionnal authenticated data.

The server and API code is located in `src/main/java/com/timestamper/`, and the
client application is in `src/static/`.

## Options

Tink is strongly centered around keysets. The `--keyset` allows specifying the
keyset file. If it doesn't exist, the server generates a new keyset. If it
already exists, the key material within is used. To see how this is done, look
in `CryptoService.java`, the `init` method: this reads the keyset or uses a key
template to generate a new one, from which we get the
(Aead)[https://github.com/google/tink/blob/master/java/src/main/java/com/google/crypto/tink/Aead.java]
object used to actually encrypt and decrypt (in `WitnessingToken.java` and
`TokenAttestation.java`).

The `--key-type` option allows specifying the type of the keys. Supported types
are `AES-GCM` which is provided by Tink itself, and `AES-CBC-HMAC` which is a
custom primitive provided by this sample app.

## Rotating keys

We've also added two buttons on the main application, called soft rotate and
hard rotate. Tink's primitives make use of keysets, which are roughly bundle of
keys, one of which is active at any given point. The application here only uses
one keyset for generating attestations, and no matter which target you run, this
keyset will only contain a single key in the current configuration.

This feature is implemented in `CryptoService.java`, function `rotate`.

### Soft rotate

The soft rotate button will trigger the *addition* of a new key to the keyset,
and sets it as the active one. This means your keyset will now contain two keys,
and even though encryptions will now use the active one, the server will still
be able to decrypt ciphertexts built with one of the non-active keys. This is
useful for when you want to rotate a key without breaking previous uses, such as
keys for generating expiring tokens, for instance.

### Hard rotate

The hard rotate button also creates a new key, but cleans up the entire bundle
before setting it as active. This means your old keys are no longer used, and
older attestations will no longer be considered as valid.

## A custom Tink primitive

This example also contains a custom primitive, AES-CBC with a HMAC. The relevant
class is `AesCbcHmacKeyManager.java`, and it is registered with Tink in
`CryptoService.java` in the `init` method. The primitive itself requires a few
custom protobufs, the details of which you can find in
`src/proto/aes_cbc_hmac.proto`. To run the timestamper with that custom scheme,
run

```shell
bazel run :TimestamperServer -- --key-type AES-CBC-HMAC --keyset keyset.cfg
```

## Possible improvements

*   Use a KMS (local, or remote) to protect the cryptographic material at rest.

*   Add tests.
