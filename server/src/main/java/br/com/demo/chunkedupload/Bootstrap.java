package br.com.demo.chunkedupload;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;

public class Bootstrap extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
	Info info = new Info().title("File management API");

	// ServletContext context = config.getServletContext();
	Swagger swagger = new Swagger().info(info);
	swagger.externalDocs(
		new ExternalDocs("Source code", "https://github.com/edsoncunha/chunked-file-upload-sample"));

	swagger.tag(new Tag().name("file").description("File upload management"));

	new SwaggerContextService().withServletConfig(config).updateSwagger(swagger);
    }
}
