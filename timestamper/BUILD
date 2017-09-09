package(default_visibility = ["//visibility:__pkg__"])

licenses(["notice"])  # Apache 2.0

java_binary(
    name = "TimestamperServer",
    jvm_flags = [" -Dfile.encoding=UTF-8 "],
    main_class = "com.timestamper.Server",
    resources = [
        "//timestamper/src/static:bundled",
    ],
    runtime_deps = [
        ":timestamper-lib",
    ],
)

java_library(
    name = "timestamper-lib",
    srcs = glob([
        "src/main/java/**/*.java",
    ]),
    deps = [
        "//timestamper/src/proto:aes_cbc_hmac_java_proto",
        "@args4j//jar",
        "@com_google_code_gson_gson//jar",
        "@com_google_protobuf_java//:protobuf_java",
        "@com_google_crypto_tink_tink//jar",
    ],
    runtime_deps = [
        "@org_json_json//jar",
    ],
)
