package api.aspect;

import api.metric.LoginCountTotalMetric;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginMetricAspect {

    private final LoginCountTotalMetric loginCountTotalMetric;

    @AfterReturning("execution(public * api.service.TokenService.login(..))")
    public void afterLogin() {
        loginCountTotalMetric.incrementLoginCount();
    }
}