package com.excell44.educam.ui.components

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * WebView optimisée pour afficher du contenu riche (HTML, formules MathJax/KaTeX, images).
 * Supporte le rendu LaTeX pour les Mathématiques, la Physique et la Chimie.
 */
@Composable
fun LatexWebView(
    content: String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false // Pour adapter le CSS si besoin
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                
                // Optimisation pour le rendu
                settings.domStorageEnabled = true
                
                // Fond transparent pour s'intégrer à l'UI
                setBackgroundColor(0x00000000)
                
                webViewClient = android.webkit.WebViewClient()
                loadDataWithBaseURL(null, wrapHTMLContent(content, isDarkTheme), "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            // Mise à jour du contenu si nécessaire
            webView.loadDataWithBaseURL(null, wrapHTMLContent(content, isDarkTheme), "text/html", "UTF-8", null)
        },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
    )
}

/**
 * Enveloppe le contenu HTML avec du CSS pour le style et KaTeX pour le rendu mathématique.
 * Inclut les bibliothèques KaTeX via CDN (ou local si configuré).
 */
private fun wrapHTMLContent(content: String, isDarkTheme: Boolean): String {
    val textColor = if (isDarkTheme) "#E0E0E0" else "#1A1A1A"
    
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css">
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/mhchem.min.js"></script>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    renderMathInElement(document.body, {
                        delimiters: [
                            {left: "$$", right: "$$", display: true},
                            {left: "$", right: "$", display: false},
                            {left: "\\(", right: "\\)", display: false},
                            {left: "\\[", right: "\\]", display: true}
                        ],
                        macros: {
                            "\\ce": "\\ce"
                        }
                    });
                });
            </script>
            <style>
                body {
                    font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
                    padding: 8px;
                    margin: 0;
                    font-size: 16px;
                    line-height: 1.6;
                    color: $textColor;
                    background-color: transparent;
                }
                img {
                    max-width: 100%;
                    height: auto;
                    border-radius: 8px;
                }
                .katex {
                    font-size: 1.1em;
                }
                p {
                    margin: 0 0 8px 0;
                }
            </style>
        </head>
        <body>
            $content
        </body>
        </html>
    """.trimIndent()
}
