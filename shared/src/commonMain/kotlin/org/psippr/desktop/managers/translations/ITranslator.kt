package org.psippr.desktop.managers.translations

import org.psippr.desktop.managers.base.IDelegate

interface ITranslator : IDelegate {
    companion object {
        val EMPTY = object : ITranslator {}
    }
}
