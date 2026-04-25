package hk.ust.cse.comp4321.project;

import hk.ust.cse.comp4321.project.crawl.CrawlCommand;
import hk.ust.cse.comp4321.project.database.DBDeleteCommand;
import hk.ust.cse.comp4321.project.database.DBViewCommand;
import hk.ust.cse.comp4321.project.retrieval.SearchCommand;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParseResult;

import java.util.Arrays;


public class Main {
    static void main(String[] args) {
        MainCommand options = new MainCommand();
        CommandLine cmdLine = new CommandLine(options)
                .addSubcommand(new CrawlCommand())
                .addSubcommand(new DBViewCommand())
                .addSubcommand(new DBDeleteCommand())
                .addSubcommand(new SearchCommand())
                .setCommandName("phase2");
        ParseResult result = cmdLine.parseArgs(args);
        if (result.hasSubcommand())
            handleSubcommand(result.subcommand(), args);
        else
            handleOptions(options, cmdLine);
    }

    private static void handleOptions(@NotNull MainCommand result, @NotNull CommandLine cmdLine) {
        if (result.helpRequested)
            cmdLine.usage(System.out);
    }

    private static void handleSubcommand(@NotNull ParseResult result, String[] args) {
        CommandSpec spec = result.commandSpec();

        String[] effectiveArgs = Arrays.copyOfRange(args, 1, args.length);
        spec.commandLine().execute(effectiveArgs);
    }
}
