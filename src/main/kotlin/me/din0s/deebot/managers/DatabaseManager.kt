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

package me.din0s.deebot.managers

import me.din0s.deebot.Config
import me.din0s.deebot.getObject
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.reflections.Reflections

object DatabaseManager {
    private val log = LogManager.getLogger(DatabaseManager::class.java)

    fun init() {
        log.info("Connecting to SQL database...")
        Database.connect(
            url = "jdbc:mysql://localhost/${Config.sqlDb}?useUnicode=true&characterEncoding=utf8",
            driver = "org.mariadb.jdbc.Driver",
            user = Config.sqlUser,
            password = Config.sqlPwd
        )

        transaction {
            Reflections("me.din0s.deebot.entities.sql")
                .getSubTypesOf(Table::class.java)
                .forEach {
                    val instance = it.getObject() as Table
                    SchemaUtils.createMissingTablesAndColumns(instance)
                }

            Reflections("me.din0s.deebot.managers")
                .getSubTypesOf(ISqlManager::class.java)
                .forEach {
                    val instance = it.getObject() as ISqlManager
                    instance.init()
                }
        }
    }
}
