# gRPC on Kotlin

gRPC 是由 Google 开发的高性能、开源的远程过程调用（RPC）框架，它基于 HTTP/2 协议进行通信，并使用 Protocol Buffers 作为默认的序列化工具。gRPC 支持多种编程语言，包括 C、C++、Java、Python、Go、C# 等，使得开发者可以在不同的平台上轻松地构建分布式系统。 

本文为您从 0 开始搭建 kotlin 语言下的 gRPC 运行环境。

## Reference

官方文档：https://grpc.io/docs/languages/kotlin/quickstart/

## 官方 Sample 的理解

### proto

gRPC 与所有的 RPC 一样，是 C/S 架构 + 通信协议为组成部分。其中 gRPC 的协议是通过 proto 文件来定义的。

Protocol Buffers（protobuf）是一种轻量级、高效的数据序列化格式，由 Google 开发并开源。它被设计用于高效地序列化结构化数据，并支持多种编程语言。

Protocol Buffers 被广泛用于分布式系统中，例如用于定义 RPC 服务的消息格式、网络通信协议的数据交换格式等。它是一种通用且高效的数据序列化方案，适用于各种不同的场景。

以官方的 Sample 为例，下面的 .proto 文件描述了一个服务：

```proto
// The greeting service definition.
service Greeter {
  // Sends a greeting
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

// The request message containing the user's name.
message HelloRequest {
  string name = 1;
}

// The response message containing the greetings
message HelloReply {
  string message = 1;
}
```

定义了一个 Greeter 服务，它提供一个 SayHello 方法，服务端和客户端的实现类都会实现该方法。

客户端侧，可以传递一个 HelloRequest 类型的请求，其中包含一个类型为字符串的参数 name；服务端侧会返回一个 HelloReply 类型的响应，其中包含一个字符串类型的 massge 属性值。

### 服务端

```
private class HelloWorldService : GreeterGrpcKt.GreeterCoroutineImplBase() {
  override suspend fun sayHello(request: HelloRequest) = helloReply {
    message = "Hello ${request.name}"
  }
}
```

grpc 会根据 proto 文件自动生成一些代码，例如上面这段代码中的 GreeterGrpcKt、GreeterCoroutineImplBase、HelloRequest 等。

在服务端的 sayHello 方法中，实现服务端内部的逻辑最终返回一个 HelloReply 类型的消息。

### 客户端

```
class HelloWorldClient(
    private val channel: ManagedChannel
) : Closeable {
  private val stub: GreeterCoroutineStub = GreeterCoroutineStub(channel)

  suspend fun greet(name: String) {
    val request = helloRequest { this.name = name }
    val response = stub.sayHello(request)
    println("Received: ${response.message}")
  }
}
```

客户端这边简单演示了如何调用自动生成的 GreeterCoroutineStub 对象的 sayHello 方法发出请求，并接受响应结果。

## 项目搭建

新建一个 Android 项目，准备开始搭建 grpc-kotlin 语言的实现。

### 添加依赖

在 app 的 `build.gradle` 模块下，增加以下依赖（如果是 library 建议使用 api 替换 implementation）：

```kotlin
implementation("io.grpc:grpc-stub:grpc_version")
implementation("io.grpc:grpc-protobuf-lite:grpc_version")
implementation("io.grpc:grpc-kotlin-stub:XXX")
implementation("com.google.protobuf:protobuf-kotlin-lite:XXX")
// 根据实际需要可选
implementation("io.grpc:grpc-okhttp:grpc_version")
implementation("io.grpc:grpc-netty:grpc_version")
```

增加插件：

```kotlin
plugins {
    // ...
    id 'com.google.protobuf' version('X.X.X')
}
```

编译 proto 文件需要在 `build.gradle` 中增加以下内容，注意不用放在任何 block 下，与 plugin/android 平级 ：

```
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    plugins {
        create("java") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.2"
        }
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.60.2"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1" + ":jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("java") {
                    option("lite")
                }
                create("grpc") {
                    option("lite")
                }
                create("grpckt") {
                    option("lite")
                }
            }
            it.builtins {
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}
```

需要注意的是，这里不能省略 "java"、"grpc" 这两部分内容，否则编译会报错缺少一些自动生成的类型。

### 增加 proto 文件

可以用多种形式在 Android 项目中导入 proto 文件，常见方式是在 `app/src/main/` 下新建一个 `proto` 目录，然后把所有 .proto 文件放在该目录下，另一种方式就是参考 sample 代码，通过独立的一个 module，其他模块引用该 module 依赖。

这里添加了一个 sample 中的示例：

```protobuf
syntax = "proto3";
import "google/protobuf/empty.proto";

option java_multiple_files = true;
option java_package = "io.grpc.examples.helloworld";
option java_outer_classname = "HelloWorldProto";

package helloworld;

// The greeting service definition.
service Greeter {
  // Sends a greeting
  rpc SayHello (HelloRequest) returns (HelloReply) {}

  rpc SayGreet (google.protobuf.Empty) returns (HelloReply) {}
}

// The request message containing the user's name.
message HelloRequest {
  string name = 1;
}

// The response message containing the greetings
message HelloReply {
  string message = 1;
}
```

