package(
    licenses = ["notice"],  # Apache 2.0
)

java_test(
    name = "JavaPreprocessorTest",
    srcs = ["JavaPreprocessorTest.java"],
    deps = [
        "//third_party:guava",
        "//third_party:junit",
        "//tools/java/com/google/j2cl/tools/gwtincompatible:gwtincompatible_lib",
    ],
)

java_test(
    name = "UnusedImportsNodeCollectorTest",
    srcs = ["UnusedImportsNodeCollectorTest.java"],
    deps = [
        "//third_party:guava",
        "//third_party:jdt-core",
        "//third_party:junit",
        "//tools/java/com/google/j2cl/tools/gwtincompatible:gwtincompatible_lib",
    ],
)
