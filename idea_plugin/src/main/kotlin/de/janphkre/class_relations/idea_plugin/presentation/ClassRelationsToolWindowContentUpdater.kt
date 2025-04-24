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