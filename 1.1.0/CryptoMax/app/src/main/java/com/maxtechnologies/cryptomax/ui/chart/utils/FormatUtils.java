package com.maxtechnologies.cryptomax.ui.chart.utils;

import com.maxtechnologies.cryptomax.misc.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Colman on 29/06/2018.
 */

public class FormatUtils {

    public static ArrayList<ArrayList<Integer>> getSigCandles(int widthSec, int candlesSize, long lastTime) {
        ArrayList<ArrayList<Integer>> sigCandles = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        if(widthSec == 60) {
            sigCandles.add(new ArrayList<Integer>());
            for (int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int minute = calendar.get(Calendar.MINUTE);
                if ((minute >= 0 && minute < 1)|| (minute >= 30 && minute < 31)) {
                    sigCandles.get(0).add(i);
                }
            }
        }

        else if(widthSec == 180) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int minute = calendar.get(Calendar.MINUTE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if ((minute >= 0 && minute < 3) || (minute >= 30 && minute < 33)) {
                    sigCandles.get(0).add(i);
                }
                if(minute >= 0 && minute < 3) {
                    sigCandles.get(1).add(i);

                    if(hour % 6 == 0) {
                        sigCandles.get(2).add(i);
                    }
                }
            }
        }

        else if(widthSec == 300) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int minute = calendar.get(Calendar.MINUTE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if ((minute >= 0 && minute < 5)|| (minute >= 30 && minute < 35)) {
                    sigCandles.get(0).add(i);
                }
                if(minute >= 0 && minute < 5) {
                    sigCandles.get(1).add(i);

                    if(hour % 6 == 0) {
                        sigCandles.get(2).add(i);
                    }
                }
            }
        }

        else if(widthSec == 900) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int minute = calendar.get(Calendar.MINUTE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if(minute >= 0 && minute < 15) {
                    sigCandles.get(0).add(i);

                    if(hour % 6 == 0) {
                        sigCandles.get(1).add(i);
                    }
                }
            }
        }

        else if(widthSec == 1800) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int minute = calendar.get(Calendar.MINUTE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if(minute >= 0 && minute < 30) {
                    if(hour % 6 == 0) {
                        sigCandles.get(0).add(i);
                    }
                    if(hour == 0) {
                        sigCandles.get(1).add(i);
                    }
                }
            }
        }

        else if(widthSec == 3600) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if(hour % 6 == 0) {
                    sigCandles.get(0).add(i);
                }
                if(hour == 0) {
                    sigCandles.get(1).add(i);
                }
            }
        }

        else if(widthSec == 7200) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                if(hour % 6 >= 0 && hour % 6 < 2) {
                    sigCandles.get(0).add(i);
                }
                if(hour >= 0 && hour < 2) {
                    sigCandles.get(1).add(i);

                    if(dayOfWeek == Calendar.SUNDAY) {
                        sigCandles.get(2).add(i);
                    }
                }
            }
        }

        else if(widthSec == 14400) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                if(hour >= 0 && hour < 4) {
                    sigCandles.get(0).add(i);

                    if(dayOfWeek == Calendar.SUNDAY) {
                        sigCandles.get(1).add(i);
                    }
                }
            }
        }

        else if(widthSec == 21600) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                if(hour >= 0 && hour < 6) {
                    sigCandles.get(0).add(i);

                    if(dayOfWeek == Calendar.SUNDAY) {
                        sigCandles.get(1).add(i);
                    }
                }
            }
        }

        else if(widthSec == 43200) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                if(hour >= 0 && hour < 12) {
                    if (dayOfWeek == Calendar.SUNDAY) {
                        sigCandles.get(0).add(i);
                    }
                    if (dayOfMonth == 1) {
                        sigCandles.get(1).add(i);
                    }
                }
            }
        }

        else if(widthSec == 86400) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                if(dayOfWeek == Calendar.SUNDAY) {
                    sigCandles.get(0).add(i);
                }
                if(dayOfMonth == 1) {
                    sigCandles.get(1).add(i);
                }
            }
        }

        else if(widthSec == 259200) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                if(dayOfMonth >= 1 && dayOfMonth < 4) {
                    sigCandles.get(0).add(i);
                    if(month % 3 == 0) {
                        sigCandles.get(1).add(i);
                    }
                }
            }
        }

        else if(widthSec == 604800) {
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());
            sigCandles.add(new ArrayList<Integer>());

            for(int i = 0; i < candlesSize; i++) {
                long time = lastTime - ((candlesSize - 1) - i) * widthSec;
                calendar.setTimeInMillis(time * 1000);

                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                if(dayOfMonth >= 1 && dayOfMonth < 8) {
                    sigCandles.get(0).add(i);
                    if(month % 3 == 0) {
                        sigCandles.get(1).add(i);
                    }
                    if(month % 6 == 0) {
                        sigCandles.get(2).add(i);
                    }
                }
            }
        }

        return sigCandles;
    }



    public static String getChartDateString(long time, int widthSec, int sigIndex) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time * 1000);

        SimpleDateFormat format;
        if(widthSec == 60) {
            int minute = calendar.get(Calendar.MINUTE);
            if(minute >= 0 && minute < 30) {
                calendar.set(Calendar.MINUTE, 0);
            }
            else {
                calendar.set(Calendar.MINUTE, 30);
            }

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if(hour == 0) {
                format = new SimpleDateFormat("MMM dd", Locale.US);
            }
            else {
                if(Settings.times == 0) {
                    format = new SimpleDateFormat("h:mm a", Locale.US);
                }
                else {
                    format = new SimpleDateFormat("H:mm", Locale.US);
                }
            }
        }

        else if(widthSec == 180 || widthSec == 300) {
            int minute = calendar.get(Calendar.MINUTE);
            if(sigIndex == 0) {
                if(minute >= 0 && minute < 30) {
                    calendar.set(Calendar.MINUTE, 0);
                }
                else {
                    calendar.set(Calendar.MINUTE, 30);
                }
            }
            else {
                calendar.set(Calendar.MINUTE, 0);
            }

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if(hour == 0) {
                format = new SimpleDateFormat("MMM dd", Locale.US);
            }
            else {
                if(Settings.times == 0) {
                    format = new SimpleDateFormat("h:mm a", Locale.US);
                }
                else {
                    format = new SimpleDateFormat("H:mm", Locale.US);
                }
            }
        }

        else if(widthSec == 900) {
            calendar.set(Calendar.MINUTE, 0);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if(hour == 0) {
                format = new SimpleDateFormat("MMM dd", Locale.US);
            }
            else {
                if(Settings.times == 0) {
                    format = new SimpleDateFormat("h:mm a", Locale.US);
                }
                else {
                    format = new SimpleDateFormat("H:mm", Locale.US);
                }
            }
        }

        else if(widthSec == 1800 || widthSec == 3600) {
            if(sigIndex == 0) {
                calendar.set(Calendar.MINUTE, 0);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if(hour == 0) {
                    format = new SimpleDateFormat("MMM dd", Locale.US);
                }
                else {
                    if(Settings.times == 0) {
                        format = new SimpleDateFormat("h:mm a", Locale.US);
                    }
                    else {
                        format = new SimpleDateFormat("H:mm", Locale.US);
                    }
                }
            }
            else {
                format = new SimpleDateFormat("MMM dd", Locale.US);
            }
        }

        else if(widthSec == 7200) {
            if(sigIndex == 0) {
                calendar.set(Calendar.MINUTE, 0);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if(hour % 6 >= 0 && hour % 6 < 2) {
                    calendar.set(Calendar.HOUR_OF_DAY, hour - (hour % 6));
                }

                int hour2 = calendar.get(Calendar.HOUR_OF_DAY);
                if(hour2 == 0) {
                    format = new SimpleDateFormat("MMM dd", Locale.US);
                }
                else {
                    if(Settings.times == 0) {
                        format = new SimpleDateFormat("h:mm a", Locale.US);
                    }
                    else {
                        format = new SimpleDateFormat("H:mm", Locale.US);
                    }
                }
            }
            else {
                format = new SimpleDateFormat("MMM dd", Locale.US);
            }
        }

        else if(widthSec == 14400 || widthSec == 21600) {
            format = new SimpleDateFormat("MMM dd", Locale.US);
        }

        else if(widthSec == 43200 || widthSec == 86400) {
            if(sigIndex == 0) {
                format = new SimpleDateFormat("MMM dd", Locale.US);
            }
            else {
                if(calendar.get(Calendar.MONTH) == 0) {
                    format = new SimpleDateFormat("yyyy", Locale.US);
                }
                else {
                    format = new SimpleDateFormat("MMM", Locale.US);
                }
            }
        }

        else if(widthSec == 259200 || widthSec == 604800) {
            if(calendar.get(Calendar.MONTH) == 0) {
                format = new SimpleDateFormat("yyyy", Locale.US);
            }
            else {
                format = new SimpleDateFormat("MMM", Locale.US);
            }
        }

        else {
            format = new SimpleDateFormat("MMM dd", Locale.US);
        }

        return format.format(calendar.getTime());
    }
}
