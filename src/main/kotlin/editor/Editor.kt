package editor

import java.awt.*
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.text.*
import kotlin.system.exitProcess

fun main() {
	Editor()
}

class Editor : JFrame() {
	private val mainPanel = JPanel(BorderLayout(5, 5))
	private val editorPane: JTextPane
	private val doc: StyledDocument

	private var backgroundColor = Color(0x101010)
	private var sliderColor = Color(0x303030)
	private var sliderButtonColor = Color(0x000000)
	private var fontColor = Color(0xFFFFFF)
	private var variableColor = Color(0x93B3F5)
	private var keywordColor = Color(0xCB633C)
	private var operatorColor = Color(0x55E744)
	private var bracketColor = Color(0xE0E0E0)
	private var focusedBracketColor = Color(0x707070)
	private var commentColor = Color(0x5E5E5E)
	private var literalColor = Color(0x53914C)
	private var numberColor = Color(0x1A6ABE)
	private var editorCaretColor = Color(0xFFF200)

	private var settingsFile = File("")
	private var settingsMap = mapOf<String, String>()
	private var autoSaveFile = File("")
	private var jarPath = ""
	private var flag = 'f'
	private var runPath = ""
	private var editorFont = Font("Cascadia Code", Font.PLAIN, 14)
	private var autoSaveTimer = 2000
	private var tabSize = 4
	private var args = ""
	private var currentFile = File("")

	init {
		loadSettings()

		layout = BorderLayout(5, 5)
		minimumSize = Dimension(600, 400)
		background = backgroundColor
		font = editorFont
		iconImage = ImageIcon("config/hasle.png").image

		mainPanel.background = backgroundColor
		add(mainPanel, BorderLayout.CENTER)

		val editorPanel = JPanel(BorderLayout(5, 5))
		editorPanel.background = backgroundColor
		editorPanel.preferredSize = Dimension(1000, 750)

		editorPane = createTextPane()
		editorPane.foreground = fontColor
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
				/* Unused */
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

		var time = System.currentTimeMillis()
		fun update() {
			if (System.currentTimeMillis() - time >= autoSaveTimer) {
				autoSaveFile.writeText(doc.getText(0, doc.length))
				time = System.currentTimeMillis()
			}
		}
		doc.addDocumentListener(object : DocumentListener {
			override fun insertUpdate(e: DocumentEvent?) {
				update()
			}

			override fun removeUpdate(e: DocumentEvent?) {
				update()
			}

			override fun changedUpdate(e: DocumentEvent?) {
				/* Unused */
			}
		})

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
		var prevTime = System.currentTimeMillis()
		val highlighter = object : SwingWorker<Any?, Any?>() {
			override fun doInBackground(): Any? {
				val run = true
				while (run) {
					if (editorPane.text != prevText || editorPane.caret.dot != prevDot) {
						highlightText()
						prevText = doc.getText(0, doc.length)
						prevDot = editorPane.caret.dot
					}

					val currentTime = System.currentTimeMillis()
					if (currentTime - prevTime >= 500L) {
						prevTime = currentTime
						highlightText()
					}
				}
				return null
			}
		}
		highlighter.execute()

		editorPane.text = currentFile.readText()
		title = "Hasle ${currentFile.path}"
		highlightText()
	}

