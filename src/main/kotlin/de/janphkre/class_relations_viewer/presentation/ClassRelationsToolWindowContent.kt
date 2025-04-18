package de.janphkre.class_relations_viewer.presentation

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
        val file = service.getInitialFile()
        return if(file == null) {
            getEmptyContent()
        } else {
            getOpenFileContent(file)
        }
    }

    fun getEmptyContent() = JBPanel<JBPanel<*>>().apply {
        showEmpty()
    }

    fun getOpenFileContent(file: VirtualFile) = JBPanel<JBPanel<*>>().apply {
        val content = service.getFileContent(file)
        if (content == null) {
            showEmpty()
        } else {
            showForFile(content)
        }
    }

    private fun JBPanel<JBPanel<*>>.showEmpty() {
        val label = JBLabel("Open a file in the editor to get started!")
        add(label)
    }

    private fun JBPanel<JBPanel<*>>.showForFile(klassDefinition: KlassDefinition) {
        add(JBLabel(klassDefinition.name))
        add(JButton("Refresh").apply {
            addActionListener {
                updateContentAction()
            }
        })
        val contentPanel = JBPanel<JBPanel<*>>()
        contentPanel.add(JBLabel("PACKAGE:"))
        contentPanel.add(JBLabel(klassDefinition.filePackage.joinToString(".")))
        contentPanel.add(JBLabel("IMPORTS:"))
        klassDefinition.fileImports.forEach { contentPanel.add(JBLabel(it.joinToString("."))) }
        contentPanel.add(JBLabel("PARAMETERS:"))
        klassDefinition.parameters.forEach { contentPanel.add(JBLabel(it)) }
        contentPanel.add(JBLabel("INHERITANCES:"))
        klassDefinition.inheritances.forEach { contentPanel.add(JBLabel(it)) }
        add(contentPanel)
        val contentPanel2 = JBPanel<JBPanel<*>>()
        val generatedPuml = service.pumlGenerator.generate(listOf(klassDefinition.toKlassWithRelations()))
        println(generatedPuml)
        contentPanel2.add(JBLabel(generatedPuml))
        add(contentPanel2)
    }

    fun setUpdateContentAction(action: () -> Unit) {
        updateContentAction = action
    }
}