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

package me.din0s.util

import me.din0s.deebot.cmds.Command
import org.apache.logging.log4j.Logger
import org.reflections.Reflections
import java.lang.reflect.Modifier

/**
 * Converts a class representation to its object.
 *
 * @return The singleton object for this class.
 */
fun <T> Class<out T>.getObject() : Any? {
    return getDeclaredField("INSTANCE").get(null)
}

/**
 * Loads dynamically all subclasses specified and adds the objects
 * created to a sorted list. Optionally applies a filter to the results.
 *
 * @param path The path that the classes are located in.
 * @param clazz The class that the objects should extend.
 * @return A list of all valid objects contained within the path.
 */
fun <T> loadClasses(path: String, clazz: Class<out T>) : List<T> {
    return Reflections(path).getSubTypesOf(clazz)
        .filter { !Modifier.isAbstract(it.modifiers) }
        .sortedBy { it.simpleName }
        .mapNotNull { it.getObject() }
        .toList() as List<T>
}

/**
 * Returns a map of labels and the commands they correspond to.
 *
 * @param log The logger instance to use in this process.
 * @return A map with all of the commands from the list.
 */
fun List<Command>.register(log: Logger) : Map<String, Command> {
    val map = mutableMapOf<String, Command>()
    forEach {
        log.debug("Registering {}", it.name)
        map[it.name] = it
        log.trace("{} -> {}", it, it.name)
        it.alias.forEach { alias ->
            if (map.containsKey(alias)) {
                log.warn("Tried to register {}, which already existed!")
            } else {
                map[alias] = it
                log.trace("{} -> {}", it, alias)
            }
        }
    }
    return map
}
