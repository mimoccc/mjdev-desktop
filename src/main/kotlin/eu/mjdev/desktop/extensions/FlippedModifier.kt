/*
 * Copyright (c) Milan Jurkulák 2024.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 */

package eu.mjdev.desktop.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale

class FlippedModifier : DrawModifier {
    override fun ContentDrawScope.draw() {
        scale(1f, -1f) {
            this@draw.drawContent()
        }
    }
}

fun Modifier.flipped() = this.then(FlippedModifier())