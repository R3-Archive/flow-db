package com.flowdb

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.loggerFor
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

val TABLE_NAME = "obligation_queue"

@InitiatingFlow
@StartableByRPC
class AddTokenValueFlow : FlowLogic<Unit>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val databaseService = serviceHub.cordaService(DatabaseService::class.java)
        databaseService.addTokenValue("bitcoin", 7000)
    }
}

@InitiatingFlow
@StartableByRPC
class UpdateTokenValueFlow : FlowLogic<Unit>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): Unit {
        val databaseService = serviceHub.cordaService(DatabaseService::class.java)
        databaseService.updateTokenValue("bitcoin", 8000)
    }
}

@InitiatingFlow
@StartableByRPC
class QueryTokenValueFlow : FlowLogic<Int>() {
    override val progressTracker: ProgressTracker = ProgressTracker()

    @Suspendable
    override fun call(): Int {
        val databaseService = serviceHub.cordaService(DatabaseService::class.java)
        return databaseService.queryTokenValue("bitcoin")
    }
}

@CordaService
class DatabaseService(val services: ServiceHub) : SingletonSerializeAsToken() {
    init {
        setUpStorage()
    }

    private companion object {
        val log = loggerFor<DatabaseService>()
    }

    // Creates a custom node database table.
    private fun setUpStorage() {
        val query = """
            create table if not exists $TABLE_NAME(
                token varchar(64),
                value int
            )"""

        executeUpdate(query, emptyMap())
        log.info("Created custom table.")
    }

    fun addTokenValue(token: String, value: Int) {
        val query = "insert into $TABLE_NAME values(?, ?)"

        val params = mapOf(1 to token, 2 to value)

        executeUpdate(query, params)
        log.info("Token $token added to Queue.")
    }

    fun updateTokenValue(token: String, value: Int) {
        val query = "update $TABLE_NAME set value = ? where token = ?"

        val params = mapOf(1 to value, 2 to token)

        executeUpdate(query, params)
        log.info("Token $token status updated.")
    }

    fun queryTokenValue(token: String): Int {
        val query = "select value from $TABLE_NAME where token = ?"

        val params = mapOf(1 to token)

        return executeQuery(query, params, { it -> it.getInt("value") }).single()
    }

    // Queries the database.
    private fun <T : Any> executeQuery(
            query: String,
            params: Map<Int, Any>,
            transformer: (ResultSet) -> T
    ): List<T> {
        val preparedStatement = prepareStatement(query, params)
        val obligations = mutableListOf<T>()

        try {
            val resultSet = preparedStatement.executeQuery()
            while (resultSet.next()) {
                obligations.add(transformer(resultSet))
            }
        } catch (e: SQLException) {
            log.error(e.message)
            throw e
        } finally {
            preparedStatement.close()
        }

        return obligations
    }

    // Updates the database.
    private fun executeUpdate(query: String, params: Map<Int, Any>) {
        val preparedStatement = prepareStatement(query, params)

        try {
            preparedStatement.executeUpdate()
        } catch (e: SQLException) {
            log.error(e.message)
            throw e
        } finally {
            preparedStatement.close()
        }
    }

    // Creates a PreparedStatement - a precompiled SQL statement to be
    // executed against the database.
    private fun prepareStatement(query: String, params: Map<Int, Any>): PreparedStatement {
        val session = services.jdbcSession()
        val preparedStatement = session.prepareStatement(query)

        params.forEach { (key, value) ->
            when (value) {
                is String -> preparedStatement.setString(key, value)
                is Int -> preparedStatement.setInt(key, value)
                is Long -> preparedStatement.setLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type.")
            }
        }

        return preparedStatement
    }
}