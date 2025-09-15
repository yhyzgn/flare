package com.yhy.http.flare.test;

import com.yhy.http.flare.Flare;
import com.yhy.http.flare.test.remote.TypeCodeApi;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * 测试类
 * <p>
 * Created on 2025-09-15 17:15
 *
 * @author 颜洪毅
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class FlareTest {

    @Test
    public void posts() {
        Flare flare = new Flare.Builder()
                .baseUrl(TypeCodeApi.BASE_URL)
                // .logEnabled(true)
                .build();

        TypeCodeApi api = flare.create(TypeCodeApi.class);

        String posts = api.posts();

        log.info("posts: {}", posts.length());
    }
}
