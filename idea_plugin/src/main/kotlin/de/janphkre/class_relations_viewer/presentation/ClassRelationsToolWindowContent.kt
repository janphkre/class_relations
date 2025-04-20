package de.janphkre.class_relations_viewer.presentation

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import de.janphkre.class_relations_viewer.domain.ClassRelationsProjectService
import de.janphkre.class_relations_viewer.model.KlassDefinition
import javax.swing.JButton

class ClassRelationsToolWindowContent(
    private val service: ClassRelationsProjectService,
) {

    private var updateContentAction: () -> Unit = {}

    fun getInitialContent(): JBPanel<JBPanel<*>> {
        val editor = service.getInitialContent()
        return if(editor == null) {
            getEmptyContent()
        } else {
            getDataContent(editor)
        }
    }

    fun getEmptyContent() = JBPanel<JBPanel<*>>().apply {
        showEmpty()
    }

    fun getDataContent(editor: TextEditor) = JBPanel<JBPanel<*>>().apply {
        val content = service.getOpenEditorContent(editor)
        if (content == null) {
            showEmpty()
        } else {
            showForFiles(content, service.getAdjacentFileContents(editor))
        }
    }

    private fun JBPanel<JBPanel<*>>.showEmpty() {
        val label = JBLabel("Open a file in the editor to get started!")
        add(label)
    }

    private fun JBPanel<JBPanel<*>>.showForFiles(openFile: KlassDefinition, otherFiles: List<KlassDefinition>) {
        add(JBLabel(openFile.name))
        add(JButton("Refresh").apply {
            addActionListener {
                updateContentAction()
            }
        })
        val contentPanel = JBPanel<JBPanel<*>>()
        contentPanel.add(JBLabel("PACKAGE:"))
        contentPanel.add(JBLabel(openFile.filePackage.joinToString(".")))
        contentPanel.add(JBLabel("IMPORTS:"))
        openFile.fileImports.forEach { contentPanel.add(JBLabel(it.joinToString("."))) }
        contentPanel.add(JBLabel("PARAMETERS:"))
        openFile.parameters.forEach { contentPanel.add(JBLabel(it)) }
        contentPanel.add(JBLabel("INHERITANCES:"))
        openFile.inheritances.forEach { contentPanel.add(JBLabel(it)) }
        add(contentPanel)
        val contentPanel2 = JBPanel<JBPanel<*>>()
        val generatedPuml = service.pumlGenerator.generate(otherFiles.plus(openFile).map { it.toKlassWithRelations() })
        println(generatedPuml)
        contentPanel2.add(JBLabel(generatedPuml))
        add(contentPanel2)
    }

    fun setUpdateContentAction(action: () -> Unit) {
        updateContentAction = action
    }
}