package forfun.williamcolton.c.inclove.component;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

@Component
public class SIDSnowflakeBuilder {

    private static final Snowflake snowflake = IdUtil.getSnowflake();

    public static String getNextSID() {
        return SIDSnowflakeBuilder.snowflake.nextIdStr();
    }

}
