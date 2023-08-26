package editor

import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*
import javax.swing.border.Border
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.plaf.basic.BasicSplitPaneDivider
import javax.swing.plaf.basic.BasicSplitPaneUI
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import javax.swing.text.StyledDocument
import kotlin.system.exitProcess

fun main() {
    Editor()
}

class Editor : JFrame("Hasle - programs/autosave.txt") {
    private val mainPanel = JPanel(BorderLayout(5, 5))
    private val editorPane: JTextPane
    private val doc: StyledDocument

    private val backgroundColor = Color(0x101010)
    private val sliderColor = Color(0x141414)
    private val variableColor = Color(0x93B3F5)
    private val keywordColor = Color(0xCB633C)
    private val operatorColor = Color(0x55E744)
    private val bracketColor = Color(0xE0E0E0)
    private val focusedBracketColor = Color(0x707070)
    private val commentColor = Color(0x5E5E5E)
    private val literalColor = Color(0x53914C)
    private val numberColor = Color(0x1A6ABE)
    private val caretColor = Color(0xFFF200)

    private val editorFont = Font("Cascadia Code", Font.PLAIN, 14)
    private val autoSaveTimer = 2_000
    private val tabSize = 4

    private val autoSaveFile = File("programs/autosave.txt")

    init {
        layout = BorderLayout(5, 5)
        minimumSize = Dimension(600, 400)
        background = backgroundColor
        font = editorFont

        mainPanel.background = backgroundColor
        add(mainPanel, BorderLayout.CENTER)

        val editorPanel = JPanel(BorderLayout(5, 5))
        editorPanel.background = backgroundColor
        editorPanel.preferredSize = Dimension(1000, 850)

        editorPane = createTextPane()
        editorPane.foreground = Color.RED
        doc = editorPane.styledDocument
        editorPane.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent?) {
                if (e != null && e.keyChar == '\n') {
                    val text = doc.getText(0, doc.length)
                    val pos = editorPane.caretPosition
                    val line = text.substring(0, pos - 1).split("\n").last()
                    var indentCount = line.length - line.trimStart().length
                    if (pos > 2 && text[pos - 2] == '{') {
                        indentCount += tabSize
                    }
                    val indent = " ".repeat(indentCount)
                    doc.insertString(pos, indent, doc.getStyle("regular"))
                    highlightText()
                }
            }

            override fun keyPressed(e: KeyEvent?) {
                /* unused */
            }

