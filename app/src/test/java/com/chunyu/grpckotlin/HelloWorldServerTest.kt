package com.chunyu.grpckotlin

import com.google.protobuf.Empty
import io.grpc.examples.helloworld.GreeterGrpcKt
import io.grpc.examples.helloworld.helloRequest
import io.grpc.testing.GrpcServerRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/**
 * @author chunyu
 * @date 2024/3/28
 * @description
 */
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

    @Test
    fun stop() {
        val port = System.getenv("PORT")?.toInt() ?: 50051
        val server = HelloWorldServer(port)
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            val stub = GreeterGrpcKt.GreeterCoroutineStub(grpcServerRule.channel)
            val reply = stub.sayGreet(Empty.newBuilder().build())
            assertNull(reply)
        }
        server.start()
        server.stop()
    }


    @Test
    fun blockUntilShutdown() {
        val port = System.getenv("PORT")?.toInt() ?: 50051
        val server = HelloWorldServer(port)
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            assertEquals(server.server.isTerminated, false)
            server.stop()
            assertEquals(server.server.isTerminated, true)
        }
        server.start()
        server.blockUntilShutdown()

    }
}