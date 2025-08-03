package sugars.drums;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class WebServer {
    private static final int PORT = 8080;
    private static final String FILES_DIR = "C:\\private\\IdeaProjects\\sugarsdrums.github.io";
    private HttpServer server;

    public WebServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new FileHandler());
        server.setExecutor(Executors.newFixedThreadPool(10));
    }

    public void start() {
        server.start();
        System.out.println("Server started on port " + PORT);
        System.out.println("Visit http://localhost:" + PORT + " in your browser");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Server stopped");
    }

    private static class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            
            // Default to index.html if root path is requested
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }
            
            // Remove leading slash
            if (requestPath.startsWith("/")) {
                requestPath = requestPath.substring(1);
            }
            
            Path filePath = Paths.get(FILES_DIR, requestPath);
            File file = filePath.toFile();
            
            if (!file.exists() || file.isDirectory()) {
                // If file doesn't exist, try to serve a default file from the files directory
                File filesDir = new File(FILES_DIR);
                File[] htmlFiles = filesDir.listFiles((dir, name) -> name.endsWith(".html"));
                
                if (htmlFiles != null && htmlFiles.length > 0) {
                    // Serve the first HTML file found
                    file = htmlFiles[0];
                    filePath = file.toPath();
                } else {
                    // If no HTML files found, return 404
                    String response = "404 Not Found";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                    return;
                }
            }
            
            // Determine content type based on file extension
            String contentType = getContentType(filePath.toString());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            
            // Read file and send response
            byte[] fileContent = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, fileContent.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileContent);
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) {
                return "text/html; charset=UTF-8";
            } else if (path.endsWith(".css")) {
                return "text/css";
            } else if (path.endsWith(".js")) {
                return "application/javascript";
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (path.endsWith(".png")) {
                return "image/png";
            } else if (path.endsWith(".gif")) {
                return "image/gif";
            } else {
                return "application/octet-stream";
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            WebServer webServer = new WebServer();
            webServer.start();
            
            // Add shutdown hook to stop server when program exits
            Runtime.getRuntime().addShutdownHook(new Thread(webServer::stop));
            
            System.out.println("Press Ctrl+C to stop the server");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
