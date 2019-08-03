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

package me.din0s;

/**
 * This enum contains variables available to all other classes.
 *
 * @author Dinos Papakostas
 */
@SuppressWarnings("unused")
public enum Variables {
    /**
     * The Discord ID of the developer who wrote this bot (that's me!).
     */
    DEV_ID("98457903660269568"),

    /**
     * An invite to the bot's support server.
     */
    SERVER_INVITE("https://discord.gg/0wEZsVCXid2URhDY"),

    /**
     * A placeholder for the bot's current version.
     * This gets replaced from the gradle build script.
     */
    VERSION("@version@");

    private final String value;

    /**
     * Constructor.
     *
     * @param value The value to set for the variable.
     */
    Variables(String value) {
        this.value = value;
    }

    /**
     * Returns the value of this variable.
     *
     * @return The variable's value.
     */
    public String getValue() {
        return value;
    }
}
