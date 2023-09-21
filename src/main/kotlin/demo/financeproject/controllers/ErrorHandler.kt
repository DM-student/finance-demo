package demo.financeproject.controllers

import demo.financeproject.users.UserLoginError
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.lang.RuntimeException
import java.util.*

@ControllerAdvice
class ErrorHandler {
    @ExceptionHandler
    fun getResponseEntity(e: Throwable): ResponseEntity<Map<String, Any?>> {
        var status = HttpStatus.INTERNAL_SERVER_ERROR
        val response: MutableMap<String, Any?> = HashMap()
        response["error"] = e.message
        response["error_class"] = e.javaClass.simpleName
        response["error_trace"] = Arrays.stream(e.stackTrace).limit(4).toArray()
        if(e is BaseError) {
            status = e.httpStatus
        }
        return ResponseEntity(response, status)
    }
}

/**
 * Рекомендуемый способ наследования:
 * class SomeError(message: String, status: HttpStatus? = null) : BaseError(message, status)
 */
open class BaseError(message: String, httpStatus: HttpStatus? = null) : RuntimeException(message) {
    var httpStatus: HttpStatus  = HttpStatus.INTERNAL_SERVER_ERROR

    init {
        if(httpStatus != null) {
            this.httpStatus = httpStatus
        }
    }
}