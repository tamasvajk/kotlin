// FILE: 1.kt
typealias Service = (String) -> Unit

inline fun Service.decorate(crossinline decorate: (Service) -> Service): Service =
    object : Service by decorate(this) {}

inline fun Service.transformMessage(crossinline transform: (String) -> String) =
    decorate { service -> { message -> service(transform(message)) } }

// FILE: 2.kt
fun Service.append(suffix: String): Service =
    transformMessage { it + suffix }

fun box(): String {
    var result = "fail"
    { it: String -> result = it }.append("K")("O")
    return result
}
