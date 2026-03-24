package hk.ust.cse.comp4321.project;

import hk.ust.cse.comp4321.project.crawl.CrawlCommand;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;


public class Main {
    static void main(String[] args) {
        MainCommand options = new MainCommand();
        CommandLine cmdLine = new CommandLine(options)
                .addSubcommand(new CrawlCommand())
                .setCommandName("phase1");
        ParseResult result = cmdLine.parseArgs(args);
        if (result.hasSubcommand())
            handleSubcommand(result.subcommand());
        else
            handleOptions(options, cmdLine);
    }

    private static void handleOptions(@NotNull MainCommand result, @NotNull CommandLine cmdLine) {
        if (result.helpRequested)
            cmdLine.usage(System.out);
    }

    private static void handleSubcommand(@NotNull ParseResult result) {
        CommandSpec spec = result.commandSpec();
        spec.commandLine().execute();
    }
}
