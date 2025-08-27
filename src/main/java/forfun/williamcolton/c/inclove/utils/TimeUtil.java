package forfun.williamcolton.c.inclove.utils;

import cn.hutool.core.date.LocalDateTimeUtil;

import java.time.LocalDate;

public class TimeUtil {

    public static Integer countAge(LocalDate birthday, LocalDate currentTime) {
        return LocalDateTimeUtil.betweenPeriod(birthday, currentTime).getYears();
    }

}
