package datastorm.espershell;
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
        ModbusDriver modbusDriver = new ModbusDriver();
        
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        String command = "";
        System.out.print(">");


        while (true) {
            try {
                line = br.readLine();
                command = command + line;
                if (command.contains(";")) {
                    if (command.equals("exit;")) {
                        return;
                    }
                    dispatchCommand(command, esper);
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

    private static void dispatchCommand(String command, EsperEngine esper) {
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

}
