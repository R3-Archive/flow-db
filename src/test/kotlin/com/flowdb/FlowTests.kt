package com.flowdb

import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val BITCOIN = "bitcoin"
private val E_CASH = "eCash"
private val INITIAL_BITCOIN_VALUE = 7000
private val NEW_BITCOIN_VALUE = 8000
private val INITIAL_E_CASH_VALUE = 100
private val NEW_E_CASH_VALUE = 200

class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedNode<MockNode>

    @Before
    fun setup() {
        network = MockNetwork()

        val nodes = network.createSomeNodes(1)
        a = nodes.partyNodes[0]
        a.database.transaction {
            a.internals.installCordaService(CryptoValuesDatabaseService::class.java)
        }

        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unsetCordappPackages()
    }

    @Test
    fun `flowWritesToTableCorrectly`() {
        val flow1 = AddTokenValueFlow(BITCOIN, INITIAL_BITCOIN_VALUE)
        val future1 = a.services.startFlow(flow1).resultFuture
        network.runNetwork()
        future1.get()

        val flow2 = QueryTokenValueFlow(BITCOIN)
        val future2 = a.services.startFlow(flow2).resultFuture
        network.runNetwork()
        val bitcoinValueFromDB = future2.get()

        assertEquals(INITIAL_BITCOIN_VALUE, bitcoinValueFromDB)
    }

    @Test
    fun `flowUpdatesTableCorrectly`() {
        val flow1 = AddTokenValueFlow(BITCOIN, INITIAL_BITCOIN_VALUE)
        val future1 = a.services.startFlow(flow1).resultFuture
        network.runNetwork()
        future1.get()

        val flow2 = UpdateTokenValueFlow(BITCOIN, NEW_BITCOIN_VALUE)
        val future2 = a.services.startFlow(flow2).resultFuture
        network.runNetwork()
        future2.get()

        val flow3 = QueryTokenValueFlow(BITCOIN)
        val future3 = a.services.startFlow(flow3).resultFuture
        network.runNetwork()
        val bitcoinValueFromDB = future3.get()

        assertEquals(NEW_BITCOIN_VALUE, bitcoinValueFromDB)
    }

    @Test
    fun `tableSupportsMultipleTokensCorrectly`() {
        val flow1 = AddTokenValueFlow(BITCOIN, INITIAL_BITCOIN_VALUE)
        val future1 = a.services.startFlow(flow1).resultFuture
        network.runNetwork()
        future1.get()

        val flow2 = UpdateTokenValueFlow(BITCOIN, NEW_BITCOIN_VALUE)
        val future2 = a.services.startFlow(flow2).resultFuture
        network.runNetwork()
        future2.get()

        val flow3 = AddTokenValueFlow(E_CASH, INITIAL_E_CASH_VALUE)
        val future3 = a.services.startFlow(flow3).resultFuture
        network.runNetwork()
        future3.get()

        val flow4 = UpdateTokenValueFlow(E_CASH, NEW_E_CASH_VALUE)
        val future4 = a.services.startFlow(flow4).resultFuture
        network.runNetwork()
        future4.get()

        val flow5 = QueryTokenValueFlow(BITCOIN)
        val future5 = a.services.startFlow(flow5).resultFuture
        network.runNetwork()
        val bitcoinValueFromDB = future5.get()

        val flow6 = QueryTokenValueFlow(E_CASH)
        val future6 = a.services.startFlow(flow6).resultFuture
        network.runNetwork()
        val eCashValueFromDB = future6.get()

        assertEquals(NEW_BITCOIN_VALUE, bitcoinValueFromDB)
        assertEquals(NEW_E_CASH_VALUE, eCashValueFromDB)
    }

    @Test
    fun `errorIsThrownIfTokenNotInTable`() {
        val flow = QueryTokenValueFlow(BITCOIN)
        val future = a.services.startFlow(flow).resultFuture
        network.runNetwork()

        assertFailsWith<IllegalArgumentException> { future.getOrThrow() }
    }
}