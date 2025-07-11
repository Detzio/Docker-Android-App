package com.example.dockerapp.data.model

data class ExecCreateRequest(
    val AttachStdout: Boolean = true,
    val AttachStderr: Boolean = true,
    val Tty: Boolean = false,
    val Cmd: List<String>
)

data class ExecCreateResponse(val Id: String)

data class ExecStartRequest(
    val Detach: Boolean = false,
    val Tty: Boolean = false
)

data class CompleteCommand(
    val command: String,
    val result: String
)