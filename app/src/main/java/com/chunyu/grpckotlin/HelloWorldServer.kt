package com.chunyu.grpckotlin

import com.google.protobuf.Empty
import io.grpc.Grpc
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.examples.helloworld.GreeterGrpcKt
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.examples.helloworld.helloReply
import io.grpc.netty.NettyServerBuilder
import io.grpc.okhttp.OkHttpServerBuilder

/**
 * @author chunyu
 * @date 2024/3/27
 * @description
 */
class HelloWorldServer(private val port: Int) {
    private val server: Server =
        NettyServerBuilder
            .forPort(port)
            .addService(HelloWorldService())
            .build()

    internal class HelloWorldService : GreeterGrpcKt.GreeterCoroutineImplBase() {
        override suspend fun sayHello(request: HelloRequest) =
            helloReply {
                message = "Hello ${request.name}"
            }

        override suspend fun sayGreet(request: Empty) = helloReply {
            message = "Hello Empty"
        }
    }

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
}


fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 50051
    val server = HelloWorldServer(port)
    server.start()
    server.blockUntilShutdown()
}
