package com.chunyu.grpckotlin

import io.grpc.testing.GrpcServerRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * @author chunyu
 * @date 2024/3/28
 * @description
 */
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
        val service = HelloWorldServer.HelloWorldService()
        grpcServerRule.serviceRegistry.addService(service)

        val client = HelloWorldClient(grpcServerRule.channel)
        val reply = client.sayGreet()

        assertEquals("Hello Empty", reply.message)
    }
}