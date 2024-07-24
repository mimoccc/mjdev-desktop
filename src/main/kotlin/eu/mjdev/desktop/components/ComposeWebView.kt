package eu.mjdev.desktop.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import javax.swing.JEditorPane
import javax.swing.JScrollPane

@Suppress("FunctionName")
@Preview
@Composable
fun ComposeWebView(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    url: String = "https://google.com",
    onLinkClicked: (w: JEditorPane, href: String?) -> Unit = { w, href ->
        w.setPage(href)
    }
) = SwingPanel(
    background = backgroundColor,
    modifier = modifier,
    factory = {
        val jed = JEditorPane().apply {
            isEditable = false
            addHyperlinkListener { hev ->
                onLinkClicked(this@apply, hev?.url?.toString())
            }
            setPage(url)
        }
        JScrollPane(
            jed,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        )
    }
)