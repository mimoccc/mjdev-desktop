package org.mjdev.desktop.managers.translations

import org.mjdev.desktop.managers.base.IDelegate

interface ITranslator : IDelegate {

    companion object {
        val EMPTY = object : ITranslator {}
    }
}
