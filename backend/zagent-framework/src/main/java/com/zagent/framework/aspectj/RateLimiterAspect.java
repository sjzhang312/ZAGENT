package com.zagent.framework.aspectj;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import com.zagent.common.annotation.RateLimiter;
import com.zagent.common.enums.LimitType;
import com.zagent.common.exception.ServiceException;
import com.zagent.common.utils.StringUtils;
import com.zagent.common.utils.ip.IpUtils;

/**
 * 限流处理（zagent.cache.type=redis 走 Redis Lua；memory 走本地滑动窗口）
 *
 * @author zagent
 */
@Aspect
@Component
public class RateLimiterAspect
{
    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    private final RedisTemplate<Object, Object> redisTemplate;

    private final RedisScript<Long> limitScript;

    /** 缓存模式：memory 本地限流；redis Lua 限流 */
    @Value("${zagent.cache.type:memory}")
    private String cacheType;

    /** 本地滑动窗口：key -> 时间戳队列（毫秒） */
    private final Map<String, Deque<Long>> localWindows = new HashMap<>();

    @Autowired
    public RateLimiterAspect(RedisTemplate<Object, Object> redisTemplate, RedisScript<Long> limitScript)
    {
        this.redisTemplate = redisTemplate;
        this.limitScript = limitScript;
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable
    {
        int time = rateLimiter.time();
        int count = rateLimiter.count();

        String combineKey = getCombineKey(rateLimiter, point);
        try
        {
            if ("memory".equals(cacheType))
            {
                checkLocal(combineKey, count, time);
            }
            else
            {
                List<Object> keys = Collections.singletonList(combineKey);
                Long number = redisTemplate.execute(limitScript, keys, count, time);
                if (StringUtils.isNull(number) || number.intValue() > count)
                {
                    throw new ServiceException("访问过于频繁，请稍候再试");
                }
                log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), combineKey);
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException("服务器限流异常，请稍候再试");
        }
    }

    private synchronized void checkLocal(String key, int count, int timeSeconds)
    {
        long now = System.currentTimeMillis();
        long windowMillis = timeSeconds * 1000L;
        Deque<Long> window = localWindows.computeIfAbsent(key, k -> new ArrayDeque<>());
        while (!window.isEmpty() && now - window.peekFirst() >= windowMillis)
        {
            window.pollFirst();
        }
        if (window.size() >= count)
        {
            throw new ServiceException("访问过于频繁，请稍候再试");
        }
        window.addLast(now);
    }

    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point)
    {
        StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.IP)
        {
            stringBuffer.append(IpUtils.getIpAddr()).append("-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }
}
