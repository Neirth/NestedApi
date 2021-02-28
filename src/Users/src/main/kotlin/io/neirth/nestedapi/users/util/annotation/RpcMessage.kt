package io.neirth.nestedapi.users.util.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RpcMessage(val topic: String = "", val queue: String = "")
