package nl.appsource.badge.lib;

import java.util.ArrayList;
import java.util.Objects;

import static java.util.Arrays.asList;

public class Widths {

    public static int getWidthOfString(final String text) {
        return (int) text.chars()
                .map(x -> x >= 0 && x <= 255 ? x : 32)
                .mapToDouble(simpleWidths::get)
                .sum()
                ;
    }

    public static final ArrayList<Double> simpleWidths = new ArrayList<>(asList(
            null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , 38.67
            , 43.29
            , 50.49
            , 90.02
            , 69.93
            , 118.38
            , 79.92
            , 29.54
            , 49.95
            , 49.95
            , 69.93
            , 90.02
            , 40.01
            , 49.95
            , 40.01
            , 49.95
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 49.95
            , 49.95
            , 90.02
            , 90.02
            , 90.02
            , 60.0
            , 110.0
            , 75.2
            , 75.41
            , 76.81
            , 84.76
            , 69.56
            , 63.22
            , 85.29
            , 82.66
            , 46.3
            , 50.0
            , 76.22
            , 61.23
            , 92.71
            , 82.29
            , 86.58
            , 66.33
            , 86.58
            , 76.48
            , 75.2
            , 67.78
            , 80.51
            , 75.2
            , 108.76
            , 75.36
            , 67.68
            , 75.36
            , 49.95
            , 49.95
            , 49.95
            , 90.02
            , 69.93
            , 69.93
            , 66.06
            , 68.54
            , 57.31
            , 68.54
            , 65.53
            , 38.67
            , 68.54
            , 69.61
            , 30.19
            , 37.87
            , 65.1
            , 30.19
            , 106.99
            , 69.61
            , 66.76
            , 68.54
            , 68.54
            , 46.94
            , 57.31
            , 43.34
            , 69.61
            , 65.1
            , 90.02
            , 65.1
            , 65.1
            , 57.79
            , 69.82
            , 49.95
            , 69.82
            , 90.02
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , null
            , 38.67
            , 43.29
            , 69.93
            , 69.93
            , 69.93
            , 69.93
            , 49.95
            , 69.93
            , 69.93
            , 110.0
            , 60.0
            , 70.9
            , 90.02
            , 0.0
            , 110.0
            , 69.93
            , 59.62
            , 90.02
            , 59.62
            , 59.62
            , 69.93
            , 70.58
            , 69.93
            , 40.01
            , 69.93
            , 59.62
            , 60.0
            , 70.9
            , 110.0
            , 110.0
            , 110.0
            , 60.0
            , 75.2
            , 75.2
            , 75.2
            , 75.2
            , 75.2
            , 75.2
            , 108.28
            , 76.81
            , 69.56
            , 69.56
            , 69.56
            , 69.56
            , 46.3
            , 46.3
            , 46.3
            , 46.3
            , 85.29
            , 82.29
            , 86.58
            , 86.58
            , 86.58
            , 86.58
            , 86.58
            , 90.02
            , 86.58
            , 80.51
            , 80.51
            , 80.51
            , 80.51
            , 67.68
            , 66.6
            , 68.21
            , 66.06
            , 66.06
            , 66.06
            , 66.06
            , 66.06
            , 66.06
            , 105.06
            , 57.31
            , 65.53
            , 65.53
            , 65.53
            , 65.53
            , 30.19
            , 30.19
            , 30.19
            , 30.19
            , 67.3
            , 69.61
            , 66.76
            , 66.76
            , 66.76
            , 66.76
            , 66.76
            , 90.02
            , 66.76
            , 69.61
            , 69.61
            , 69.61
            , 69.61
            , 65.1
            , 68.54
            , null));

}
