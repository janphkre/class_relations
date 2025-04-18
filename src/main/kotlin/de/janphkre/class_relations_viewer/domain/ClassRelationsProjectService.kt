package de.janphkre.class_relations_viewer.domain

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
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

    fun getInitialFile(): VirtualFile? {
        val currentDoc = fileEditorManager.selectedTextEditor?.document ?: return null
        val currentFile = fileDocumentManager.getFile(currentDoc) ?: return null
        return currentFile
    }

    fun getFileContent(file: VirtualFile): KlassDefinition? {
        if (!file.isFile || !file.isInLocalFileSystem) {
            return null
        }
        if (file.extension != "kt") {
            return null
        }
        val input = parser.parse(file)
        return input
    }


    companion object {
        @JvmStatic
        fun getInstance(project: Project): ClassRelationsProjectService = project.service()
    }
}