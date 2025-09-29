package net.optifine;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;

public class CrashReporter {
    public static void onCrashReport(final CrashReport crashReport, final CrashReportCategory category) {
        try {
            final Throwable throwable = crashReport.getCrashCause();
            if (throwable == null) {
                return;
            }
            if (throwable.getClass() == Throwable.class) {
                return;
            }
            extendCrashReport(category);
        } catch (final Exception exception) {
            Config.dbg(exception.getClass().getName() + ": " + exception.getMessage());
        }
    }

    public static void extendCrashReport(final CrashReportCategory cat) {

    }
}
