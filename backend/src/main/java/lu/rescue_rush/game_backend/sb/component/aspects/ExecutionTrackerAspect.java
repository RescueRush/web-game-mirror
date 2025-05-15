package lu.rescue_rush.game_backend.sb.component.aspects;

import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import lu.rescue_rush.game_backend.sb.component.annotation.ExecutionTrack;
import lu.rescue_rush.game_backend.utils.SpringUtils;
import lu.rescue_rush.game_backend.utils.monitoring.ExecutionMonitor;

@Aspect
@Component
public class ExecutionTrackerAspect {

	private static final Logger LOGGER = Logger.getLogger(ExecutionTrackerAspect.class.getName());

	@Around("@annotation(perfTrack)")
	public Object perfTrack(ProceedingJoinPoint joinPoint, ExecutionTrack perfTrack) throws Throwable {
		String methodName = perfTrack.value();
		if (methodName.isEmpty()) {
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			Method method = signature.getMethod();
			Class<?> clazz = joinPoint.getTarget().getClass();
			methodName = SpringUtils.getFullPath(method, clazz);
		}

		return ExecutionMonitor.time(methodName, () -> {
			try {
				return joinPoint.proceed();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
	}

}