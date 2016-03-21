package com.github.wens.netty.web.util;

import com.github.wens.netty.web.example.Result;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Created by wens on 15-9-2.
 */
public class JsonUtilsTest extends TestCase {

    public void test_0() {

        Result result = new Result(11, "hi");

        byte[] serialize = JsonUtils.serialize(result);

        Assert.assertEquals("{\"code\":11,\"msg\":\"hi\"}", new String(serialize));


    }

}
