/*
Copyright (C) 2022-2023  PsiCodes

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.wildzeus.pythonktx.Activities
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.termoneplus.TermActivity
import com.wildzeus.pythonktx.DataStore.SettingsDataStore
import com.wildzeus.pythonktx.R
import com.wildzeus.pythonktx.Utils.Commands
import com.wildzeus.pythonktx.Utils.CrashHandler
import com.wildzeus.pythonktx.ViewModels.EditorActivityViewModel
import com.wildzeus.pythonktx.databinding.ActivityEditorBinding
import com.wildzeus.pythonktx.ui.theme.EditorTheme
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.EditorKeyEvent
import io.github.rosemoe.sora.event.KeyBindingEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.event.SideIconClickEvent
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.text.LineSeparator
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.EditorSearcher
import io.github.rosemoe.sora.widget.component.Magnifier
import io.github.rosemoe.sora.widget.style.builtin.ScaleCursorAnimator
import io.github.rosemoe.sora.widget.subscribeEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.regex.PatternSyntaxException


class EditorActivity : AppCompatActivity() {
    private lateinit var binding:ActivityEditorBinding
    private var undo: MenuItem? = null
    private lateinit var mEditorActivityViewModelFactory: EditorActivityViewModelFactory
    private var redo: MenuItem? = null
    private lateinit var mUserViewModel: EditorActivityViewModel
    private lateinit var file: File
    private lateinit var filename:String
    private lateinit var dataStore: SettingsDataStore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       CrashHandler.INSTANCE.init(this)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val inputView = binding.symbolInput
        inputView.bindEditor(binding.editor)
        file= intent.getStringExtra("fileUrl")?.let {
            File(it)
        }!!
        filename=file.name.toString()
        dataStore= SettingsDataStore(applicationContext)
        mEditorActivityViewModelFactory= EditorActivityViewModelFactory(file)
        mUserViewModel= ViewModelProvider(this,mEditorActivityViewModelFactory)[EditorActivityViewModel::class.java]
        val typeface = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
        inputView.addSymbols(
            arrayOf(
                "->",
                "{",
                "}",
                "(",
                ")",
                ",",
                ".",
                ";",
                "\"",
                "?",
                "+",
                "-",
                "*",
                "/",
                "!",
                "&",
                "#",
                "=",
                "-"
            ), arrayOf("\t", "{}", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/","!","&","#","=", "-")
        )
        inputView.forEachButton {
            it.typeface = typeface
        }
        binding.searchEditor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotEmpty()) {
                    try {
                        binding.editor.searcher.search(
                            editable.toString(),
                            EditorSearcher.SearchOptions(true, true)
                        )
                    } catch (e: PatternSyntaxException) {
                        // Regex error
                    }
                } else {
                    binding.editor.searcher.stopSearch()
                }
            }
        })
        binding.editor.apply {
            typefaceText = typeface
            setLineSpacing(2f, 1.1f)
            cursorAnimator = ScaleCursorAnimator(this)
            nonPrintablePaintingFlags =
                CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR or CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION
            // Update display dynamically
            subscribeEvent<SelectionChangeEvent> { _, _ -> updatePositionText() }
            subscribeEvent<ContentChangeEvent> { _, _ ->
                postDelayed(
                    ::updateBtnState,
                    50
                )
            }
            subscribeEvent<SideIconClickEvent> { _, _ ->
                Toast.makeText(this@EditorActivity, "Side icon clicked", Toast.LENGTH_SHORT).show()
            }

            subscribeEvent<KeyBindingEvent> { event, _ ->
                if (event.eventType != EditorKeyEvent.Type.DOWN) {
                    return@subscribeEvent
                }
                Toast.makeText(
                    context,
                    "Keybinding event: " + generateKeybindingString(event),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.fab.setOnClickListener{
            saveFile()
            val intent=Intent(this, TermActivity::class.java)
            intent.putExtra("Command", Commands.makeCommand(this,file.path))
            startActivity(intent)
        }
        val editor = binding.editor
        editor.setText(mUserViewModel.text)
        updatePositionText()
        updateBtnState()
        CoroutineScope(Dispatchers.Default).launch{
            setTheme(dataStore.mThemeString.first() ?: EditorTheme.QuietLight)
        }
    }

    private fun generateKeybindingString(event: KeyBindingEvent): String {
        val sb = StringBuilder()
        if (event.isCtrlPressed) {
            sb.append("Ctrl + ")
        }

        if (event.isAltPressed) {
            sb.append("Alt + ")
        }

        if (event.isShiftPressed) {
            sb.append("Shift + ")
        }

        sb.append(KeyEvent.keyCodeToString(event.keyCode))
        return sb.toString()
    }
    private fun updateBtnState() {
        undo?.isEnabled = binding.editor.canUndo()
        redo?.isEnabled = binding.editor.canRedo()
    }

    private fun updatePositionText() {
        val cursor = binding.editor.cursor
        var text=""
        text += if (cursor.isSelected) {
            "(" + (cursor.right - cursor.left) + " chars)"
        } else {
            val content = binding.editor.text
            if (content.getColumnCount(cursor.leftLine) == cursor.leftColumn) {
                "(<" + content.getLine(cursor.leftLine).lineSeparator.let {
                    if (it == LineSeparator.NONE) {
                        "EOF"
                    } else {
                        it.name
                    }
                } + ">)"
            } else {
                val char = binding.editor.text.charAt(
                    cursor.leftLine,
                    cursor.leftColumn
                )
                if (char.isLowSurrogate() && cursor.leftColumn > 0) {
                    "(" + String(
                        charArrayOf(
                            binding.editor.text.charAt(
                                cursor.leftLine,
                                cursor.leftColumn - 1
                            ), char
                        )
                    ) + ")"
                } else if (char.isHighSurrogate() && cursor.leftColumn + 1 < binding.editor.text.getColumnCount(
                        cursor.leftLine
                    )
                ) {
                    "(" + String(
                        charArrayOf(
                            char, binding.editor.text.charAt(
                                cursor.leftLine,
                                cursor.leftColumn + 1
                            )
                        )
                    ) + ")"
                } else {
                    "(" + escapeIfNecessary(
                        binding.editor.text.charAt(
                            cursor.leftLine,
                            cursor.leftColumn
                        )
                    ) + ")"
                }
            }
        }
        binding.positionDisplay.text = text
    }

    private fun escapeIfNecessary(c: Char): String {
        return when (c) {
            '\n' -> "\\n"
            '\t' -> "\\t"
            '\r' -> "\\r"
            ' ' -> "<ws>"
            else -> c.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        undo = menu.findItem(R.id.text_undo)
        redo = menu.findItem(R.id.text_redo)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.editor.release()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val editor = binding.editor
        when (id) {
            R.id.text_undo -> editor.undo()
            R.id.text_redo -> editor.redo()
            R.id.Save -> saveFile()
            R.id.goto_end -> editor.setSelection(
                editor.text.lineCount - 1,
                editor.text.getColumnCount(editor.text.lineCount - 1)
            )
            R.id.move_up -> editor.moveSelectionUp()
            R.id.move_down -> editor.moveSelectionDown()
            R.id.home -> editor.moveSelectionHome()
            R.id.end -> editor.moveSelectionEnd()
            R.id.move_left -> editor.moveSelectionLeft()
            R.id.move_right -> editor.moveSelectionRight()
            R.id.magnifier -> {
                item.isChecked = !item.isChecked
                editor.getComponent(Magnifier::class.java).isEnabled = item.isChecked
            }
            R.id.useIcu -> {
                item.isChecked = !item.isChecked
                editor.props.useICULibToSelectWords = item.isChecked
            }
            R.id.code_format -> editor.formatCodeAsync()
            R.id.search_panel_st -> {
                if (binding.searchPanel.visibility == View.GONE) {
                    binding.apply {
                        searchEditor.setText("")
                        editor.searcher.stopSearch()
                        searchPanel.visibility = View.VISIBLE
                        item.isChecked = true
                    }
                } else {
                    binding.searchPanel.visibility = View.GONE
                    editor.searcher.stopSearch()
                    item.isChecked = false
                }
            }
            R.id.switch_colors -> {
                val themes = arrayOf(
                    "QuietLight for TM",
                    "Darcula for TM",
                    "Abyss for TM",
                )
                AlertDialog.Builder(this)
                    .setTitle(R.string.color_scheme)
                    .setSingleChoiceItems(themes,0) { dialog: DialogInterface, which: Int ->
                        when (which) {
                            0 -> try {

                                val themeSource = IThemeSource.fromInputStream(
                                    assets.open("textmate/QuietLight.tmTheme"),
                                    "QuietLight.tmTheme",
                                    null
                                )
                                val colorScheme = TextMateColorScheme.create(themeSource)
                                editor.colorScheme = colorScheme
                                val language = editor.editorLanguage
                                if (language is TextMateLanguage) {
                                    language.updateTheme(themeSource)
                                }
                                CoroutineScope(Dispatchers.IO).launch {
                                    dataStore.updateTheme(EditorTheme.QuietLight)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            1 -> try {
                                val themeSource = IThemeSource.fromInputStream(
                                    assets.open("textmate/darcula.json"),
                                    "darcula.json",
                                    null
                                )

                                val colorScheme = TextMateColorScheme.create(themeSource)
                                editor.colorScheme = colorScheme
                                val language = editor.editorLanguage
                                if (language is TextMateLanguage) {
                                    language.updateTheme(themeSource)
                                }
                                CoroutineScope(Dispatchers.IO).launch {
                                    dataStore.updateTheme(EditorTheme.DarculaTheme)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            2 -> try {
                                val themeSource = IThemeSource.fromInputStream(
                                    assets.open("textmate/abyss-color-theme.json"),
                                    "abyss-color-theme.json",
                                    null
                                )
                                val colorScheme = TextMateColorScheme.create(themeSource)
                                editor.colorScheme = colorScheme
                                val language = editor.editorLanguage
                                if (language is TextMateLanguage) {
                                    language.updateTheme(themeSource)
                                }
                                CoroutineScope(Dispatchers.IO).launch {
                                    dataStore.updateTheme(EditorTheme.AbyssColor)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            R.id.text_wordwrap -> {
                item.isChecked = !item.isChecked
                editor.isWordwrap = item.isChecked
            }
            R.id.editor_line_number -> {
                editor.isLineNumberEnabled = !editor.isLineNumberEnabled
                item.isChecked = editor.isLineNumberEnabled
            }
            R.id.pin_line_number -> {
                editor.setPinLineNumber(!editor.isLineNumberPinned)
                item.isChecked = editor.isLineNumberPinned
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveFile() {
         val fos=FileOutputStream(file)
        CoroutineScope(Dispatchers.IO).run {
            fos.write(binding.editor.text.toString().toByteArray())
        }
        Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show()
    }


    override fun onPause() {
        super.onPause()
        mUserViewModel.text=binding.editor.text.toString()
    }
    class EditorActivityViewModelFactory(private val myFile:File?):ViewModelProvider.Factory  {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(EditorActivityViewModel::class.java)) return myFile?.let {
                EditorActivityViewModel(
                    it
                )
            } as T
            else(throw IllegalArgumentException("Wrong ViewModel"))
        }
    }
    private  fun setTheme(mTheme:String)
    {
        val editor= binding.editor
        when (mTheme) {
            EditorTheme.QuietLight -> try {
                val themeSource = IThemeSource.fromInputStream(
                    assets.open("textmate/QuietLight.tmTheme"),
                    "QuietLight.tmTheme",
                    null
                )
                val colorScheme = TextMateColorScheme.create(themeSource)
                editor.colorScheme = colorScheme
                val language = TextMateLanguage.create(
                    IGrammarSource.fromInputStream(
                        assets.open("textmate/python/syntaxes/python.tmLanguage.json"),
                        "Python.tmLanguage.json",
                        null
                    ),
                    InputStreamReader(assets.open("textmate/python/language-configuration.json")),
                    (editor.colorScheme as TextMateColorScheme).themeSource
                )
                editor.setEditorLanguage(language)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            EditorTheme.DarculaTheme -> try {
                val themeSource = IThemeSource.fromInputStream(
                    assets.open("textmate/darcula.json"),
                    "darcula.json",
                    null
                )

                val colorScheme = TextMateColorScheme.create(themeSource)
                editor.colorScheme = colorScheme
                val language = TextMateLanguage.create(
                    IGrammarSource.fromInputStream(
                        assets.open("textmate/python/syntaxes/python.tmLanguage.json"),
                        "Python.tmLanguage.json",
                        null
                    ),
                    InputStreamReader(assets.open("textmate/python/language-configuration.json")),
                    (editor.colorScheme as TextMateColorScheme).themeSource
                )
                editor.setEditorLanguage(language)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            EditorTheme.AbyssColor -> try {
                val themeSource = IThemeSource.fromInputStream(
                    assets.open("textmate/abyss-color-theme.json"),
                    "abyss-color-theme.json",
                    null
                )
                val colorScheme = TextMateColorScheme.create(themeSource)
                editor.colorScheme = colorScheme
                val language = TextMateLanguage.create(
                    IGrammarSource.fromInputStream(
                        assets.open("textmate/python/syntaxes/python.tmLanguage.json"),
                        "Python.tmLanguage.json",
                        null
                    ),
                    InputStreamReader(assets.open("textmate/python/language-configuration.json")),
                    (editor.colorScheme as TextMateColorScheme).themeSource
                )
                editor.setEditorLanguage(language)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}