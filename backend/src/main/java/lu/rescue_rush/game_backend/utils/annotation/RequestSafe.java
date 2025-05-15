package lu.rescue_rush.game_backend.utils.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.ErrorResponseException;

/**
 * Marks a method, class, or constructor as "Request Safe," indicating that it
 * exclusively throws {@link ErrorResponseException}s to represent errors using
 * appropriate {@link HttpStatusCode}s.
 * 
 * <p>
 * Members annotated with {@code @RequestSafe} ensure that any errors
 * encountered during execution are communicated through standardized HTTP error
 * responses, encapsulated in {@link ErrorResponseException}. This annotation
 * highlights code designed to safely handle errors in the context of request
 * processing.
 * </p>
 */
@Documented
@Retention(CLASS)
@Target({ TYPE, METHOD, CONSTRUCTOR })
public @interface RequestSafe {

}
