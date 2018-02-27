package(default_visibility = ["//visibility:public"])

licenses(["notice"])  # Apache 2.0

java_binary(
    name = "helloworld",
    srcs = glob([
        "src/main/**/*.java",
    ]),
    main_class = "com.helloworld.HelloWorld",
    deps = [
        "@args4j//jar",
        "@com_google_crypto_tink_tink//jar",
    ],
    runtime_deps = [
        "@com_google_protobuf//:protobuf_java",
        "@org_json_json//jar",
    ],
)
