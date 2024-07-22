package io.github.defectly;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;

public class Server {
    private final Gson Json = new Gson();
    private final HttpServer Server;
    private static final SecureRandom random = new SecureRandom();
    public List<User> Users = new ArrayList<>();
    public UserToken UserToken = new UserToken();
    public List<Message> Chat = new ArrayList<>();

    public Consumer<String> onNewMessage;


    public Server(String hostname, int port) throws IOException {
        Server = HttpServer.create(new InetSocketAddress(hostname, port), 0);

//        Users.add(new User("defectly", "howtokillmyself"));
    }

    public void start() {
        Server.createContext("/users", this::getUsers);
        Server.createContext("/register", this::register);
        Server.createContext("/login", this::login);
        Server.createContext("/chat", this::getChat);
        Server.createContext("/message", this::sendMessage);
        Server.start();

        Chat.add(new Message("defectly", "meow"));
        Chat.add(new Message("defectly", "woof"));
        Chat.add(new Message("defectly", "bark"));

        var message = new Message("System", "Сервер запущен");

        Chat.add(message);

        if (onNewMessage != null)
            onNewMessage.accept(message.Username + ": " + message.Content);

    }

    public void stop() {
        var message = new Message("System", "Сервер остановлен");
        Chat.add(message);

        if (onNewMessage != null)
            onNewMessage.accept(message.Username + ": " + message.Content);

        Server.stop(0);
    }

    private void sendMessage(HttpExchange exchange) throws IOException {
        var output = exchange.getResponseBody();
        var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);


        var authorizedUserMessage = Json.fromJson(requestBody, AuthorizedUserMessage.class);

        if(Users.stream().noneMatch(u -> Objects.equals(u.Username, authorizedUserMessage.Username))) {
            var response = "User doesn't exists";
            exchange.sendResponseHeaders(409, response.length());
            output.write(response.getBytes());
            return;
        }

        var storedUser = Users.stream().filter(u -> Objects.equals(u.Username, authorizedUserMessage.Username)).findFirst().get();

        if(UserToken.Content.get(storedUser).stream().noneMatch(td -> Objects.equals(td.Token, authorizedUserMessage.Token))) {
            var response = "no such token";
            exchange.sendResponseHeaders(409, response.length());
            output.write(response.getBytes());
            return;
        }

        Chat.add(new Message(authorizedUserMessage.Username, authorizedUserMessage.Message));
        exchange.sendResponseHeaders(204, -1);


        if (onNewMessage != null)
            onNewMessage.accept(authorizedUserMessage.Username + ": " + authorizedUserMessage.Message);

    }

    private void getChat(HttpExchange exchange) throws IOException {
        var output = exchange.getResponseBody();
        var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        var authorizedUser = Json.fromJson(requestBody, AuthorizedUser.class);


        if(Users.stream().noneMatch(u -> Objects.equals(u.Username, authorizedUser.Username))) {
            var response = "User doesn't exists";
            exchange.sendResponseHeaders(409, response.length());
            output.write(response.getBytes());
            return;
        }

        var storedUser = Users.stream().filter(u -> Objects.equals(u.Username, authorizedUser.Username)).findFirst().get();

        if(UserToken.Content.get(storedUser).stream().noneMatch(td -> Objects.equals(td.Token, authorizedUser.Token))) {
            var response = "no such token";
            exchange.sendResponseHeaders(409, response.length());
            output.write(response.getBytes());
            return;
        }

        String response = "";

        try {
            response = Json.toJson(Chat);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }

//        var response = Json.toJson(Chat);

        exchange.sendResponseHeaders(200, response.getBytes().length);
        try {
            output.write(response.getBytes());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void login(HttpExchange exchange) throws IOException {
        var output = exchange.getResponseBody();
        var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);


        var user = Json.fromJson(requestBody, User.class);

        if(Users.stream().noneMatch(u -> Objects.equals(u.Username, user.Username))) {
            var response = "User doesn't exists";
            exchange.sendResponseHeaders(409, response.length());
            output.write(response.getBytes());
            return;
        }

        var meow = Users.stream().filter(u -> Objects.equals(u.Username, user.Username)).findFirst();
        var storedUser = meow.get();

        if(!Objects.equals(storedUser.Password, user.Password)) {
            var response = "Wrong password";
            exchange.sendResponseHeaders(409, response.length());
            output.write(response.getBytes());
            return;
        }

        var token = generateToken();

        try {
            if(!UserToken.Content.containsKey(storedUser))
                UserToken.Content.put(storedUser, new ArrayList<>());

            UserToken.Content.get(storedUser).add(new TokenDate(token, LocalDate.now(ZoneId.of("Europe/Moscow")).toString()));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }


        exchange.sendResponseHeaders(200, token.length());
        output.write(token.getBytes());
        return;
    }

    private String generateToken() {
        var bytes = new byte[128];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private void getUsers(HttpExchange exchange) throws IOException {
            var response = Json.toJson(Users);
            var output = exchange.getResponseBody();

            exchange.sendResponseHeaders(200, response.length());
            output.write(response.getBytes());
    }

    private void register(HttpExchange exchange) throws IOException {
            var requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            var output = exchange.getResponseBody();
            User user;
            try {
                user = Json.fromJson(requestBody, User.class);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                return;
            }

            if(Users.stream().anyMatch(u -> Objects.equals(u.Username, user.Username))) {
                var response = "User already registered";
                exchange.sendResponseHeaders(409, response.length());
                output.write(response.getBytes());
                return;
            }

            Users.add(user);

            exchange.sendResponseHeaders(204, -1);
    }
}
