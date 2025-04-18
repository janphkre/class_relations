package de.janphkre.class_relations_viewer.domain

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.isFile
import de.janphkre.class_relations_viewer.model.KlassDefinition

@Service(Service.Level.PROJECT)
class ClassRelationsProjectService(
    project: Project
) {

    private val fileEditorManager = FileEditorManager.getInstance(project)
    private val fileDocumentManager = FileDocumentManager.getInstance()
    private val parser: KotlinHeaderParser = KotlinHeaderParser()
    val pumlGenerator = ClassRelationsPumlGenerator(
        ClassRelationsPumlGenerator.Settings("asdf","#ff0000")
    )

    fun getInitialDocument(): Document? {
        val currentDoc = fileEditorManager.selectedTextEditor?.document ?: return null
        return currentDoc
    }

    fun getAdjacentFileContents(document: Document): List<KlassDefinition> {
        val currentFile = fileDocumentManager.getFile(document) ?: return emptyList()
        return currentFile.parent.children.mapNotNull {
            if (it == currentFile) {
                return@mapNotNull null
            }
            getFileContent(it)
        }
    }

    //TODO: title
    fun getOpenEditorContent(document: Document): KlassDefinition? {
        return parser.parse(document.text, document.toString())
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