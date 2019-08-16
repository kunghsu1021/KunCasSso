package com.kunghsu.cache;

public interface ISessionCacheService {


    void setSessionCache(String key, String value);

    <T> T getSessionCache(String key);

    void deleteSession(String key);

}
