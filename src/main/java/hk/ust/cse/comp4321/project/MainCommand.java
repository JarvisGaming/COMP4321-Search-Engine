package hk.ust.cse.comp4321.project;

import picocli.CommandLine.*;


public class MainCommand {
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays this message.")
    public boolean helpRequested;
}
