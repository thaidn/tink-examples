# Java Hello World

This is a command-line tool that can encrypt and decrypt small files with
AES128-GCM.

It demonstrates the basic steps of using Tink, namely loading key material,
obtaining a primitive, and using the primitive to do crypto.

It also shows how one can add a dependency on Tink using Maven or Bazel.

## Build and run

**Maven**

```shell
git clone https://github.com/thaidn/tink-examples
cd tink-examples/helloworld/java
mvn package
echo foo > foo.txt
mvn exec:java -Dexec.args="encrypt --keyset test.cfg --in foo.txt --out bar.encrypted"
mvn exec:java -Dexec.args="decrypt --keyset test.cfg --in bar.encrypted --out foo2.txt"
cat foo2.txt
```

**Bazel**

```shell
git clone https://github.com/thaidn/tink-examples
cd tink-examples/
bazel build ...
echo foo > foo.txt
./bazel-bin/helloworld/java/helloworld encrypt --keyset test.cfg --in foo.txt --out bar.encrypted
./bazel-bin/helloworld/java/helloworld decrypt --keyset test.cfg --in bar.encrypted --out foo2.txt
cat foo2.txt
```
