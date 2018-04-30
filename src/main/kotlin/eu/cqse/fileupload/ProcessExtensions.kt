package eu.cqse.fileupload

import mu.KotlinLogging
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.time.Duration
import java.util.concurrent.TimeUnit

/** Result of running a process. */
sealed class ProcessResult

/** The process did not finish in time. */
class ProcessTimedOut(
    /** The stdout of the process until the timeout occurred. */
    val stdout: String,
    /** The stderr of the process until the timeout occurred. */
    val stderr: String,
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

/**
 * Reads the given [inputStream] in the background and allows early termination of the read loop.
 * Starts reading right away after this object is constructed. You must call [readAndTerminateThread] or [close] to
 * clean up the background [thread].
 */
private class ProcessStreamReader(inputStream: InputStream) : Closeable {

    private val buffer = StringBuilder()

    @Volatile
    private var isTerminated = false

    private val thread = Thread {
        inputStream.bufferedReader().useLines {
            it.forEach {
                if (isTerminated) {
                    return@Thread
                }

                synchronized(buffer) {
                    buffer.append(it).append("\n")
                }
            }
        }
    }.also { it.start() }

    /**
     * Terminates the [thread] and reads all data written so far to the input stream.
     * Must only be called once!
     */
    @Synchronized
    fun readAndTerminateThread(): String {
        if (isTerminated) {
            throw IllegalStateException("Buffer was already read")
        }

        return synchronized(buffer) {
            isTerminated = true
            buffer.toString()
        }
    }

    /** Waits for the read [thread] to stop on its own. */
    fun join() {
        thread.join()
    }

    /** Cleans up the background [thread] lazily (on next read from the stream). */
    override fun close() {
        isTerminated = true
    }

}

/** Waits for the given duration for the process to finish. Also handles reading stderr and stdout. */
private fun Process.waitFor(maxWaitTime: Duration): ProcessResult {
    // we must read stderr and stdout in parallel, otherwise the process may hang
    val stdout = ProcessStreamReader(inputStream)
    val stderr = ProcessStreamReader(errorStream)

    stdout.use {
        stderr.use {
            return waitFor(maxWaitTime, stdout, stderr)
        }
    }
}

/**
 * Waits for the given duration for the process to finish. Also handles reading stderr and stdout but does not
 * close them.
 */
private fun Process.waitFor(
    maxWaitTime: Duration,
    stdout: ProcessStreamReader,
    stderr: ProcessStreamReader
): ProcessResult {
    val finishedInTime = waitFor(maxWaitTime.toMillis(), TimeUnit.MILLISECONDS)
    if (!finishedInTime) {
        return ProcessTimedOut(
            stdout = stdout.readAndTerminateThread(),
            stderr = stderr.readAndTerminateThread()
        ) {
            destroyForcibly()
        }
    }

    stdout.join()
    stderr.join()
    return ProcessFinished(
        stdout = stdout.readAndTerminateThread(),
        stderr = stderr.readAndTerminateThread(),
        exitCode = exitValue()
    )
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
