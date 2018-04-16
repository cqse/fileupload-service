package eu.cqse.fileupload

import mu.KotlinLogging
import java.io.File
import java.io.IOException
import java.time.Duration
import java.util.concurrent.TimeUnit

/** Result of running a process. */
sealed class ProcessResult

/** The process did not finish in time. */
class ProcessTimedOut(
    private val destroyForcibly: () -> Unit
) : ProcessResult() {

    /** Kills the still running process. */
    fun killProcess() {
        destroyForcibly()
    }

}

/** The process finished in time. */
data class ProcessFinished(
    /** The stdout of the process. */
    val stdout: String,
    /** The stderr of the process. */
    val stderr: String,
    /** The exit code. */
    val exitCode: Int
) : ProcessResult() {

    /** Whether the exit code was 0. */
    val wasSuccessful get() = exitCode == 0

}

/** Waits for the given duration for the process to finish. Also handles reading stderr and stdout. */
private fun Process.waitFor(maxWaitTime: Duration): ProcessResult {
    var stdout: String? = null
    var stderr: String? = null

    // we must read stderr and stdout in parallel, otherwise the process may hang
    val stdoutThread = Thread {
        stdout = inputStream.bufferedReader().readText()
    }.also { it.start() }

    val stderrThread = Thread {
        stderr = errorStream.bufferedReader().readText()
    }.also { it.start() }

    val finishedInTime = waitFor(maxWaitTime.toMillis(), TimeUnit.MILLISECONDS)
    if (!finishedInTime) {
        return ProcessTimedOut { destroyForcibly() }
    }

    stdoutThread.join()
    stderrThread.join()
    return ProcessFinished(stdout = stdout!!, stderr = stderr!!, exitCode = exitValue())
}

/** Specifies all data needed to run a process. */
data class ProcessSpec(
    /** The command line to start. */
    val commandLine: List<String>,
    /** The working directory. */
    val workingDirectory: File,
    /** The data to send on stdin. */
    val stdin: String,
    /** The timeout for the process run. */
    val timeout: Duration
)

/** Runs a process and returns the result. */
typealias ProcessRunner = (spec: ProcessSpec) -> ProcessResult

private val logger = KotlinLogging.logger { }

/** Runs the given process and returns the result. */
fun runProcess(spec: ProcessSpec): ProcessResult {
    val builder = ProcessBuilder(spec.commandLine).directory(spec.workingDirectory)
    val process = builder.start()

    try {
        process.outputStream.bufferedWriter().use {
            it.write(spec.stdin)
        }
    } catch (e: IOException) {
        logger.error(e) { "Unable to write to output stream of process ${spec.commandLine}" }
    }
    return process.waitFor(spec.timeout)
}
