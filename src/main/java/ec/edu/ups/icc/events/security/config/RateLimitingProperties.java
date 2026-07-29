package ec.edu.ups.icc.events.security.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class RateLimitingProperties {

    private final long loginLimit;
    private final long loginWindowSeconds;
    private final int loginFailedToBlock;
    private final long loginBlockDurationMinutes;

    private final long registerLimit;
    private final long registerWindowSeconds;

    private final long generalAnonymousLimit;
    private final long generalAuthLimit;
    private final long generalWindowSeconds;

    public RateLimitingProperties(Environment env) {
        this.loginLimit = Long.parseLong(env.getProperty("rate-limiting.login.limit", "5"));
        this.loginWindowSeconds = Long.parseLong(env.getProperty("rate-limiting.login.window-seconds", "60"));
        this.loginFailedToBlock = Integer.parseInt(env.getProperty("rate-limiting.login.failed-attempts-to-block", "5"));
        this.loginBlockDurationMinutes = Long.parseLong(env.getProperty("rate-limiting.login.block-duration-minutes", "15"));

        this.registerLimit = Long.parseLong(env.getProperty("rate-limiting.register.limit", "3"));
        this.registerWindowSeconds = Long.parseLong(env.getProperty("rate-limiting.register.window-seconds", "3600"));

        this.generalAnonymousLimit = Long.parseLong(env.getProperty("rate-limiting.general.anonymous-limit", "60"));
        this.generalAuthLimit = Long.parseLong(env.getProperty("rate-limiting.general.auth-limit", "120"));
        this.generalWindowSeconds = Long.parseLong(env.getProperty("rate-limiting.general.window-seconds", "60"));
    }

    public long getLoginLimit() { return loginLimit; }
    public long getLoginWindowSeconds() { return loginWindowSeconds; }
    public int getLoginFailedToBlock() { return loginFailedToBlock; }
    public long getLoginBlockDurationMinutes() { return loginBlockDurationMinutes; }

    public long getRegisterLimit() { return registerLimit; }
    public long getRegisterWindowSeconds() { return registerWindowSeconds; }

    public long getGeneralAnonymousLimit() { return generalAnonymousLimit; }
    public long getGeneralAuthLimit() { return generalAuthLimit; }
    public long getGeneralWindowSeconds() { return generalWindowSeconds; }
}
