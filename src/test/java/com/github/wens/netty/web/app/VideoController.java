package com.github.wens.netty.web.app;

import com.github.wens.netty.web.annotaction.Controller;
import com.github.wens.netty.web.annotaction.PathValue;
import com.github.wens.netty.web.annotaction.Router;

/**
 * Created by wens on 15-5-15.
 */
@Controller
public class VideoController {

    @Router("/api/index")
    public void index() {

    }

    @Router("/api/.*/like/{id}")
    public void like(@PathValue("id") String id) {
        if (id == null) {
            throw new RuntimeException();
        }
    }

    @Router("/api/{appkey}/videos")
    public void videos(@PathValue("appkey") String appkey) {
        if (appkey == null) {
            throw new RuntimeException();
        }
    }

    @Router("/api/{appkey}/video/{videoid}")
    public void video(@PathValue("appkey") String appkey, @PathValue("videoid") String videoid) {
        if (appkey == null || videoid == null) {
            throw new RuntimeException();
        }
    }

    @Router("/api/{appkey:[A-Z]+}/videos/{pagesize:\\d+}")
    public void videos(@PathValue("appkey") String appkey, @PathValue("pagesize") String pageSize) {
        if (appkey == null || pageSize == null) {
            throw new RuntimeException();
        }
    }

}
