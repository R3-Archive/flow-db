package com.flowdb

import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.unsetCordappPackages
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

val BITCOIN_README_URL = "https://raw.githubusercontent.com/bitcoin/bitcoin/4405b78d6059e536c36974088a8ed4d9f0f29898/readme.txt"

class FlowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedNode<MockNode>

    @Before
    fun setup() {
        network = MockNetwork()
        val nodes = network.createSomeNodes(1)
        a = nodes.partyNodes[0]
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unsetCordappPackages()
    }

    @Test
    fun `testFlowReturnsCorrectHtml`() {
        // The flow should return the first commit of the BitCoin readme.
        val flow = HttpCallFlow()
        val future = a.services.startFlow(flow).resultFuture
        network.runNetwork()
        val returnValue = future.get()

        // We run the flow and retrieve its return value.
        val httpRequest = Request.Builder().url(BITCOIN_README_URL).build()
        val httpResponse = OkHttpClient().newCall(httpRequest).execute()
        val expectedValue = httpResponse.body().string()

        // We check that the strings are equal.
        assertEquals(expectedValue, returnValue)
    }
}