这里与 sample 不同的是，增加了一个 `google.protobuf.Empty` 作为参数，这个类型的定义在 Google 通用的 protobuf 文件中，通过

```
import "google/protobuf/empty.proto";
```

来引用，并通过完整包名来使用，这样构建时才不会出现问题。

> 在 proto 文件中，以下配置不会对 proto 文件跨平台产生影响，只是配置 java 环境下生成内容的一些属性，比如包名、类名等。
>
> ```proto
> option java_multiple_files = true;
> option java_package = "io.grpc.examples.helloworld";
> option java_outer_classname = "HelloWorldProto";
> ```
>
> 在 C++ 或其他语言会忽略 java 的这些配置。

此时，就可以运行 build 来构建项目了，但此时 build 项目，依然会报错：

```log
10 files found with path 'META-INF/INDEX.LIST'.
Adding a packaging block may help, please refer to
https://developer.android.com/reference/tools/gradle-api/8.3/com/android/build/api/dsl/Packaging
for more information
```

在 `build.gradle`  的 `android` 块中增加以下配置：

```kotlin
android {
		// ... 
		packagingOptions {
        resources.excludes.add("META-INF/*")
    }
}
```

到此，整个项目的构建就完成了。

## 代码实现

### 本地服务器实现

实现本地服务器代码分为以下部分：

1. 定义一个服务；
2. 服务中实现 proto 定义的方法，返回 proto 中定义的返回值；
3. 构建一个 Server；
4. 为 Server 实现生命周期逻辑：启动、停止、阻塞等。

第一步，定义服务：

```kotlin
class HelloWorldServer {
    internal class HelloWorldService : GreeterGrpcKt.GreeterCoroutineImplBase() {
        override suspend fun sayHello(request: HelloRequest) = helloReply {
						// TODO
				}

        override suspend fun sayGreet(request: Empty) = helloReply {
            // TODO
        }
    }
}
```

这里的 `GreeterGrpcKt.GreeterCoroutineImplBase` 是 grpc 自动生成的代码类型。

第二步，实现返回逻辑：

```kotlin
    internal class HelloWorldService : GreeterGrpcKt.GreeterCoroutineImplBase() {
        override suspend fun sayHello(request: HelloRequest) =
            helloReply {
                message = "Hello ${request.name}"
            }

        override suspend fun sayGreet(request: Empty) = helloReply {
            message = "Hello Empty"
        }
    }
```

只需要定义返回值即可。

第三步，构建 Server：

```kotlin
    val server: Server =
        ServerBuilder
            .forPort(port)
            .addService(HelloWorldService())
            .build()
```

第四步，server 的生命周期：

```kotlin
    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@HelloWorldServer.stop()
                println("*** server shut down")
            },
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
```

#### 服务器启动

```kotlin
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50051
    val server = HelloWorldServer(port)
    server.start()
    server.blockUntilShutdown()
}
```

在启动服务器时，应该会报错，需要添加以下依赖：

```kotlin
implementation("io.grpc:grpc-netty:1.60.2")
implementation("com.squareup.okio:okio:3.8.0")
implementation("io.perfmark:perfmark-api:0.27.0")
```

并更改服务端代码：

```kotlin
    private val server: Server =
        NettyServerBuilder
            .forPort(port)
            .addService(HelloWorldService())
            .build()
```

### 本地客户端实现

实现本地客户端实现非常简单：

```kotlin
class HelloWorldClient(private val channel: ManagedChannel) {
    private val stub: GreeterGrpcKt.GreeterCoroutineStub =
        GreeterGrpcKt.GreeterCoroutineStub(channel)

    suspend fun sayHello(name: String) {
        val request = helloRequest { this.name = name }
        val response = stub.sayHello(request)
        println("Received: ${response.message}")
    }

    suspend fun sayGreet() {
        val request = helloRequest { this.name = name }
        val response = stub.sayGreet(Empty.newBuilder().build())
        println("Received: ${response.message}")
    }
}
```

通过 ManagedChannel 对象，构建 grpc 自动生成的对应类的 Stub 对象，然后通过 Stub 对象调用 proto 服务中定义的 rpc 方法，接收返回值即可。

grpc-kotlin，会根据 proto 定义的 server 自动生成 XXXCoroutineStub，配合协程使用。

java 可以使用 XXXStub（异步）、XXXBlockingStub（同步）来实现客户端请求、接收响应的逻辑。

#### 客户端发起请求

```kotlin
fun main() {
    runBlocking {
        val port = 50051
        val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()
        val client = HelloWorldClient(channel)
        println("call sayHello")
        client.sayHello("user")
        while (true) {
            Thread.sleep(3000)
            println("time next")
        }
    }
}
```

### Android 客户端实现

