import com.intellij.openapi.GitSilentFileAdderProvider
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import liveplugin.registerEditorAction
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

registerEditorAction(
    id = "Create Android Preview",
    keyStroke = "alt A",
    actionGroupId = "EditorPopupMenu",
) { editor, _, _ ->
    val originalPath = editor.virtualFile.toNioPath()

    var srcPath = originalPath
    while (!srcPath.endsWith(Path("src"))) {
        srcPath = srcPath.parent
    }

    var packagePath = originalPath.parent
    while (!packagePath.startsWith("kotlin")) {
        packagePath = packagePath.subpath(1, packagePath.nameCount)
    }
    packagePath = packagePath.subpath(1, packagePath.nameCount)

    val previewPath = Path(
        srcPath.pathString,
        "androidMain",
        "kotlin",
        packagePath.pathString,
    )

    previewPath.toFile().mkdirs()

    val previewFilename = "${originalPath.nameWithoutExtension}Preview.kt"
    val previewFile = File(previewPath.pathString, previewFilename)

    if (!previewFile.exists()) {
        val previewContent = """
        package ${packagePath.pathString.replace('\\', '.')}
        
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.tooling.preview.Preview
        import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
        
        @Preview
        @Composable
        private fun Preview() {
            NeonTheme {
                
            }
        }
    """.trimIndent()

        previewFile.writeText(previewContent)
    }

    VirtualFileManager.getInstance().syncRefresh()

    val virtualPreviewFile = LocalFileSystem.getInstance().findFileByIoFile(previewFile)

    val project = project
    if (project != null && virtualPreviewFile != null) {
        FileEditorManager.getInstance(project).openEditor(
            OpenFileDescriptor(project, virtualPreviewFile, 0),
            true,
        )

        // TODO: This is currently using an internal API. Find a public API that accomplishes
        //  the same thing.
        @Suppress("UnstableApiUsage")
        GitSilentFileAdderProvider.create(project).apply {
            markFileForAdding(virtualPreviewFile)
        }.finish()
    }
}