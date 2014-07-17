package datastorm.espershell;

import ist.smartoffice.datapointconnectivity.IDatapointConnectivityService;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import Datastream.Measure;

import com.espertech.esper.client.EPStatementException;

import datastorm.espershell.dataacquisition.ModbusDriver;
import datastorm.espershell.esperengine.EsperEngine;
import datastorm.espershell.esperengine.QueryMetadata;

/*
 * @author Diogo Anjos (diogo.silva.anjos@tecnico.ulisboa.pt)
 */

public class App {


    public static void main(String[] args) {
        EsperEngine esper = new EsperEngine();
        IDatapointConnectivityService modbusDriver = new ModbusDriver();
        modbusDriver.addDatapointListener(esper);


        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        String command = "";
        printHeaderShell();

        System.out.print(">");


        while (true) {
            try {
                line = br.readLine();
                command = command + line;
                if (command.contains(";")) {
                    dispatchCommand(command, esper, modbusDriver);
                    line = "";
                    command = "";
                    System.out.print(">");
                } else {
                    command = command + "\n";
                }

            } catch (Exception e) {
                System.out.println("Malformed command");
                line = "";
                command = "";
                System.out.print(">");
            }
        }
    }

    private static void dispatchCommand(String command,
                                        EsperEngine esper,
                                        IDatapointConnectivityService modbusDriver) {
        // add select * from stream => ['add','add select * from stream']
        String[] tokens = command.split("\\W+", 2);

        if (tokens.length != 0) {
            switch (tokens[0]) {
                case "install": // install queries
                    install_CommandHandler(tokens, esper);
                    break;
                case "push": // send events into engine
                    push_CommandHandler(tokens, esper);
                    break;
                case "list": //list installed queries and their state (des/activated)
                    list_commandHandler(esper);
                    break;
                case "enable": //activate queries
                    //turnOn -5; => ['turnOn', '-5;'] if using "\\s+", ['turnOn', '5;'] if using "\\W+"
                    enable_commandHandler(esper, command.split("\\s+", 2));
                    break;
                case "disable": //desactivate queries
                    disable_commandHandler(esper, command.split("\\s+", 2));
                    break;
                case "drop":
                    drop_commandHandler(esper, command.split("\\s+", 2));
                    break;
                case "dropall":
                    dropall_commandHandler(esper);
                    break;
                case "enableoutputfile":
                    enableOutputFile_commandHandler(esper, command.split("\\s+", 2));
                    break;
                case "disableoutputfile":
                    disableOutputFile_commandHandler(esper, command.split("\\s+", 2));
                    break;
                case "enableoutputterminal":
                    enableOtputTerminal_commandHandler(esper, command.split("\\s+", 2));
                    break;
                case "disableoutputterminal":
                    disableOtputTerminal_commandHandler(esper, command.split("\\s+", 2));
                    break;
                case "help": //all available commands
                    help_commandHandler();
                    break;
                case "enableinputterminal": //list installed queries and their state (des/activated)
                    enableInputTerminal_commandHandler(esper);
                    break;
                case "disableinputterminal": //list installed queries and their state (des/activated)
                    disableInputTerminal_commandHandler(esper);
                    break;
                case "reload": //list installed queries and their state (des/activated)
                    reloadConfig_commandHandler(modbusDriver);
                    break;
                case "run": //list installed queries and their state (des/activated)
                    runScript_commandHandler(tokens, esper, modbusDriver);
                    break;

                default:
                    System.out
                            .println("\'" + tokens[0] + "\'" + " is not recognized as a command.");
            }
        }
    }


    //add command -> install queries in the engine
    // syntax: add select * from stream;
    private static void install_CommandHandler(String[] tokens, EsperEngine esper) {
        // add select * from stream => ['add','add select * from stream']                
        String eplQuery = tokens[1]; //query that will be sent to Esper Engine

        try {
            //remover ";" do final do statement da query 
            QueryMetadata queryMetaData = esper.installQuery(eplQuery.replace(";", ""));
            System.out.println("\nQuery installed with success! \n" + queryMetaData + "\n");
        } catch (EPStatementException e) {
            System.out.println("\nCompilation Error: " + e.getMessage());
            System.out.println("Evaluated Expression: " + e.getExpression() + "\n");
        }

    }

