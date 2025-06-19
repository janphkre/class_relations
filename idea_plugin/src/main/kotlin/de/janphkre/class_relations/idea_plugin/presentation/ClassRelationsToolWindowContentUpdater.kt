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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory

class ClassRelationsToolWindowContentUpdater(
    private val toolWindowContent: ClassRelationsToolWindowContent,
    private val toolWindow: ToolWindow,
    private val contentFactory: ContentFactory = ContentFactory.getInstance()
): FileEditorManagerListener {

    fun initialize() {
        toolWindowContent.setUpdateContentAction(::updateContent)
        updateContent()
        subscribeToFileChanges()
    }

    private fun subscribeToFileChanges() {
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, this)
    }

    override fun selectionChanged(event: FileEditorManagerEvent) {
        val content = contentFactory.createContent(
            (event.newEditor as? TextEditor)?.let { toolWindowContent.getDataContent(it) } ?: toolWindowContent.getEmptyContent(),
            null,
            false
        )//TODO: DO NOT RECREATE WHOLE UI AT ALL TIMES -> create canvas for plantuml renderer!
        toolWindow.contentManager.removeAllContents(true)
        toolWindow.contentManager.addContent(content)
        println("Updated content!")
    }

    private fun updateContent() {
        val content = contentFactory.createContent(toolWindowContent.getInitialContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }
}