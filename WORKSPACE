# Shared dependencies

maven_server(
    name = "maven_central_snapshot",
    url = "https://oss.sonatype.org/content/repositories/snapshots/",
)

maven_jar(
    name = "com_google_crypto_tink_tink",
    artifact = "com.google.crypto.tink:tink:1.0.0",
    sha1 = "49a929cec8791a1fbc8047799f3a9330bfe25975",
)

# proto_library rules implicitly depend on @com_google_protobuf//:protoc,
# which is the proto-compiler.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    strip_prefix = "protobuf-3.3.0",
    urls = ["https://github.com/google/protobuf/archive/v3.3.0.tar.gz"],
    sha256 = "94c414775f275d876e5e0e4a276527d155ab2d0da45eed6b7734301c330be36e",
)

# java_proto_library rules implicitly depend on @com_google_protobuf_java//:java_toolchain,
# which is the Java proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_java",
    strip_prefix = "protobuf-3.3.0",
    urls = ["https://github.com/google/protobuf/archive/v3.3.0.tar.gz"],
    sha256 = "94c414775f275d876e5e0e4a276527d155ab2d0da45eed6b7734301c330be36e",
)

# HelloWorld

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