首先，上面构建本地客户端的形式适用于 Android 客户端直接去实现，另一种是使用 grpc-android，增加以下依赖：

```kotlin
implementation("io.grpc:grpc-android:1.62.2")
```

通过这个库中的 AndroidChannelBuilder 来构建 channel：

```kotlin
// val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()
val androidChannel = AndroidChannelBuilder
            .forAddress("localhost", port)
            .context(this@MainActivity)
            .usePlaintext()
            .build()
```

AndroidChannelBuilder 用来构建一个 ManagedChannel，当提供了一个 Context 时，它会自动监控 Android 设备的网络状态，以平滑处理间歇性的网络故障。 目前仅兼容 gRPC 的 OkHttp 传输，在运行时必须可用。 需要 Android 的 ACCESS_NETWORK_STATE 权限。

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.hwBtn).setOnClickListener {
            sayHello()
        }
    }

    private fun sayHello() {
        val port = System.getenv("PORT")?.toInt() ?: 50051
        val androidChannel = AndroidChannelBuilder
            .forAddress("localhost", port)
            .context(this@MainActivity)
            .usePlaintext()
            .build()
        val client = HelloWorldClient(androidChannel)
        CoroutineScope(Dispatchers.IO).launch {
            client.sayHello("from android")
        }
    }
}
```

## 单元测试

单元测试首先要添加 gRPC 测试依赖：

```kotlin
testImplementation("io.grpc:grpc-testing:1.60.2")
```

#### 为 Server 创建测试

```kotlin
class HelloWorldServerTest {
    @get:Rule
    val grpcServerRule: GrpcServerRule = GrpcServerRule().directExecutor()

    @Test
    fun sayHello() = runBlocking {
            val service = HelloWorldServer.HelloWorldService()
            grpcServerRule.serviceRegistry.addService(service)

            val stub = GreeterGrpcKt.GreeterCoroutineStub(grpcServerRule.channel)
            val testName = "test user"
            val reply = stub.sayHello(helloRequest { name = testName })
            assertEquals("Hello $testName", reply.message)
        }

    @Test
    fun sayGreet() = runBlocking {
        val service = HelloWorldServer.HelloWorldService()
        grpcServerRule.serviceRegistry.addService(service)

        val stub = GreeterGrpcKt.GreeterCoroutineStub(grpcServerRule.channel)
        val reply = stub.sayGreet(Empty.newBuilder().build())
        assertEquals("Hello Empty", reply.message)
    }

    @Test
    fun start() {
        val port = System.getenv("PORT")?.toInt() ?: 50051
        val server = HelloWorldServer(port)
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            val stub = GreeterGrpcKt.GreeterCoroutineStub(grpcServerRule.channel)
            val testName = "test user"
            val reply = stub.sayGreet(Empty.newBuilder().build())
            assertEquals("Hello Empty", reply.message)
        }
        server.start()
    }
}
```

使用 Junit4 和 grpc-testing 来构建测试代码，GrpcServerRule 是 grpc-testing 中的工具，GrpcServerRule 是一个 JUnit TestRule，对于测试基于 gRPC 的客户端和服务非常有用。

前两个方法是测试请求响应的逻辑；最后一个对服务生命周期的测试则需要考虑更多问题，例如服务器启动后，会一直处于阻塞状态，要确保请求是在服务器启动后才发出的，需要开发者模拟实际情境构建测试。

#### 为客户端添加测试

首先为了方便把 HelloWorldClient 中的 sayHello 和 sayGreet 方法无返回值改为返回 HelloRely：

```kotlin
    suspend fun sayHello(name: String): HelloReply {
        val request = helloRequest { this.name = name }
        val response = stub.sayHello(request)
        println("Received: ${response.message}")
        return response
    }

    suspend fun sayGreet(): HelloReply {
        val response = stub.sayGreet(Empty.newBuilder().build())
        println("Received: ${response.message}")
        return response
    }
```

然后为这两个方法创建测试：

```kotlin
class HelloWorldClientKtTest {
    @get:Rule
    val grpcServerRule: GrpcServerRule = GrpcServerRule().directExecutor()

    @Before
    fun setup() {
        val service = HelloWorldServer.HelloWorldService()
        grpcServerRule.serviceRegistry.addService(service)
    }

    @Test
    fun sayHello() = runBlocking {
            val client = HelloWorldClient(grpcServerRule.channel)
            val testName = "test user"
            val reply = client.sayHello(testName)
            assertEquals("Hello $testName", reply.message)
        }

    @Test
    fun sayGreet() = runBlocking {
        val client = HelloWorldClient(grpcServerRule.channel)
        val reply = client.sayGreet()

        assertEquals("Hello Empty", reply.message)
    }
}
```

首先，在创建客户端测试之前要启动一个服务端，才能验证客户端的请求和返回结果的正确性。所以这里在所有测试方法之前添加 @Before 注解的 setup 方法先运行服务端。

然后构建客户端对象，调用请求，最后通过断言验证结果。

## Demo 仓库

https://github.com/JChunyu/GrpcKotlin