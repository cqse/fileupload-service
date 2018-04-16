package eu.cqse.fileupload

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import mu.KotlinLogging
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.util.*

private val programVersion = ResourceBundle.getBundle("eu.cqse.fileupload.app").getString("version")
private val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {
    mainBody("eu.cqse.fileupload.jar") {

        ArgParser(args).parseInto(::CommandLineArguments).run {
            logger.info {
                "Starting file upload receiver $programVersion on port $port. Writing output to $outputDirectory"
            }

            val commandRunner = CommandRunner(commandToRun)
            UploadHandler(outputDirectory, commandRunner::runAsync).asServer(Jetty(port)).start()
        }
    }
}