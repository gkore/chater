package com.example.chaterv3;

import java.util.Calendar;
import java.util.TimeZone;

public class ILikeTime {
    static int getTimeZoneFromMSK(){
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        return (tz.getRawOffset() - 10800000) / 1000; // 10800000 = 3часа (MSK = +3 GMT, server is in msk)
    }
}
