package lu.rescue_rush.game_backend.configs;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Profile("debug")
public class GlobalExceptionHandler {

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> handleJsonParseException(HttpMessageNotReadableException ex) {
		ex.printStackTrace(); // Full stack trace in logs

		return ResponseEntity.badRequest().body("Invalid JSON: " + ex.getMostSpecificCause().getMessage());
	}
}
