package datastorm.espershell;
import ist.smartoffice.datapointconnectivity.IDatapointConnectivityService;

import java.io.BufferedReader;
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

//------------------------------------
//TODO bugs to solve:
//TODO shell aceita (e não devia) query correcta + \n + lixo 
//-----------------------------------



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
                    if (command.equals("exit;")) {
                        return;
                    }
                    dispatchCommand(command, esper, modbusDriver);
                    line = "";
                    command = "";
                    System.out.print(">");
                } else {
                    command = command + "\n";
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void dispatchCommand(String command, EsperEngine esper, IDatapointConnectivityService modbusDriver) {
        // add select * from stream => ['add','add select * from stream']
        String[] tokens = command.split("\\W+", 2);

        if (tokens.length != 0) {
            switch (tokens[0]) {
                case "add": // install queries
                    add_CommandHandler(tokens, esper);
                    break; 
                case "send": // send events into engine
                    send_CommandHandler(tokens, esper);
                    break; 
                case "list": //list installed queries and their state (des/activated)
                    list_commandHandler(esper);
                    break;
                case "turnOn": //activate queries
                    //turnOn -5; => ['turnOn', '-5;'] if using "\\s+", ['turnOn', '5;'] if using "\\W+"
                    turnON_commandHandler(esper,command.split("\\s+", 2)); 
                    break;
                case "turnOff": //desactivate queries
                    turnOFF_commandHandler(esper,command.split("\\s+", 2));
                    break;
                case "drop" :
                    drop_commandHandler(esper,command.split("\\s+", 2));
                    break;
                case "dropAll" :
                    dropAll_commandHandler(esper);
                    break;
                case "printToFile" :
                    printToFile_commandHandler(esper,command.split("\\s+", 2));
                    break;
                case "dontPrintToFile" :
                    dontPrintToFile_commandHandler(esper,command.split("\\s+", 2));
                    break;
                case "printToTerminal" :
                    printToTerminal_commandHandler(esper,command.split("\\s+", 2));
                    break;
                case "dontPrintToTerminal" :
                    dontPrintToTerminal_commandHandler(esper,command.split("\\s+", 2));
                    break;
                case "help": //all available commands
                    help_commandHandler();
                    break;
                case "showInput": //list installed queries and their state (des/activated)
                    showInput_commandHandler(esper);
                    break;
                case "dontShowInput": //list installed queries and their state (des/activated)
                    dontShowInput_commandHandler(esper);
                    break;    
                case "loadConfig": //list installed queries and their state (des/activated)
                    reloadConfig_commandHandler(modbusDriver);
                    break;
                    
                default:
                    System.out.println("\'" + tokens[0] + "\'" + " is not recognized as a command.");
            }
        }
    }


    //add command -> install queries in the engine
    // syntax: add select * from stream;
    private static void add_CommandHandler(String[] tokens, EsperEngine esper) {
        // add select * from stream => ['add','add select * from stream']                
        String eplQuery = tokens[1]; //query that will be sent to Esper Engine

        try {
            //remover ";" do final do statement da query 
            //TODO[Fix] mete isto a devlver uma string em vez do objecto esta classe nao te de conhecer este bjecto
            QueryMetadata queryMetaData = esper.installQuery(eplQuery.replace(";", ""));
            System.out.println("\nQuery installed with success! \n" + queryMetaData + "\n");
        } catch (EPStatementException e) {
            System.out.println("\nCompilation Error: " + e.getMessage());
            System.out.println("Evaluated Expression: " + e.getExpression() + "\n");
        }

    }

    // syntax: send (deviceId, measure, timestamp);
    private static void send_CommandHandler(String[] tokens, EsperEngine esper) {
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
            ts = Long.parseLong(eventParts[2]);
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + event + ")");
            return;
        }
        esper.push(new Measure(deviceID, ts, value));
    }

    private static void list_commandHandler(EsperEngine esper) {
        esper.listInstalledQueries();
    }

    
    private static void dontPrintToTerminal_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if(esper.dontPrintToTerminal(queryID)){
                System.out.println("\nQuery "+queryID+" will not print to terminal.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
        
    }

    private static void printToTerminal_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if(esper.printToTerminal(queryID)){
                System.out.println("\nQuery "+queryID+" will print to terminal.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void dontPrintToFile_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if(esper.dontPrintToFile(queryID)){
                System.out.println("\nQuery "+queryID+" will not print to file.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void printToFile_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if(esper.printToFile(queryID)){
                System.out.println("\nQuery "+queryID+" will print to file.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }
    
    
    private static void turnON_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if(esper.turnOnQuery(queryID)){
                System.out.println("\nQuery "+queryID+" is Activated.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }

    private static void turnOFF_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if(esper.turnOffQuery(queryID)){
                System.out.println("\nQuery "+queryID+" is Desactivated.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }
    
    private static void drop_commandHandler(EsperEngine esper, String[] tokens) {
        try {
            int queryID = Integer.parseInt(tokens[1].replace(";", ""));
            if(esper.dropQuery(queryID)){
                System.out.println("\nQuery "+queryID+" was dropped.\n");
            }
        } catch (Exception e) {
            System.out.println("Error: Malformed input around (" + tokens[1] + ")");
        }
    }
    
    private static void dropAll_commandHandler(EsperEngine esper) {
        int countDroppedQueries = esper.dropAllQueries();
        System.out.println("\n"+countDroppedQueries+" queries were dropped.\n");
    }
    
    private static void showInput_commandHandler(EsperEngine esper) {
        esper.setShowInput(true);        
        System.out.println("\n Arriving events will be printed in terminal.\n");
    }
    
    private static void dontShowInput_commandHandler(EsperEngine esper) {
        esper.setShowInput(false);
        System.out.println("\n Arriving events will Not be printed in terminal.\n");        
    }
    
    private static void reloadConfig_commandHandler(IDatapointConnectivityService modbusDriver){
        System.out.println("---1");
        modbusDriver.requestDatapointWrite(null, null, null);
        System.out.println("\n Config file has been reloaded.\n");
    }
    
    
    private static void printHeaderShell(){
        System.out.println("\n-----------------------------------------------");
        System.out.println("-     1st Data Storm Big Data Summer School   -");
        System.out.println("-        Streaming Data Hands-On Lab          -");
        System.out.println("-----------------------------------------------");
        System.out.println("\nType \"help;\" for available commands.\n");
    }
    
        
    private static void help_commandHandler(){
        System.out.println("------------- Available commands -------------");
        System.out.println("add query_statement;\n\tInstall the query stated by <query_statement> in Esper, associating it with an Id.");
        System.out.println("send (meterId,timestamp,measure);\n\tSend the event (deviceId,timestamp,measure) into Esper query engine.");
        System.out.println("list;\n\t List the queries installed in Esper.");
        System.out.println("turnOn query_id;\n\t Turn ON the query associated with the id <query_id>.");
        System.out.println("turnOff query_id;\n\t Turn OFF the query associated with the id <query_id>.");
        System.out.println("drop query_id;\n\t Remove from Esper the query associated with the id <query_id>.");
        System.out.println("dropAll query_id;\n\t Remove all the queries previously installed in Esper.");
        System.out.println("printToFile query_id;\n\t Set the query related with <query_id> to print their output into file <query_id>output.txt");
        System.out.println("dontPrintToFile query_id;\n\t Set the query related with <query_id> to Do Not print their output into file <query_id>output.txt");
        System.out.println("printToTerminal query_id;\n\t Set the query related with <query_id> to print their output to this terminal.");
        System.out.println("dontPrintToFile query_id;\n\t Set the query related with <query_id> to Do Not print their output to this terminal.");
        System.out.println("showInput;\n\t Print in terminal all events that are being sent by the sensors.");
        System.out.println("dontShowInput;\n\t Don't print in terminal the events that are being sent by the sensors.");
        System.out.println("runScript <file_name.script>;\n\t Run the script file with name <file_name.script>.");
        System.out.println("----------------------------------------------\n");        
        
    }
}
