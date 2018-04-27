package eu.cqse.fileupload

import mu.KLogging
import org.apache.tools.ant.types.Commandline
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.Executors

/** Runs the given command after each upload if it is not null. */
internal class CommandRunner(command: String?) {

    private val commandLine: Array<String> = when (command) {
        null -> emptyArray()
        else -> Commandline.translateCommandline(command)
    }

    init {
        logger.info {
            when (command) {
                null -> "No command given to run after the upload"
                else -> "Running `$command` after each upload"
            }
        }
    }

    companion object : KLogging()

    private val executor = Executors.newSingleThreadExecutor()

    /** Runs the [commandLine] and replaces {F} with the actual [path]. */
    fun runAsync(path: Path) {
        executor.submit { run(path) }
    }

    private fun run(path: Path) {
        if (commandLine.isEmpty()) {
            return
        }

        logger.info { "Running command for $path" }

        val actualCommandLine = commandLine.map { it.replace("{F}", path.toAbsolutePath().toString()) }

        val result = runProcess(
            ProcessSpec(
                commandLine = actualCommandLine.toList(),
                stdin = "",
                timeout = Duration.ofSeconds(5),
                workingDirectory = Paths.get("").toAbsolutePath().toFile()
            )
        )

        when (result) {
            is ProcessFinished -> {
                if (result.wasSuccessful) {
                    logger.debug { "Command finished successfully for $path" }
                } else {
                    logger.error {
                        """
                        |Command $actualCommandLine failed for $path:
                        |exit code = ${result.exitCode}
                        |stdout = ${result.stdout}
                        |stderr = ${result.stderr}
                        """.trimMargin()
                    }
                }
            }
            is ProcessTimedOut -> logger.error { "Command $actualCommandLine timed out for $path" }
        }
    }

}