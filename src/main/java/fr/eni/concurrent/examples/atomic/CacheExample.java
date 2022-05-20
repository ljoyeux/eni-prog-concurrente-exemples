package fr.eni.concurrent.examples.atomic;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

import java.util.concurrent.TimeUnit;

/**
 * Created by ljoyeux on 07/07/2017.
 */
public class CacheExample {
    public static void ehcache() {
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("preConfigured",
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class,
                                ResourcePoolsBuilder.heap(3))
                            .withExpiry(Expirations.timeToLiveExpiration(Duration.of(5, TimeUnit.SECONDS)))
                        .build())
                .build(true);

        Cache<Long, String> cache = cacheManager.getCache("preConfigured", Long.class, String.class);


        cache.put(1L, "one");
        boolean containsKey = cache.containsKey(1L);
        String value = cache.get(1L);
        System.out.println("Before expiration time\n\tContains key: " + containsKey  + ", value: " + value);

        // wait 5 seconds to reach ttl
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        containsKey = cache.containsKey(1L);
        value = cache.get(1L);
        System.out.println("After expiration time\n\tContains key: " + containsKey  + ", value: " + value);

        // limited cache size
        cache.put(1L, "one");
        System.out.println(cache.containsKey(1L));
        cache.put(2L, "two");
        System.out.println(cache.containsKey(1L));
        cache.put(3L, "three");
        System.out.println(cache.containsKey(1L));
        cache.put(4L, "four");
        System.out.println(cache.containsKey(1L));

        cacheManager.close();
    }

    public static void main(String[] args) {
        ehcache();
    }
}
