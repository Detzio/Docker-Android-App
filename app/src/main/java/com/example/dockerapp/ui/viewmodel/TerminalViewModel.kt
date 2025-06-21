package com.example.dockerapp.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dockerapp.data.api.RetrofitClient
import com.example.dockerapp.data.model.ExecCreateRequest
import com.example.dockerapp.data.model.ExecStartRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import androidx.compose.runtime.State
import com.example.dockerapp.data.model.CompleteCommand

const val TAG = "TerminalViewModel"

class TerminalViewModel: ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _commandResults = mutableStateOf(listOf<CompleteCommand>())
    val commandResults: State<List<CompleteCommand>> = _commandResults

    fun execCommand(command: String, containerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val execCreateBody = ExecCreateRequest(Cmd = listOf("/bin/sh", "-c", command))
                val createResponse = RetrofitClient.apiService.createExecInstance(containerId, execCreateBody)

                if (createResponse.isSuccessful) {
                    val execId = createResponse.body()?.Id

                    if (execId != null) {
                        val startBody = ExecStartRequest()
                        val startResponse = RetrofitClient.apiService.startExec(execId, startBody)

                        if (startResponse.isSuccessful) {
                            val inputStream = startResponse.body()?.byteStream()
                            val output = parseDockerMultiplexedStream(inputStream!!)
                            Log.d(TAG, "Command Output: $output")
                            _commandResults.value += CompleteCommand("$ $command", output)
                        } else {
                            Log.e(TAG, "Failed to start exec: ${startResponse.code()}")
                            _error.value = "Une erreur est survenue"
                        }
                    } else {
                        Log.e(TAG, "execId is null")
                        _error.value = "Une erreur est survenue"
                    }
                } else {
                    Log.e(TAG, "Failed to create exec: ${createResponse.code()}")
                    _error.value = "Une erreur est survenue"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during execCommand", e)
                _error.value = "Une erreur est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseDockerMultiplexedStream(inputStream: InputStream): String {
        val output = StringBuilder()
        val buffer = ByteArray(8) // for the header

        while (true) {
            // Read header
            val headerBytesRead = inputStream.read(buffer)
            if (headerBytesRead == -1) break // End of stream

            if (headerBytesRead < 8) {
                throw IOException("Invalid Docker stream header")
            }

            buffer[0].toInt()
            val payloadSize = ByteBuffer.wrap(buffer, 4, 4).int

            val payloadBuffer = ByteArray(payloadSize)
            var totalRead = 0
            while (totalRead < payloadSize) {
                val bytesRead = inputStream.read(payloadBuffer, totalRead, payloadSize - totalRead)
                if (bytesRead == -1) break
                totalRead += bytesRead
            }

            val payloadText = payloadBuffer.decodeToString()
            output.append(payloadText) // Or separate stdout/stderr by streamType
        }

        return output.toString()
    }
}