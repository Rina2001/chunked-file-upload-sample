package br.com.demo.chunkedupload.resource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import br.com.demo.chunkedupload.data.Session;
import br.com.demo.chunkedupload.data.SessionStore;
import br.com.demo.chunkedupload.exception.InvalidOperationException;
import br.com.demo.chunkedupload.model.UploadStatusResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/file")
@Api(value = "/file", tags = "file")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class FileResource {
    private Logger LOG = LoggerFactory.getLogger(FileResource.class);
    private static SessionStore sessionStore;

    static {
	sessionStore = new SessionStore();
    }

    @POST
    @Path("/upload/{userId}")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "uploads a file")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Chunk uploaded successfully"),
	    @ApiResponse(code = 410, message = "Session expired"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    public Response uploadFile(@ApiParam(value = "ID of user", required = true) @PathParam("userId") Long userId,
	    @ApiParam(value = "Chunk number (starts from 1)", required = true) @QueryParam("chunkNumber") int chunkNumber,
	    @ApiParam(value = "Chunk size in bytes", required = true) @QueryParam("chunkSize") int chunkSize,
	    @ApiParam(value = "Total file size", required = true) @QueryParam("totalSize") Long totalSize,
	    @ApiParam(value = "file to upload") @FormDataParam("file") InputStream inputStream,
	    @ApiParam(value = "file detail") @FormDataParam("file") FormDataContentDisposition fileDetail) {
	try {
	    Session session = getOrCreateSession(userId, chunkSize, totalSize, fileDetail.getFileName());

	    // may happen when the session is still being created.
	    // I chose to do this and tell the client to send the package again
	    // instead of adding a busy wait on the server side.
	    if (session == null) {
		return Response.status(202).build();
	    }

	    if (session.isExpired()) {
		return Response.status(410).build();
	    }

	    if (session.isConcluded()) {
		return Response.status(200).build();
	    }

	    // OK, let's persist the chunks till the end...
	    session.persistBlock(chunkNumber, IOUtils.toByteArray(inputStream));

	    LOG.debug("File " + fileDetail.getFileName() + ", block " + chunkNumber + " saved successfully");

	    return Response.status(200).build();
	} catch (Exception e) {
	    return new SampleExceptionMapper().toResponse(e);
	}
    }

    @GET
    @Path("/upload/{userId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    @ApiOperation(value = "gets the status of a single upload", response = br.com.demo.chunkedupload.model.UploadStatusResponse.class)
    public Response getUploadStatus(@ApiParam(value = "ID of user", required = true) @PathParam("userId") Long userId,
	    @QueryParam("chunkNumber") int chunkNumber, @QueryParam("totalSize") Long totalSize,
	    @QueryParam("fileName") String fileName) {
	try {
	    Session session = sessionStore.getSession(userId, fileName);

	    if (session != null && session.isBlockPersisted(chunkNumber)) {
		return Response.status(200).entity(UploadStatusResponse.fromSession(session)).build();
	    }

	    return Response.status(404).build();
	} catch (Exception e) {
	    return Response.status(500).build();
	}
    }

    @GET
    @Path("/uploads")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    @ApiOperation(value = "gets the status of upload sessions", response = UploadStatusResponse.class, responseContainer = "List")
    public Response listUploadsStatus() {
	try {
	    List<UploadStatusResponse> sessions = UploadStatusResponse.fromSessionList(sessionStore.getAllSessions());
	    return Response.status(200).entity(sessions).build();
	} catch (Exception e) {
	    return Response.status(500).build();
	}
    }

    // @GET
    // @Path("/download/{fileId}")
    // // @Produces(MediaType.APPLICATION_OCTET_STREAM)
    // @ApiOperation(value = "downloads a previously uploaded file")
    // public StreamingOutput downloadFile(
    // @ApiParam(value = "File ID", required = true) @PathParam("fileId") String
    // fileId, OutputStream output)
    // throws Exception {
    // return new StreamingOutput() {
    // @Override
    // public void write(OutputStream arg0) throws IOException,
    // WebApplicationException {
    // BufferedOutputStream bus = new BufferedOutputStream(arg0);
    // try {
    // // ByteArrayInputStream reader = (ByteArrayInputStream)
    // // Thread.currentThread().getContextClassLoader().getResourceAsStream();
    // // byte[] input = new byte[2048];
    // java.net.URL uri =
    // Thread.currentThread().getContextClassLoader().getResource("");
    // File file = new File("D:\\Test1.zip");
    // FileInputStream fizip = new FileInputStream(file);
    // byte[] buffer2 = IOUtils.toByteArray(fizip);
    // bus.write(buffer2);
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // };
    // }

    @GET
    @Path("/download/{sessionId}")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    @ApiOperation(value = "downloads a previously uploaded file")
    public Response downloadFile(
	    @ApiParam(value = "File ID", required = true) @PathParam("sessionId") String sessionId) {

	Session session = sessionStore.getSession(sessionId);

	if (session == null) {
	    return Response.status(404).build();
	}

	StreamingOutput stream = new StreamingOutput() {
	    @Override
	    public void write(OutputStream out) throws IOException, WebApplicationException {
		Writer writer = new BufferedWriter(new OutputStreamWriter(out));

		// chunk numbers under this API start from 1.
		for (int i = 1; i <= session.getTotalNumberOfChunks(); i++) {
		    IOUtils.write(session.getChunkContent(i), writer);
		}

		writer.flush();
	    }
	};
	return Response.ok(stream)
		.header("Content-Disposition", "attachment; filename=\"" + session.getFileName() + "\"").build();
    }

    private Session getOrCreateSession(Long userId, int chunkSize, Long totalSize, String fileName)
	    throws InvalidOperationException {
	Session session = sessionStore.getSession(userId, fileName);

	if (session == null) {
	    session = sessionStore.createSession(userId, fileName, chunkSize, totalSize);
	}
	return session;
    }
}
