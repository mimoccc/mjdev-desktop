package eu.mjdev.desktop.components.fonticon

import eu.mjdev.desktop.helpers.ResourceStream

class CodePointsFile(
    private val codepointsFileName: String,
    private val codepointsResourceResource: ResourceStream = ResourceStream(codepointsFileName),

    ) {
    val icons: Map<String, Int> by lazy {
        codepointsResourceResource.string.split("\n")
            .map { it.split(" ") }
            .mapNotNull { if (it.size == 2) Pair(it[0], it[1].toInt(radix = 16)) else null }
            .toMap()
    }
}