package org.mjdev.desktop.system

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
