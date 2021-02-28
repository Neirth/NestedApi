package io.neirth.nestedapi.users.util.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class RpcMessage(val topic : String, val queue : String)
