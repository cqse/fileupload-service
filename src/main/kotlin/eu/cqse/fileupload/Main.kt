package eu.cqse.fileupload

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main(args: Array<String>) = mainBody("eu.cqse.fileupload.jar") {
    ArgParser(args).parseInto(::CommandLineArguments).run {
        UploadHandler(outputDirectory).asServer(Jetty(port)).start()
    }
}