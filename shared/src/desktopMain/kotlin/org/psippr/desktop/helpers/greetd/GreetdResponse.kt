package org.psippr.desktop.helpers.greetd

import com.google.gson.annotations.SerializedName

/**
 * One greetd IPC response.
 *
 * @property type the [GreetdMessageType.value] of the response
 * @property errorType failure kind of an [GreetdMessageType.Error] response
 * @property description failure text of an [GreetdMessageType.Error] response
 * @property authMessageType [GreetdAuthMessageType.value] of an [GreetdMessageType.AuthMessage]
 * @property authMessage text of an [GreetdMessageType.AuthMessage] (PAM prompt)
 */
data class GreetdResponse(
    val type: String = "",
    @SerializedName("error_type") val errorType: String? = null,
    val description: String? = null,
    @SerializedName("auth_message_type") val authMessageType: String? = null,
    @SerializedName("auth_message") val authMessage: String? = null,
)
