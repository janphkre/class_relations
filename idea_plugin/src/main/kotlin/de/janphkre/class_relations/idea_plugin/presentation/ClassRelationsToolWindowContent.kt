/**
 *    Copyright 2025 Jan Phillip Kretzschmar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.janphkre.class_relations.idea_plugin.presentation

import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import de.janphkre.class_relations.idea_plugin.domain.ClassRelationsProjectService
import de.janphkre.class_relations.library.model.KlassWithRelations
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

    private fun JBPanel<JBPanel<*>>.showForFiles(openFile: KlassWithRelations, otherFiles: List<KlassWithRelations>) {
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
        val generatedPuml = service.pumlGenerator.generate(otherFiles.plus(openFile))
        println(generatedPuml)
        contentPanel2.add(JBLabel(generatedPuml))
        add(contentPanel2)
    }

    fun setUpdateContentAction(action: () -> Unit) {
        updateContentAction = action
    }
}