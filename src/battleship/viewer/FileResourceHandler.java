package battleship.viewer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.*;


public class FileResourceHandler implements HttpHandler {
	private final String resourcePath;
	private final String mimeType;
	
	public FileResourceHandler(String resourcePath, String mimeType) {
		this.resourcePath = resourcePath;
		this.mimeType = mimeType;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
        //System.out.println("@FileResourceHandler(" + resourcePath + "): " + exchange.getRequestURI());
        
        Headers responseHeader = exchange.getResponseHeaders();
        responseHeader.set("Content-Type", this.mimeType);
        
        InputStream is = exchange.getRequestBody();
        String request = new String(is.readAllBytes());
        System.out.println(request);
                
        try(
    		OutputStream os = exchange.getResponseBody();
    		FileInputStream fis = new FileInputStream(resourcePath);
        ) {
        	
        	byte[] site = fis.readAllBytes();
        	exchange.sendResponseHeaders(200, site.length);
        	os.write(site);

        }
		
	}
}
