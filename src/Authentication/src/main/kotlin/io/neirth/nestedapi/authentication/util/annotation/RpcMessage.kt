package io.neirth.nestedapi.authentication.util.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RpcMessage(val topic: String = "", val queue: String = "")