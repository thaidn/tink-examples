package(default_visibility = ["//visibility:__pkg__"])

licenses(["notice"])  # Apache 2.0

proto_library(
    name = "aes_cbc_hmac",
    srcs = ["aes_cbc_hmac.proto"],
)

java_proto_library(
    name = "aes_cbc_hmac_java_proto",
    visibility = ["//visibility:public"],
    deps = [":aes_cbc_hmac"],
)
