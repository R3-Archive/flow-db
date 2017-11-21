package com.birthdays

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.node.services.transactions.ValidatingNotaryService
import net.corda.nodeapi.User
import net.corda.nodeapi.internal.ServiceInfo
import net.corda.testing.driver.driver

/**
 * This file is exclusively for being able to run your nodes through an IDE (as opposed to using deployNodes)
 * Do not use in a production environment.
 */
fun main(args: Array<String>) {
    // No permissions required as we are not invoking flows.
    val user = User("user1", "test", permissions = setOf("StartFlow.com.flowhttp.HttpCallFlow"))
    driver(isDebug = true) {
        listOf(
                startNode(providedName = CordaX500Name("Controller", "London", "GB"), advertisedServices = setOf(ServiceInfo(ValidatingNotaryService.type))),
                startNode(providedName = CordaX500Name("PartyA", "London", "GB"), rpcUsers = listOf(user))).map { it.getOrThrow() }

        waitForAllNodesToFinish()
    }
}