package net.minecraft.crash;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.minecraft.util.ReportedException;
import net.minecraft.world.gen.layer.IntCache;
import net.optifine.CrashReporter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import tritium.Tritium;
import tritium.utils.logging.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CrashReport {
    private static final Logger logger = LogManager.getLogger("CrashReport");

    /**
     * Description of the crash report.
     * -- GETTER --
     * Returns the description of the Crash Report.
     */
    @Getter
    private final String description;

    /**
     * The Throwable that is the "cause" for this crash and Crash Report.
     */
    private final Throwable cause;

    /**
     * Category of crash
     */
    private final CrashReportCategory theReportCategory = new CrashReportCategory("系统信息");
    private final List<CrashReportCategory> crashReportSections = Lists.newArrayList();

    /**
     * File of crash report.
     */
    private File crashReportFile;

    /**
     * Is true when the current category is the first in the crash report
     */
    private boolean firstCategoryInCrashReport = true;
    private StackTraceElement[] stacktrace = new StackTraceElement[0];
    private boolean reported = false;

    public CrashReport(String descriptionIn, Throwable causeThrowable) {
        this.description = descriptionIn;
        this.cause = causeThrowable;
        this.populateEnvironment();
    }

    /**
     * Populates this crash report with initial information about the running server and operating system / java
     * environment
     */
    private void populateEnvironment() {
        this.theReportCategory.addCrashSectionCallable("《我的手艺》 版本", () -> "1.8.9");
        this.theReportCategory.addCrashSectionCallable("氚 版本", () -> Tritium.getVersion().toString());
        this.theReportCategory.addCrashSectionCallable("操作系统", () -> System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") 版本 " + System.getProperty("os.version"));
        this.theReportCategory.addCrashSectionCallable("Java版本", () -> System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        this.theReportCategory.addCrashSectionCallable("Java虚拟机版本", () -> System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor"));
        this.theReportCategory.addCrashSectionCallable("内存", () -> {
            Runtime runtime = Runtime.getRuntime();
            long i = runtime.maxMemory();
            long j = runtime.totalMemory();
            long k = runtime.freeMemory();
            long l = i / 1024L / 1024L;
            long i1 = j / 1024L / 1024L;
            long j1 = k / 1024L / 1024L;
            return k + " 字节 (" + j1 + " MB) / " + j + " 字节 (" + i1 + " MB) 最高 " + i + " 字节 (" + l + " MB)";
        });
        this.theReportCategory.addCrashSectionCallable("Java虚拟机参数", () -> {
            RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
            List<String> list = runtimemxbean.getInputArguments();
            int i = 0;
            StringBuilder stringbuilder = new StringBuilder();

            for (String s : list) {
                if (s.startsWith("-X")) {
                    if (i++ > 0) {
                        stringbuilder.append(" ");
                    }

                    stringbuilder.append(s);
                }
            }

            return String.format("总共%d个; %s", i, stringbuilder);
        });
        this.theReportCategory.addCrashSectionCallable("整型缓存池", IntCache::getCacheSizes);
    }

    /**
     * Returns the Throwable object that is the cause for the crash and Crash Report.
     */
    public Throwable getCrashCause() {
        return this.cause;
    }

    /**
     * Gets the various sections of the crash report into the given StringBuilder
     */
    public void getSectionsInStringBuilder(StringBuilder builder) {
        if ((this.stacktrace == null || this.stacktrace.length == 0) && !this.crashReportSections.isEmpty()) {
            this.stacktrace = ArrayUtils.subarray(this.crashReportSections.getFirst().getStackTrace(), 0, 1);
        }

        if (this.stacktrace != null && this.stacktrace.length > 0) {
            builder.append("堆栈跟踪:\n");

            for (StackTraceElement stacktraceelement : this.stacktrace) {
                builder.append("\t").append("在 ").append(stacktraceelement.toString());
                builder.append("\n");
            }

            builder.append("\n");
        }

        for (CrashReportCategory crashreportcategory : this.crashReportSections) {
            crashreportcategory.appendToStringBuilder(builder);
            builder.append("\n\n");
        }

        this.theReportCategory.appendToStringBuilder(builder);
    }

    /**
     * Gets the stack trace of the Throwable that caused this crash report, or if that fails, the cause .toString().
     */
    public String getCauseStackTraceOrString() {
        StringWriter stringwriter = null;
        PrintWriter printwriter = null;
        Throwable throwable = this.cause;

        if (throwable.getMessage() == null) {
            switch (throwable) {
                case NullPointerException nullPointerException ->
                        throwable = new NullPointerException(this.description);
                case StackOverflowError stackOverflowError -> throwable = new StackOverflowError(this.description);
                case OutOfMemoryError outOfMemoryError -> throwable = new OutOfMemoryError(this.description);
                default -> {
                }
            }

            throwable.setStackTrace(this.cause.getStackTrace());
        }

        String s;

        try {
            stringwriter = new StringWriter();
            printwriter = new PrintWriter(stringwriter);
            throwable.printStackTrace(printwriter);
            s = stringwriter.toString();
        } finally {
            IOUtils.closeQuietly(stringwriter);
            IOUtils.closeQuietly(printwriter);
        }

        return s;
    }

    /**
     * Gets the complete report with headers, stack trace, and different sections as a string.
     */
    public String getCompleteReport() {
        if (!this.reported) {
            this.reported = true;
            CrashReporter.onCrashReport(this, this.theReportCategory);
        }

        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append("---- 《我的手艺》 崩溃报告 ----\n");
        stringbuilder.append("// ");
        stringbuilder.append(getWittyComment());
        stringbuilder.append("\n\n");
        stringbuilder.append("时间: ");
        stringbuilder.append((new SimpleDateFormat()).format(new Date()));
        stringbuilder.append("\n");
        stringbuilder.append("描述: ");
        stringbuilder.append(this.description);
        stringbuilder.append("\n\n");
        stringbuilder.append(this.getCauseStackTraceOrString());
        stringbuilder.append("\n\n以下是对该异常的详细解析, 包括调用栈和所有已知细节:\n");

        stringbuilder.append("-".repeat(87));

        stringbuilder.append("\n\n");
        this.getSectionsInStringBuilder(stringbuilder);
        return stringbuilder.toString();
    }

    /**
     * Gets the file this crash report is saved into.
     */
    public File getFile() {
        return this.crashReportFile;
    }

    /**
     * Saves this CrashReport to the given file and returns a value indicating whether we were successful at doing so.
     */
    public boolean saveToFile(File toFile) {
        if (this.crashReportFile != null) {
            return false;
        } else {
            if (toFile.getParentFile() != null) {
                toFile.getParentFile().mkdirs();
            }

            try {
                FileWriter filewriter = new FileWriter(toFile);
                filewriter.write(this.getCompleteReport());
                filewriter.close();
                this.crashReportFile = toFile;
                return true;
            } catch (Throwable throwable) {
                logger.error("Could not save crash report to {}", toFile, throwable);
                return false;
            }
        }
    }

    public CrashReportCategory getCategory() {
        return this.theReportCategory;
    }

    /**
     * Creates a CrashReportCategory
     */
    public CrashReportCategory makeCategory(String name) {
        return this.makeCategoryDepth(name, 1);
    }

    /**
     * Creates a CrashReportCategory for the given stack trace depth
     */
    public CrashReportCategory makeCategoryDepth(String categoryName, int stacktraceLength) {
        CrashReportCategory crashreportcategory = new CrashReportCategory(categoryName);

        if (this.firstCategoryInCrashReport) {
            int i = crashreportcategory.getPrunedStackTrace(stacktraceLength);
            StackTraceElement[] astacktraceelement = this.cause.getStackTrace();
            StackTraceElement stacktraceelement = null;
            StackTraceElement stacktraceelement1 = null;
            int j = astacktraceelement.length - i;

            if (j < 0) {
                System.out.println("Negative index in crash report handler (" + astacktraceelement.length + "/" + i + ")");
            }

            if (0 <= j && j < astacktraceelement.length) {
                stacktraceelement = astacktraceelement[j];

                if (astacktraceelement.length + 1 - i < astacktraceelement.length) {
                    stacktraceelement1 = astacktraceelement[astacktraceelement.length + 1 - i];
                }
            }

            this.firstCategoryInCrashReport = crashreportcategory.firstTwoElementsOfStackTraceMatch(stacktraceelement, stacktraceelement1);

            if (i > 0 && !this.crashReportSections.isEmpty()) {
                CrashReportCategory crashreportcategory1 = this.crashReportSections.getLast();
                crashreportcategory1.trimStackTraceEntriesFromBottom(i);
            } else if (astacktraceelement.length >= i && 0 <= j && j < astacktraceelement.length) {
                this.stacktrace = new StackTraceElement[j];
                System.arraycopy(astacktraceelement, 0, this.stacktrace, 0, this.stacktrace.length);
            } else {
                this.firstCategoryInCrashReport = false;
            }
        }

        this.crashReportSections.add(crashreportcategory);
        return crashreportcategory;
    }

    /**
     * Gets a random witty comment for inclusion in this CrashReport
     */
    private static String getWittyComment() {
//        String[] astring = new String[] {"Who set us up the TNT?", "Everything's going to plan. No, really, that was supposed to happen.", "Uh... Did I do that?", "Oops.", "Why did you do that?", "I feel sad now :(", "My bad.", "I'm sorry, Dave.", "I let you down. Sorry :(", "On the bright side, I bought you a teddy bear!", "Daisy, daisy...", "Oh - I know what I did wrong!", "Hey, that tickles! Hehehe!", "I blame Dinnerbone.", "You should try our sister game, Minceraft!", "Don't be sad. I'll do better next time, I promise!", "Don't be sad, have a hug! <3", "I just don't know what went wrong :(", "Shall we play a game?", "Quite honestly, I wouldn't worry myself about that.", "I bet Cylons wouldn't have this problem.", "Sorry :(", "Surprise! Haha. Well, this is awkward.", "Would you like a cupcake?", "Hi. I'm Minecraft, and I'm a crashaholic.", "Ooh. Shiny.", "This doesn't make any sense!", "Why is it breaking :(", "Don't do that.", "Ouch. That hurt :(", "You're mean.", "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]", "There are four lights!", "But it works on my machine."};

        String[] astring = Arrays.asList(
                "我看看...",
                "但是它在我电脑上跑的好好的啊?!",
                "怎么会是呢",
                "不了解",
                "额 怎么感觉不是我的问题呢",
                "IT'S NOT MY FAULT",
                "LMAO"
        ).toArray(new String[0]);

        try {
            return astring[(int) (System.nanoTime() % (long) astring.length)];
        } catch (Throwable var2) {
            return "Witty comment 暂时缺席 :(";
        }
    }

    /**
     * Creates a crash report for the exception
     */
    public static CrashReport makeCrashReport(Throwable causeIn, String descriptionIn) {
        CrashReport crashreport;

        if (causeIn instanceof ReportedException) {
            crashreport = ((ReportedException) causeIn).getCrashReport();
        } else {
            crashreport = new CrashReport(descriptionIn, causeIn);
        }

        return crashreport;
    }
}
