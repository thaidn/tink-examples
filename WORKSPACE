# Shared dependencies

maven_jar(
    name = "com_google_crypto_tink_tink",
    artifact = "com.google.crypto.tink:tink:1.1.0",
    sha1 = "6eb0bddb58506c9a8c0f663f3b266e2fdb36698e",
)

# proto_library, cc_proto_library and java_proto_library rules implicitly depend
# on @com_google_protobuf//:proto, @com_google_protobuf//:cc_toolchain and
# @com_google_protobuf//:java_toolchain, respectively.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    strip_prefix = "protobuf-3.4.1",
    urls = ["https://github.com/google/protobuf/archive/v3.4.1.zip"],
)

# java_lite_proto_library rules implicitly depend on
# @com_google_protobuf_javalite//:javalite_toolchain, which is the JavaLite proto
# runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_javalite",
    strip_prefix = "protobuf-javalite",
    urls = ["https://github.com/google/protobuf/archive/javalite.zip"],
)

# HelloWorld Java

maven_jar(
    name = "args4j",
    artifact = "args4j:args4j:2.33",
    sha1 = "bd87a75374a6d6523de82fef51fc3cfe9baf9fc9",
)

# Timestamper

maven_jar(
    name = "com_google_code_gson_gson",
    artifact = "com.google.code.gson:gson:2.8.0",
    sha1 = "c4ba5371a29ac9b2ad6129b1d39ea38750043eff",
)

maven_jar(
    name = "org_json_json",
    artifact = "org.json:json:20170516",
    sha1 = "949abe1460757b8dc9902c562f83e49675444572",
)