            override fun keyReleased(e: KeyEvent?) {
                if (e != null && e.keyChar == '}') {
                    val pos = editorPane.caretPosition
                    val first = doc.getText(0, pos - 1)
                    val second = doc.getText(pos - 1, doc.length + 1 - pos)
                    if (first.endsWith(" ".repeat(tabSize))) {
                        editorPane.text = first.dropLast(tabSize) + second
                        editorPane.caretPosition = pos - tabSize
                        highlightText()
                    }
                }
            }
        })

        val lineWrapWrapper = JPanel(BorderLayout())
        lineWrapWrapper.background = backgroundColor
        lineWrapWrapper.add(editorPane, BorderLayout.CENTER)

        val editorScrollPane = createScrollPane(lineWrapWrapper)
        editorPanel.add(editorScrollPane, BorderLayout.CENTER)

        mainPanel.add(editorPanel, BorderLayout.CENTER)

        createMenuBar()

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                autoSaveFile.writeText(doc.getText(0, doc.length))
                exitProcess(0)
            }
        })
        pack()
        setLocationRelativeTo(null)
        isVisible = true

        var prevText = ""
        var prevDot = 0
        var time = System.currentTimeMillis()
        val highlighter = object : SwingWorker<Any?, Any?>() {
            override fun doInBackground(): Any? {
                val run = true
                while (run) {
                    if (editorPane.text != prevText || editorPane.caret.dot != prevDot) {
                        highlightText()
                        prevText = doc.getText(0, doc.length)
                        prevDot = editorPane.caret.dot

                        if (System.currentTimeMillis() - time >= autoSaveTimer) {
                            autoSaveFile.writeText(doc.getText(0, doc.length))

                            time = System.currentTimeMillis()
                        }
                    }
                }
                return null
            }
        }
        highlighter.execute()

        editorPane.text = autoSaveFile.readText()
        highlightText()
    }

    private fun createMenuBar() {
        val menuBar = JMenuBar()
        menuBar.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        menuBar.background = backgroundColor
        mainPanel.add(menuBar, BorderLayout.SOUTH)

        val fileMenu = createMenu("File", 'f')
        menuBar.add(fileMenu)

        val openItem = createMenuItem("Open", 'o')
        fileMenu.add(openItem)

        val saveItem = createMenuItem("Save", 's')
        fileMenu.add(saveItem)

        val saveAsItem = createMenuItem("Save As", 'a')
        fileMenu.add(saveAsItem)

        val runMenu = createMenu("Run", 'r')
        menuBar.add(runMenu)

        val runItem = createMenuItem("Run", 'r')
        runMenu.add(runItem)

        val terminateItem = createMenuItem("Terminate", 't')
        runMenu.add(terminateItem)

        val viewEditor = createMenu("View", 'v')
        menuBar.add(viewEditor)

        val refreshItem = createMenuItem("Refresh", 'r')
        viewEditor.add(refreshItem)

        val autoScrollItem = createMenuItem("Auto scroll: OFF", 'a')
        viewEditor.add(autoScrollItem)
    }

    private fun createSplitPane(leftPanel: JPanel, rightPanel: JPanel): JSplitPane {
        val splitPane = JSplitPane(SwingConstants.VERTICAL, leftPanel, rightPanel)
        splitPane.background = backgroundColor
        splitPane.border = BorderFactory.createEmptyBorder()
        splitPane.setUI(object : BasicSplitPaneUI() {
            override fun createDefaultDivider(): BasicSplitPaneDivider {
                return object : BasicSplitPaneDivider(this) {
                    override fun setBorder(b: Border) {
                        /* unused */
                    }

                    override fun paint(g: Graphics) {
                        g.color = sliderColor
                        g.fillRect(0, 0, size.width, size.height)
                        super.paint(g)
                    }
                }
            }
        })
        return splitPane
    }

    private fun createMenu(label: String, mnemonic: Char): JMenu {
        val menu = JMenu(label)
        menu.mnemonic = KeyEvent.getExtendedKeyCodeForChar(mnemonic.code)
        menu.font = editorFont
        menu.foreground = Color.WHITE
        menu.background = backgroundColor
        return menu
    }

    private fun createMenuItem(label: String, mnemonic: Char): JMenuItem {
        val item = JMenuItem(label)
        item.mnemonic = KeyEvent.getExtendedKeyCodeForChar(mnemonic.code)
        item.font = editorFont
        item.foreground = Color.WHITE
        item.background = backgroundColor
        return item
    }

    private fun createScrollPane(content: JComponent): JScrollPane {
        val scrollPane = JScrollPane(content)
        scrollPane.background = backgroundColor
        scrollPane.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        scrollPane.verticalScrollBar.background = backgroundColor
        scrollPane.horizontalScrollBar.background = backgroundColor
        class ScrollBarUI : BasicScrollBarUI() {
            override fun configureScrollBarColors() {
                scrollBarWidth = 15
                thumbColor = sliderColor
            }

            override fun createDecreaseButton(orientation: Int): JButton? {
                val button = super.createDecreaseButton(orientation)
                button.background = Color.BLACK
                button.foreground = Color.BLACK
                return button
            }

            override fun createIncreaseButton(orientation: Int): JButton? {
                val button = super.createIncreaseButton(orientation)
                button.background = Color.BLACK
                button.foreground = Color.BLACK
                return button
            }
        }
        scrollPane.verticalScrollBar.setUI(ScrollBarUI())
        scrollPane.horizontalScrollBar.setUI(ScrollBarUI())
        return scrollPane
    }

    private fun createTextPane(): JTextPane {
        val textPane = JTextPane()
        textPane.background = backgroundColor
        textPane.border = BorderFactory.createEmptyBorder(10, 20, 10, 20)
        textPane.caretColor = caretColor

        val doc = textPane.styledDocument
        val def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE)

        val regular = doc.addStyle("regular", def)
        StyleConstants.setFontFamily(def, "Cascadia Code")
        StyleConstants.setForeground(def, Color.WHITE)
        StyleConstants.setFontSize(def, 14)

        var style = doc.addStyle("variable", regular)
        StyleConstants.setForeground(style, variableColor)

        style = doc.addStyle("keyword", regular)
        StyleConstants.setForeground(style, keywordColor)

        style = doc.addStyle("operator", regular)
        StyleConstants.setForeground(style, operatorColor)

        style = doc.addStyle("bracket", regular)
        StyleConstants.setForeground(style, bracketColor)

        style = doc.addStyle("focusedBracket", regular)
        StyleConstants.setForeground(style, focusedBracketColor)
        StyleConstants.setItalic(style, true)

        style = doc.addStyle("comment", regular)
        StyleConstants.setForeground(style, commentColor)
        StyleConstants.setItalic(style, true)

        style = doc.addStyle("literal", regular)
        StyleConstants.setForeground(style, literalColor)

        style = doc.addStyle("number", regular)
        StyleConstants.setForeground(style, numberColor)

        return textPane
    }

    private fun highlightText() {
        val sourceCode = doc.getText(0, doc.length)
        val length = sourceCode.length

        var focusedBracket: Int? = null
        var index = 0
        while (index < length) {
            val currentChar = sourceCode[index]
            val startingIndex = index

            var type = when (currentChar) {
                '$', '!', '~', '@', '.' -> "operator"

                '(', '{', '[' -> {
                    if (editorPane.caret.dot == index + 1) {
                        var lookAhead = index + 1
                        var count = 1
                        while (lookAhead < length && count != 0) {
                            when (sourceCode[lookAhead]) {
                                '(', '{', '[' -> count++
                                ')', '}', ']' -> count--
                            }

                            if (count == 0) {
                                focusedBracket = lookAhead
                            } else {
                                lookAhead++
                            }
                        }

                        "focusedBracket"
                    } else {
                        "bracket"
                    }
                }

                ')', '}', ']' -> {
                    if (editorPane.caret.dot == index) {
                        var lookBehind = index - 1
                        var count = 1
                        while (lookBehind >= 0 && count != 0) {
                            when (sourceCode[lookBehind]) {
                                ')', '}', ']' -> count++
                                '(', '{', '[' -> count--
                            }

                            if (count == 0) {
                                focusedBracket = lookBehind
                            } else {
                                lookBehind--
                            }
                        }

                        "focusedBracket"
                    } else {
                        "bracket"
                    }
                }

                else -> "regular"
            }

            if (currentChar == '#') {
                while (index < length && sourceCode[index] != '\n') {
                    index++
                }

                type = "comment"
            }

            if (index + 1 < length &&
                (
                        (currentChar == '&' && sourceCode[index + 1] == '&') ||
                                (currentChar == '|' && sourceCode[index + 1] == '|')
                        )
            ) {
                index++
                type = "operator"
            }

            if (currentChar == '<') {
                if (index + 1 < length && (sourceCode[index + 1] == '=' || sourceCode[index + 1] == '-')) {
                    index++
                }
                type = "operator"
            }

            if (currentChar == '+') {
                if (index + 1 < length && (sourceCode[index + 1] == '+' || sourceCode[index + 1] == '=')) {
                    index++
                }
                type = "operator"
            }

            if (currentChar == '-') {
                if (index + 1 < length) {
                    if (sourceCode[index + 1].isDigit()) {
                        type = "number"
                    } else if (sourceCode[index + 1] == '-' || sourceCode[index + 1] == '=') {
                        index++
                        type = "operator"
                    }
                } else {
                    type = "operator"
                }
            }

            when (currentChar) {
                '!', '=', '>', '*', '/', '%', '^' -> {
                    if (index + 1 < length && sourceCode[index + 1] == '=') {
                        index++
                    }
                    type = "operator"
                }
            }

            if (currentChar.isLetter()) {
                var value = ""
                while (index < length && sourceCode[index].isLetterOrDigit()) {
                    value += sourceCode[index]
                    index++
                }
                index--

                type = when (value) {
                    "main", "while", "if", "else", "struct" -> "keyword"
                    "true", "false" -> "number"
                    else -> if (index + 1 < length && sourceCode[index + 1] == '(') {
                        "regular"
                    } else {
                        "variable"
                    }
                }
            }

            if (index + 1 < length && currentChar == '"') {
                index++
                while (index < length && sourceCode[index] != '"') {
                    if (index + 1 < length && sourceCode[index] == '\\' && sourceCode[index + 1] == '"') {
                        index++
                    }

                    index++
                }

                type = "literal"
            }

            if (index + 2 < length && currentChar == '\'') {
                if (index + 3 < length &&
                    sourceCode[index + 1] == '\\' &&
                    sourceCode[index + 3] == '\''
                ) {
                    index++
                }

                if (sourceCode[index + 2] == '\'') {
                    index += 2

                    type = "literal"
                }
            }

            if (currentChar.isDigit()) {
                while (index < length && sourceCode[index].toString().matches(Regex("[0-9._]"))) {
                    index++
                }
                index--

                type = "number"
            }

            index++
            doc.setCharacterAttributes(startingIndex, index - startingIndex, doc.getStyle(type), true)
        }

        if (focusedBracket != null) {
            doc.setCharacterAttributes(focusedBracket, 1, doc.getStyle("focusedBracket"), true)
        }
    }
}
