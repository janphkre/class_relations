package de.janphkre.class_relations.idea_plugin.domain

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import de.janphkre.class_relations.library.model.KlassDefinition
import de.janphkre.class_relations.library.domain.KotlinHeaderParser
import de.janphkre.class_relations.library.domain.ClassRelationsPumlGenerator

@Service(Service.Level.PROJECT)
class ClassRelationsProjectService(
    project: Project
) {

    private val fileEditorManager = FileEditorManager.getInstance(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()
    private val parser: KotlinHeaderParser = KotlinHeaderParser()
    val pumlGenerator = ClassRelationsPumlGenerator(
        ClassRelationsPumlGenerator.Settings(
            projectPackagePrefix = "asdf",
            selfColor = "#ff0000",
            spaceCount = 2
        )
    )

    fun getInitialContent(): TextEditor? {
        val currentEditor = fileEditorManager.selectedTextEditor ?: return null
        return currentEditor as? TextEditor
    }

    fun getAdjacentFileContents(baseEditor: TextEditor): List<KlassDefinition> {
        val currentFile = fileDocumentManager.getFile(baseEditor.editor.document) ?: return emptyList()
        val editors = fileEditorManager.allEditors.filterIsInstance<TextEditor>()
        return currentFile.parent.children.mapNotNull { file ->
            if (file == currentFile) {
                return@mapNotNull null
            }
            val editor = editors.firstOrNull { editor ->
                editor.file == file
            }
            if (editor != null) {
                getOpenEditorContent(editor)
            } else {
                getFileContent(file)
            }
        }
    }

    fun getOpenEditorContent(editor: TextEditor): KlassDefinition? {
        if (editor.file.extension != "kt") {
            return null
        }
        return parser.parse(editor.editor.document.text, editor.name)
    }

    fun getFileContent(file: VirtualFile): KlassDefinition? {
        if (file.extension != "kt") {
            return null
        }
        val fileContent = String(file.contentsToByteArray())
        val input = parser.parse(fileContent, file.presentableName)
        return input
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): ClassRelationsProjectService = project.service()
    }
}