package org.example;

import java.io.*;
import java.net.*;

public class ServiceFacade {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Fachada lista en el puerto 8080...");

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
        if (request == null || request.isEmpty()) {
            output.write("HTTP/1.1 400 Bad Request\r\n");
            output.write("Content-Type: application/json\r\n\r\n");
            output.write("{\"error\": \"Empty request\"}");
            output.flush();
            return;
        }

        if (request.contains("/calculadora")) {
            serveClientPage(output);
        } else if (request.contains("/computar")) {
            String query = request.split(" ")[1].split("\\?")[1];
            String response = delegateToCalculator(query);

            output.write("HTTP/1.1 200 OK\r\n");
            output.write("Content-Type: application/json\r\n\r\n");
            output.write(response);
        } else {
            output.write("HTTP/1.1 404 Not Found\r\n");
        }
        output.flush();
    }

    private static void serveClientPage(BufferedWriter output) throws IOException {
        String html = """
        <html>
        <head><title>Calculadora</title></head>
        <body>
            <h1>Calculadora Web</h1>
            <input type="text" id="command" placeholder="Escribe comando">
            <button onclick="compute()">Calcular</button>
            <p id="result"></p>
            <script>
            function compute() {
                var command = document.getElementById("command").value;
                fetch('/computar?comando=' + command)
                .then(response => response.json())
                .then(data => document.getElementById("result").innerText = data.result)
                .catch(error => console.error('Error:', error));
            }
            </script>
        </body>
        </html>
        """;
        output.write("HTTP/1.1 200 OK\r\n");
        output.write("Content-Type: text/html\r\n\r\n");
        output.write(html);
    }

    private static String delegateToCalculator(String query) {
        try (Socket calcSocket = new Socket("localhost", 9090)) {
            BufferedWriter calcOutput = new BufferedWriter(new OutputStreamWriter(calcSocket.getOutputStream()));
            BufferedReader calcInput = new BufferedReader(new InputStreamReader(calcSocket.getInputStream()));

            calcOutput.write("GET /compreflex?" + query + " HTTP/1.1\r\n\r\n");
            calcOutput.flush();

            String response = calcInput.readLine();
            return response;
        } catch (IOException e) {
            return "{\"error\": \"Failed to connect to calculator\"}";
        }
    }
}