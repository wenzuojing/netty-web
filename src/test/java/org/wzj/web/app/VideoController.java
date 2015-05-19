package org.wzj.web.app;

import org.wzj.web.annotaction.Controller;
import org.wzj.web.annotaction.PathValue;
import org.wzj.web.annotaction.Router;

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