    // syntax: send (deviceId, measure, timestamp);
    private static void push_CommandHandler(String[] tokens, EsperEngine esper) {
        // send (lib, 17, 234); => ['send','(lib, 17, 234)']                        
        String event = tokens[1]; //event: (lib, 17, 234)
        event = (event.replace("(", "")).replace(")", "").replaceAll("\\s+|;", ""); //Remove white spaces = lib,17,234
        String[] eventParts = event.split(","); // ['lib','17','234']

        String deviceID;
        double value;
        long ts;

        try {
            deviceID = eventParts[0];
            value = Double.parseDouble(eventParts[1]);
            //ts = Long.parseLong(eventParts[2]);
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + event + ")");
            return;
        }
        esper.push(new Measure(deviceID, /* ts, */value));
    }

    private static void list_commandHandler(EsperEngine esper) {
        esper.listInstalledQueries();
    }


    private static void disableOtputTerminal_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if (esper.dontPrintToTerminal(queryID)) {
                System.out.println("\nQuery " + queryID + " will not print to terminal.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }

    }

    private static void enableOtputTerminal_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if (esper.printToTerminal(queryID)) {
                System.out.println("\nQuery " + queryID + " will print to terminal.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void disableOutputFile_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if (esper.dontPrintToFile(queryID)) {
                System.out.println("\nQuery " + queryID + " will not print to file.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void enableOutputFile_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if (esper.printToFile(queryID)) {
                System.out.println("\nQuery " + queryID + " will print to file.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }


    private static void enable_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if (esper.turnOnQuery(queryID)) {
                System.out.println("\nQuery " + queryID + " is Activated.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void disable_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if (esper.turnOffQuery(queryID)) {
                System.out.println("\nQuery " + queryID + " is Desactivated.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void drop_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if (esper.dropQuery(queryID)) {
                System.out.println("\nQuery " + queryID + " was dropped.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void dropall_commandHandler(EsperEngine esper) {
        int countDroppedQueries = esper.dropAllQueries();
        System.out.println("\n" + countDroppedQueries + " queries were dropped.\n");
    }

    private static void enableInputTerminal_commandHandler(EsperEngine esper) {
        esper.setShowInput(true);
        System.out.println("\n Arriving events will be printed in terminal.\n");
    }

    private static void disableInputTerminal_commandHandler(EsperEngine esper) {
        esper.setShowInput(false);
        System.out.println("\n Arriving events will Not be printed in terminal.\n");
    }

    private static void reloadConfig_commandHandler(IDatapointConnectivityService modbusDriver) {
        modbusDriver.requestDatapointWrite(null, null, null);
        System.out.println("\n Config file has been reloaded.\n");
    }


    private static void printHeaderShell() {
        System.out.println("\n-----------------------------------------------");
        System.out.println("-     1st Data Storm Big Data Summer School   -");
        System.out.println("-        Streaming Data Hands-On Lab          -");
        System.out.println("-----------------------------------------------");
        System.out.println("\nType \"help;\" for available commands.\n");
    }


    private static void help_commandHandler() {
        System.out.println("------------- Available commands -------------");

        System.out
                .println("install <query_statement>;\n\t Install the query stated by <query_statement> in Esper, \n\t associating it with an Id.\n");

        System.out
                .println("push (meterId,measure);\n\t Push the event (deviceId,measure) into Esper query engine.\n");

        System.out.println("list;\n\t List the queries installed in Esper.\n");

        System.out
                .println("enable <query_id>;\n\t Enable the query associated with the id <query_id>.\n");

        System.out
                .println("disable <query_id>;\n\t Disable the query associated with the id <query_id>.\n");

        System.out
                .println("drop <query_id>;\n\t Remove from Esper the query associated with the id <query_id>.\n");

        System.out.println("dropall;\n\t Remove all the queries previously installed in Esper.\n");

        System.out
                .println("enableoutputfile <query_id>;\n\t Set the query related with <query_id> to print its output \n\t into file queriesOutput/<query_id>output.txt\n");

        System.out
                .println("disableoutputfile <query_id>;\n\t Set the query related with <query_id> to Do Not print its \n\t output into file queriesOutput/<query_id>output.txt\n");

        System.out
                .println("enableoutputterminal <query_id>;\n\t Set the query related with <query_id> to print its output \n\t to terminal.\n");

        System.out
                .println("disableoutputterminal <query_id>;\n\t Set the query related with <query_id> to Do Not print its output \n\t to terminal.\n");

        System.out
                .println("enableinputterminal;\n\t Display in terminal all events being sent by the sensors.\n");

        System.out
                .println("disableinputterminal;\n\t Do not display in terminal events being sent by the sensors.\n");

        System.out
                .println("run <script_filename.txt>;\n\t Run script file named scripts/<script_filename>.txt\n");

        System.out.println("reload;\n\t Reload configuration file modbusDriverConf.json\n");

        //        System.out.println("exit;\n\t End the program."); //there is no exit command

        System.out.println("----------------------------------------------\n");
    }

    private static void runScript_commandHandler(String[] tokens,
                                                 EsperEngine esper,
                                                 IDatapointConnectivityService modbusDriver) {
        String scriptFilename = tokens[1].replace(";", "");
        String script = getScriptFile(scriptFilename);
        if (script == null) {
            System.out.println("Error: Malformed script file, " + scriptFilename);
            return;
        }
        //       System.out.println(script); //DEBUG
        script = script.replaceAll("[\\t\\n\\r]+", " ");
        String[] commands = script.split(";");
        ; //comandos sem ; no fim
        for (String command : commands) {
            if (command.length() > 1) {
                command = command.replaceFirst("^ *", "");//remove all empty spaces before keyword (e.g '   list;' -> 'list;')
                //System.out.println("dispatching command<"+command+">size: "+commands.length); //DEBUG
                dispatchCommand(command, esper, modbusDriver);
            }
        }
    }


    private static String getScriptFile(String scriptFilename) {
        BufferedReader br = null;
        String scriptString = null;
        StringBuilder sb = new StringBuilder();
        try {
            //           System.out.println("[DEBUG]Looking for file here: "+ System.getProperty("user.dir")); //TODO DEBUG 
            br = new BufferedReader(new FileReader("../scripts/" + scriptFilename));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            scriptString = sb.toString();
        } catch (IOException e) {
            System.out.println("System cannot find the script file '" + scriptFilename
                    + "', do you forgot file extension(.txt) ?");

            return null;
        }
        return scriptString;
    }
}
