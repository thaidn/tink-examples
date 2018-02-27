# Shared dependencies

maven_server(
    name = "maven_central_snapshot",
    url = "https://oss.sonatype.org/content/repositories/snapshots/",
)

maven_jar(
    name = "com_google_crypto_tink_tink",
    artifact = "com.google.crypto.tink:tink:1.1.0-SNAPSHOT",
    sha1 = "34b4cbcd606b3fd0f486e40b673ea2db589bb19c",
    server = "maven_central_snapshot",
)

# proto_library rules implicitly depend on @com_google_protobuf//:protoc,
# which is the proto-compiler.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    strip_prefix = "protobuf-3.4.0",
    urls = ["https://github.com/google/protobuf/releases/download/v3.4.0/protobuf-cpp-3.4.0.tar.gz"],
    sha256 = "71434f6f836a1e479c44008bb033b2a8b2560ff539374dcdefb126be739e1635",
)

# cc_proto_library rules implicitly depend on @com_google_protobuf_cc//:cc_toolchain,
# which is the C++ proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_cc",
    strip_prefix = "protobuf-3.4.0",
    urls = ["https://github.com/google/protobuf/releases/download/v3.4.0/protobuf-cpp-3.4.0.tar.gz"],
    sha256 = "71434f6f836a1e479c44008bb033b2a8b2560ff539374dcdefb126be739e1635",
)

# java_proto_library rules implicitly depend on @com_google_protobuf_java//:java_toolchain,
# which is the Java proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_java",
    strip_prefix = "protobuf-3.4.0",
    urls = ["https://github.com/google/protobuf/releases/download/v3.4.0/protobuf-java-3.4.0.tar.gz"],
    sha256 = "7c61081cc2346542070cd35db7d15c7f24aeeefcd3afac08ec88fec03c45d698",
)

# java_lite_proto_library rules implicitly depend on @com_google_protobuf_javalite//:javalite_toolchain,
# which is the JavaLite proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_javalite",
    strip_prefix = "protobuf-javalite",
    urls = ["https://github.com/google/protobuf/archive/javalite.zip"],
    sha256 = "b9ca3f706c6a6a6a744a7ba85321abce9e7d49825a19aaba6f29278871d41926",
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
