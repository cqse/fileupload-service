package eu.cqse.fileupload

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import mu.KLogging
import org.conqat.lib.commons.filesystem.FileSystemUtils
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

private val programVersion = ResourceBundle.getBundle("eu.cqse.fileupload.app").getString("version")

/** Entry point. */
class Main : CliktCommand() {

    companion object : KLogging()

    val outputDirectory: Path by option(
        "-o", "--out",
        help = "Path under which the uploaded files will be stored."
    )
        .convert { Paths.get(it) }
        .required()
        .validate {
            FileSystemUtils.ensureDirectoryExists(it.toFile())
        }

    val port: Int by option(
        "-p", "--port",
        help = "Port under which to listen for uploads. Defaults to 9346."
    )
        .int()
        .default(9346)

    val commandToRun: String? by option(
        "-c", "--command",
        help = "Optional command to run after each upload. \$F will be replaced with the path to the uploaded file."
    )

    override fun run() {
        logger.info {
            "Starting file upload receiver $programVersion on port $port. Writing output to $outputDirectory"
        }

        val commandRunner = CommandRunner(commandToRun)
        UploadHandler(outputDirectory, commandRunner::runAsync).asServer(Jetty(port)).start()
    }

}

/** Entry point. */
fun main(args: Array<String>) {
    Main().main(args)
}