package com.smilelive.utils;

public class RedisContent {
    public static final String TOKEN_KEY="sl:token:";
    public static final Long TOKEN_TTL=7L;
    public static final Long TRYLOCK_TTL=10L;
    /*验证码*/
    public static final String CAPTCHA_KEY="sl:captcha:";
    public static final Long CAPTCHA_TTL=5L;
    /*直播间*/
    public static final String LIVEROOM_KEY="sl:liveroom:";
    public static final Long LIVEROOM_TTL=30L;
    /*分布式锁*/
    public static final String LOCK_RUNE_KEY="sl:lock:rune:";
    public static final Long LOCK_RUNE_TTL=3L;

    public static final String GIFT_RANKING_KEY="sl:gift:ranking:";
}
