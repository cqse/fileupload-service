package eu.cqse.fileupload

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.conqat.lib.commons.filesystem.FileSystemUtils
import java.nio.file.Path
import java.nio.file.Paths

internal class CommandLineArguments(private val parser: ArgParser) {

    val outputDirectory: Path by parser.storing("-o", "--out",
        help = "Path under which the uploaded files will be stored.") { Paths.get(this) }
        .addValidator {
            FileSystemUtils.ensureDirectoryExists(value.toFile())
        }

    val port: Int by parser.storing("-p", "--port",
        help = "Port under which to listen for uploads. Defaults to 9346.") { toInt() }
        .default(9346)

}