	private fun loadSettings() {
		settingsFile = File("config/settings.txt")
		settingsMap = settingsFile.readLines().associate {
			val line = it.trim()
			if (line.isEmpty()) {
				"" to ""
			} else {
				val index = line.indexOf(":")

				if (index == -1) {
					"" to ""
				} else {
					if (line[index + 1] == '"' && line.last() == '"') {
						line.take(index).trim() to line.substring(index + 2).dropLast(1).trim()
					} else {
						line.take(index).trim() to line.substring(index + 1).trim()
					}
				}
			}
		}
		autoSaveFile = File(settingsMap["autosave"] ?: "")
		jarPath = settingsMap["jar"] ?: ""
		flag = (settingsMap["flag"] ?: "f").first()
		runPath = settingsMap["run"] ?: ""
		editorFont = Font(
			settingsMap["font-family"] ?: "Cascadia Code",
			Font.PLAIN,
			(settingsMap["font-size"] ?: "14").toInt()
		)
		autoSaveTimer = (settingsMap["autosave-timer"] ?: "2000").toInt()
		tabSize = (settingsMap["tab-size"] ?: "4").toInt()
		args = settingsMap["args"] ?: ""
		currentFile = autoSaveFile
		backgroundColor = loadColor("backgroundColor") ?: backgroundColor
		sliderColor = loadColor("sliderColor") ?: sliderColor
		sliderButtonColor = loadColor("sliderButtonColor") ?: sliderButtonColor
		fontColor = loadColor("fontColor") ?: fontColor
		variableColor = loadColor("variableColor") ?: variableColor
		keywordColor = loadColor("keywordColor") ?: keywordColor
		operatorColor = loadColor("operatorColor") ?: operatorColor
		bracketColor = loadColor("bracketColor") ?: bracketColor
		focusedBracketColor = loadColor("focusedBracketColor") ?: focusedBracketColor
		commentColor = loadColor("commentColor") ?: commentColor
		literalColor = loadColor("literalColor") ?: literalColor
		numberColor = loadColor("numberColor") ?: numberColor
		editorCaretColor = loadColor("caretColor") ?: editorCaretColor
	}

	private fun loadColor(name: String): Color? {
		with(settingsMap[name]) {
			return if (this != null) {
				Color(Integer.decode(this))
			} else {
				null
			}
		}
	}

	private fun createMenuBar() {
		val toolBar = JPanel()
		toolBar.maximumSize = Dimension(Int.MAX_VALUE, 30)
		toolBar.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
		toolBar.background = backgroundColor
		mainPanel.add(toolBar, BorderLayout.SOUTH)

		val openItem = createToolBarItem("Open", 'o')
		openItem.addActionListener {
			val dialog = FileDialog(this, "Select File to Open")
			dialog.mode = FileDialog.LOAD
			dialog.isVisible = true
			val filePath = dialog.directory + dialog.file
			dialog.dispose()
			currentFile = File(filePath)
			editorPane.text = currentFile.readText()
			title = "Hasle ${currentFile.path}"
			highlightText()
		}
		toolBar.add(openItem)

		val saveItem = createToolBarItem("Save", 's')
		saveItem.addActionListener {
			currentFile.writeText(doc.getText(0, doc.length))
		}
		toolBar.add(saveItem)

		val saveAsItem = createToolBarItem("Save As", 'a')
		saveAsItem.addActionListener {
			val dialog = FileDialog(this, "Select File to Save to")
			dialog.mode = FileDialog.SAVE
			dialog.isVisible = true
			val filePath = dialog.directory + dialog.file
			dialog.dispose()
			currentFile = File(filePath)
			title = "Hasle ${currentFile.path}"
			currentFile.writeText(doc.getText(0, doc.length))
		}
		toolBar.add(saveAsItem)

		val runItem = createToolBarItem("Run", 'r')
		runItem.addActionListener {
			autoSaveFile.writeText(doc.getText(0, doc.length))
			Runtime.getRuntime().exec(
				arrayOf(
					runPath,
					currentFile.name,
					"java -jar $jarPath $flag ${currentFile.path} $args"
				)
			)
		}
		toolBar.add(runItem)

		val argsItem = createToolBarItem("Args", 'g')
		argsItem.addActionListener {
			args = JOptionPane.showInputDialog(
				this,
				"<html>Program arguments: \"$args\"<br>New program arguments:</html>"
			) ?: args
		}
		toolBar.add(argsItem)

		val flagItem = createToolBarItem("Flag: $flag", 'l')
		flagItem.addActionListener {
			if (flag == 'f') {
				flag = 'd'
			} else if (flag == 'd') {
				flag = 'f'
			}
			flagItem.text = "Flag: $flag"
		}
		toolBar.add(flagItem)

		val refreshItem = createToolBarItem("Refresh", 'f')
		refreshItem.addActionListener { highlightText() }
		toolBar.add(refreshItem)

		val settingsItem = createToolBarItem("Settings", 'e')
		settingsItem.addActionListener {
			Runtime.getRuntime().exec(arrayOf("Notepad", "config/settings.txt"))
		}
		toolBar.add(settingsItem)
	}

