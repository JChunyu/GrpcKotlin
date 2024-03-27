package com.chunyu.grpckotlin

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.examples.helloworld.GreeterGrpcKt
import io.grpc.examples.helloworld.helloRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * @author chunyu
 * @date 2024/3/27
 * @description
 */
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