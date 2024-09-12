package org.example;

import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.*;

public class ReflexCalculator {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9090);  // Puerto para el servicio
        System.out.println("Calculadora lista en el puerto 9090...");

        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                handleClientRequest(clientSocket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleClientRequest(Socket clientSocket) throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        BufferedWriter output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        String request = input.readLine();
        String response = processRequest(request);

        output.write("HTTP/1.1 200 OK\r\n");
        output.write("Content-Type: application/json\r\n\r\n");
        output.write(response);
        output.flush();
    }

    private static String processRequest(String request) {
        // Parsear el comando y los parámetros del request
        String[] parts = request.split(" ")[1].split("\\?");
        if (parts.length < 2) return "{\"error\": \"Invalid command\"}";

        String query = parts[1].split("=")[1];
        String[] commandParts = query.split("\\(");
        String command = commandParts[0];
        String[] params = commandParts[1].replace(")", "").split(",");

        try {
            if (command.equals("bbl")) {
                return bubbleSort(params);
            } else {
                return invokeMathMethod(command, params);
            }
        } catch (Exception e) {
            return "{\"error\": \"Invalid operation\"}";
        }
    }

    private static String bubbleSort(String[] params) {
        double[] numbers = Arrays.stream(params).mapToDouble(Double::parseDouble).toArray();
        // Bubble Sort Algorithm
        for (int i = 0; i < numbers.length - 1; i++) {
            for (int j = 0; j < numbers.length - i - 1; j++) {
                if (numbers[j] > numbers[j + 1]) {
                    double temp = numbers[j];
                    numbers[j] = numbers[j + 1];
                    numbers[j + 1] = temp;
                }
            }
        }
        return Arrays.toString(numbers);
    }

    private static String invokeMathMethod(String methodName, String[] params) throws Exception {
        Method method = Math.class.getMethod(methodName, double.class);
        double param = Double.parseDouble(params[0]);
        double result = (double) method.invoke(null, param);
        return "{\"result\": " + result + "}";
    }
}