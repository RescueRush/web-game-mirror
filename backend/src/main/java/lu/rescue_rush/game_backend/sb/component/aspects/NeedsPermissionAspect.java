package lu.rescue_rush.game_backend.sb.component.aspects;

import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lu.rescue_rush.game_backend.db.data.user.UserData;
import lu.rescue_rush.game_backend.sb.component.annotation.NeedsPermission;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.monitoring.InteractionMonitor;

@Aspect
@Component
public class NeedsPermissionAspect {

	private static final Logger LOGGER = Logger.getLogger(NeedsPermissionAspect.class.getName());

	@Around("@annotation(needsPermission)")
	public Object checkPermission(ProceedingJoinPoint joinPoint, NeedsPermission needsPermission) throws Throwable {
		final UserData ud = SpringUtils.getContextUser();

		if (ud == null) {
			InteractionMonitor.pushDeniedDesc("Missing authentication.");
			SpringUtils.unauthorized(true, "Missing token.");
		}

		if (ud.isSuperuser()) {
			return joinPoint.proceed();
		}

		String[] permissions = needsPermission.value();
		final String uri = SpringUtils.getContextPath();

		if (permissions.length == 0) {
			LOGGER.warning("Checking for empty permissions for endpoint: " + uri);
			return joinPoint.proceed();
		}

		switch (needsPermission.condition()) {
		case AND:
			SpringUtils.permissionAnd(ud, permissions);
			break;
		case OR:
			SpringUtils.permissionOr(ud, permissions);
			break;
		default:
			LOGGER.severe("Unknown type: " + needsPermission.condition() + " (" + uri + ")");
			SpringUtils.internalServerError(true, "Unknown type: " + needsPermission.condition() + " (" + uri + ")");
		}

		return joinPoint.proceed();
	}

}
