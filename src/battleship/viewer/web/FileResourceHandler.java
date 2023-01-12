package battleship.viewer.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.*;


public class FileResourceHandler implements HttpHandler {
	private String resourcePath;
	private byte[] data;
	private final String mimeType;
	private final String webPath;
	
	public FileResourceHandler(HttpServer server, String webPath, String resourcePath, String mimeType) 
			throws FileNotFoundException, IOException {
		
		this.resourcePath = resourcePath;
		
		this.mimeType = mimeType;
		
		this.webPath = webPath;
		server.createContext(this.webPath, this);
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if(!exchange.getRequestURI().getPath().equals(this.webPath)) return;
		
		try(
    		FileInputStream fis = new FileInputStream(resourcePath);
        ) {
			// TODO: move to constructor
			this.data = fis.readAllBytes();
        }
		
		exchange.getResponseHeaders().set("Content-Type", this.mimeType);
		exchange.sendResponseHeaders(200, data.length);
		try(OutputStream os = exchange.getResponseBody()) {
			os.write(data);
		}
	}
}
