package eu.cqse.fileupload

import mu.KLogging
import org.conqat.lib.commons.filesystem.FileSystemUtils
import org.http4k.core.*
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

/** [HttpHandler] that accepts uploads */
internal class UploadHandler(
    private val outputDirectory: Path,
    private val postUploadHook: (Path) -> Unit
) : HttpHandler {

    companion object : KLogging()

    private val counter = AtomicInteger(0)

    /** Handles the given upload request. */
    override fun invoke(request: Request): Response {
        val file = try {
            MultipartFormBody.from(request).file("file")
        } catch (e: RuntimeException) {
            logger.error(e) { "Invalid multi-part request $request" }
            return Response(Status.BAD_REQUEST)
                .body("Upload failed. Not a valid multi-part form data request: ${e.message}")
        }

        if (file == null) {
            logger.error { "No file provided in request $request in form parameter `file`" }
            return Response(Status.BAD_REQUEST).body("Upload failed. No file provided in form parameter `file`")
        }

        val content = try {
            file.content.use { it.readBytes() }
        } catch (e: IOException) {
            logger.error(e) { "Failed to read file from request: $request" }
            return Response(Status.INTERNAL_SERVER_ERROR).body("Upload failed. Failed to read file from request")
        }

        val path = calculateOutputPath(file)
        try {
            FileSystemUtils.writeFileBinary(path.toFile(), content)
        } catch (e: IOException) {
            logger.error(e) { "Failed to write file to disk: $path" }
            return Response(Status.INTERNAL_SERVER_ERROR).body("Upload failed. Failed to write file to disk")
        }

        postUploadHook(path)
        return Response(Status.OK).body("Upload successful")
    }

    private fun calculateOutputPath(file: FormFile): Path {
        val timestamp = System.currentTimeMillis()
        val uniqueId = counter.incrementAndGet()
        val fileName = normalize("${timestamp}_${uniqueId}_${file.filename}")
        return outputDirectory.resolve(fileName)
    }

    private fun normalize(fileName: String) = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")

}