	private fun createToolBarItem(label: String, mnemonic: Char): JButton {
		val item = JButton(label)
		item.mnemonic = KeyEvent.getExtendedKeyCodeForChar(mnemonic.code)
		item.font = editorFont
		item.foreground = fontColor
		item.background = backgroundColor
		return item
	}

	private fun createScrollPane(content: JComponent): JScrollPane {
		val scrollPane = JScrollPane(content)
		scrollPane.background = backgroundColor
		scrollPane.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
		scrollPane.verticalScrollBar.background = backgroundColor
		scrollPane.horizontalScrollBar.background = backgroundColor
		scrollPane.verticalScrollBar.unitIncrement = 16
		scrollPane.horizontalScrollBar.unitIncrement = 8
		class ScrollBarUI : BasicScrollBarUI() {
			override fun configureScrollBarColors() {
				scrollBarWidth = 15
				thumbColor = sliderColor
			}

			override fun createDecreaseButton(orientation: Int): JButton? {
				val button = super.createDecreaseButton(orientation)
				button.background = sliderButtonColor
				button.foreground = sliderButtonColor
				return button
			}

			override fun createIncreaseButton(orientation: Int): JButton? {
				val button = super.createIncreaseButton(orientation)
				button.background = sliderButtonColor
				button.foreground = sliderButtonColor
				return button
			}
		}
		scrollPane.verticalScrollBar.setUI(ScrollBarUI())
		scrollPane.horizontalScrollBar.setUI(ScrollBarUI())
		return scrollPane
	}

	private fun createTextPane(): JTextPane {
		val textPane = object : JTextPane() {
			override fun getBackground(): Color = backgroundColor
			override fun getCaretColor(): Color = editorCaretColor

			override fun paintComponent(g: Graphics?) {
				super.paintComponent(g)
				val g2d = g as Graphics2D
				g2d.font = editorFont
				g2d.color = commentColor
				val fm = g2d.fontMetrics
				val lineCount = text.lines().size
				val width = fm.stringWidth(lineCount.toString())
				val lineHeight = fm.height
				val maxAscent = fm.maxAscent
				val baseLine = 20 + maxAscent
				border = BorderFactory.createEmptyBorder(20, 20 + width, 20, 20)
				repeat(lineCount) {
					val n = (it + 1).toString()
					g2d.drawString(n, 10 + width - fm.stringWidth(n), baseLine + lineHeight * it)
				}
			}
		}

		val doc = textPane.styledDocument
		val def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE)

		(doc as AbstractDocument).documentFilter = object : DocumentFilter() {
			override fun replace(fb: FilterBypass?, offset: Int, length: Int, text: String, attrs: AttributeSet?) {
				super.insertString(fb, offset, text.replace("\t", " ".repeat(tabSize)), attrs)
			}
		}

		val regular = doc.addStyle("regular", def)
		StyleConstants.setFontFamily(def, editorFont.family)
		StyleConstants.setForeground(def, fontColor)
		StyleConstants.setFontSize(def, editorFont.size)

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
					"main", "while", "if", "else", "class", "fun" -> "keyword"
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
					if (index + 1 < length && sourceCode[index] == '\\' &&
					    "\"\\\t\b\r\n".contains(sourceCode[index + 1])) {
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
