package org.psippr.desktop.system

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
