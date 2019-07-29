/*
 * MIT License
 *
 * Copyright (c) 2019 Dinos Papakostas
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.din0s.sql

import me.din0s.config.Config
import me.din0s.sql.managers.ISqlManager
import me.din0s.util.loadClasses
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Handles connection to the database and the initialization
 * for all managers that extend the [ISqlManager] interface.
 *
 * @author Dinos Papakostas
 */
object Database {
    private val log = LogManager.getLogger()

    /**
     * Initializes the database, by connecting to it, creating possibly missing tables,
     * and invoking the [ISqlManager.preReady] function of all SQL managers.
     */
    fun init() {
        log.info("Connecting to SQL database")
        Database.connect(
            url = "jdbc:mysql://localhost/${Config.sqlDb}?useUnicode=true&characterEncoding=utf8",
            driver = "org.mariadb.jdbc.Driver",
            user = Config.sqlUser,
            password = Config.sqlPwd
        )

        log.debug("Creating Missing Tables")
        transaction {
            loadClasses("me.din0s.sql.tables", Table::class.java).forEach {
                SchemaUtils.createMissingTablesAndColumns(it)
            }
        }

        log.info("Registering SQL Managers")
        loadClasses("me.din0s.sql.managers", ISqlManager::class.java).forEach {
            log.debug("Registering {}", it.javaClass.simpleName)
            it.preReady()
        }
    }

    /**
     * Invokes the [ISqlManager.postReady] function of all SQL managers.
     */
    fun postReady() {
        log.info("Registering post-ready SQL Managers")
        loadClasses("me.din0s.sql.managers", ISqlManager::class.java).forEach {
            log.debug("Registering {}", it.javaClass.simpleName)
            it.postReady()
        }
    }
}
