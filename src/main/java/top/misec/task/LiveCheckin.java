package top.misec.task;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import top.misec.apiquery.ApiList;
import top.misec.utils.HttpUtil;

import static top.misec.task.TaskInfoHolder.statusCodeStr;

/**
 * 直播签到
 *
 * @author @JunzhouLiu @Kurenai
 * @since 2020-11-22 5:42
 */

public class LiveCheckin implements Task {

    static Logger logger = (Logger) LogManager.getLogger(LiveCheckin.class.getName());

    private final String taskName = "直播签到";

    @Override
    public void run() {
        JsonObject liveCheckinResponse = HttpUtil.doGet(ApiList.liveCheckin);
        int code = liveCheckinResponse.get(statusCodeStr).getAsInt();
        if (code == 0) {
            JsonObject data = liveCheckinResponse.get("data").getAsJsonObject();
            logger.info("直播签到成功，本次签到获得" + data.get("text").getAsString() + "," + data.get("specialText").getAsString());
        } else {
            String message = liveCheckinResponse.get("message").getAsString();
            logger.debug("直播签到失败: " + message);
        }
    }

    @Override
    public String getName() {
        return taskName;
    }
}
