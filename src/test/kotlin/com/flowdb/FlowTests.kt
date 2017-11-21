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

class FlowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedNode<MockNode>

    @Before
    fun setup() {
        network = MockNetwork()

        val nodes = network.createSomeNodes(1)
        a = nodes.partyNodes[0]
        a.database.transaction {
            a.internals.installCordaService(DatabaseService::class.java)
        }

        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unsetCordappPackages()
    }

    @Test
    fun `testFlowReturnsCorrectHtml`() {
        val flow = AddTokenValueFlow()
        val future = a.services.startFlow(flow).resultFuture
        network.runNetwork()
        val returnValue = future.get()

        val flow2 = UpdateTokenValueFlow()
        val future2 = a.services.startFlow(flow2).resultFuture
        network.runNetwork()
        val returnValue2 = future2.get()

        val flow3 = QueryTokenValueFlow()
        val future3 = a.services.startFlow(flow3).resultFuture
        network.runNetwork()
        val returnValue3 = future3.get()

        println()
        println(returnValue3)
        println()
    }
}