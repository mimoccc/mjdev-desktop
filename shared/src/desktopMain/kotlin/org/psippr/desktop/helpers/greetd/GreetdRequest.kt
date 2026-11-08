package org.psippr.desktop.helpers.greetd

/**
 * One greetd IPC request; fields not used by a [type] stay null and are left out of the JSON.
 *
 * @property type the [GreetdMessageType.value] of the request
 * @property username user to log in, for [GreetdMessageType.CreateSession]
 * @property response answer, for [GreetdMessageType.PostAuthMessageResponse]
 * @property cmd session command line, for [GreetdMessageType.StartSession]
 * @property env extra session environment (`KEY=value`), for [GreetdMessageType.StartSession]
 */
data class GreetdRequest(
    val type: String,
    val username: String? = null,
    val response: String? = null,
    val cmd: List<String>? = null,
    val env: List<String>? = null,
)
