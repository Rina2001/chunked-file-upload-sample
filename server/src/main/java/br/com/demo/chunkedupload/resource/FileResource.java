package br.com.demo.chunkedupload.resource;

import static br.com.demo.chunkedupload.model.ApiResponse.ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.jersey.multipart.FormDataParam;

import br.com.demo.chunkedupload.data.Session;
import br.com.demo.chunkedupload.data.SessionStore;
import br.com.demo.chunkedupload.exception.SampleExceptionMapper;
import br.com.demo.chunkedupload.exception.SessionAlreadyBeingCreatedException;
import br.com.demo.chunkedupload.model.SessionCreationStatusResponse;
import br.com.demo.chunkedupload.model.UploadStatusResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("/file")
@Api(value = "/file", tags = "file")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Component
public class FileResource {
    private Logger LOG = LoggerFactory.getLogger(FileResource.class);

    @Autowired
    private SessionStore sessionStore;

    @POST
    @Path("/bolas")
    @ApiOperation(value = "this is fake")
    public Response bolas() {
	return Response.ok().build();
    }

    @POST
    @Path("/upload/{userId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "creates an upload session")
    @ApiResponses(value = {
	    @ApiResponse(code = 200, message = "Session started successfully", response = SessionCreationStatusResponse.class),
	    @ApiResponse(code = 500, message = "Internal server error") })
    public Response startSession(@ApiParam(value = "ID of user", required = true) @PathParam("userId") Long userId,
	    @ApiParam(value = "Chunk size in bytes", required = true) @FormParam("chunkSize") int chunkSize,
	    @ApiParam(value = "Total file size in bytes", required = true) @FormParam("totalSize") Long totalSize,
	    @ApiParam(value = "File name") @FormParam("fileName") String fileName) {
	try {

	    Session session = sessionStore.createSession(userId, fileName, chunkSize, totalSize);

	    LOG.debug(String.format("Session started for user {0} and file {1}. Session id: {2}", userId, fileName,
		    session.getId()));

	    return Response.status(200).entity(SessionCreationStatusResponse.fromSession(session)).build();
	} catch (SessionAlreadyBeingCreatedException ce) {
	    return Response.status(202).build();
	} catch (Exception e) {
	    return new SampleExceptionMapper().toResponse(e);
	}
    }

    /// upload/user/11/session/1fbb108c-517c-4562-abf9-d56e40024066?chunkNumber=1&chunkSize=184042&totalSize=184042&resumableType=text%2Fx-log&resumableIdentifier=184042-java_error_in_STUDIO_5038log&fileName=java_error_in_STUDIO_5038.log&resumableRelativePath=java_error_in_STUDIO_5038.log&resumableTotalChunks=1
    @PUT
    @Path("/upload/user/{userId}/session/{sessionId}/")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "uploads a file chunk")
    @ApiResponses(value = {
	    @ApiResponse(code = 200, message = "Chunk uploaded successfully", response = UploadStatusResponse.class),
	    @ApiResponse(code = 202, message = "Server busy during that particular upload. Try again."),
	    @ApiResponse(code = 410, message = "Session expired"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    public Response uploadFileChunk(@ApiParam(value = "ID of user", required = true) @PathParam("userId") Long userId,
	    @ApiParam(value = "Session id", required = true) @PathParam("sessionId") String sessionId,
	    @ApiParam(value = "Chunk number (starts from 1)", required = true) @QueryParam("chunkNumber") int chunkNumber,
	    @ApiParam(value = "file content to upload") @FormDataParam("file") InputStream inputStream) {
	try {
	    Session session = sessionStore.getSession(sessionId);

	    if (session == null) {
		return Response.status(404)
			.entity(new br.com.demo.chunkedupload.model.ApiResponse(ERROR, "Session not found")).build();
	    }

	    if (session.isExpired()) {
		return Response.status(410)
			.entity(new br.com.demo.chunkedupload.model.ApiResponse(ERROR, "Session expired")).build();
	    }

	    if (session.isConcluded()) {
		return Response.status(200).build();
	    }

	    // OK, let's persist the chunks till the end...
	    session.persistBlock(chunkNumber, IOUtils.toByteArray(inputStream));

	    LOG.debug(String.format("Chunk number {0} saved. Session id: {1}", chunkNumber, session.getId()));

	    return Response.status(200).build();
	} catch (Exception e) {
	    return new SampleExceptionMapper().toResponse(e);
	}
    }

    @GET
    @Path("/upload/{userId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UploadStatusResponse.class),
	    @ApiResponse(code = 404, message = "Not found"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    @ApiOperation(value = "gets the status of a single upload", response = UploadStatusResponse.class)
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

    @OPTIONS
    @Path("/upload/user/{userId}/session/{sessionId}/")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getUploadFileChunkOptions(@PathParam("userId") String userId,
	    @PathParam("sessionId") String sessionId) {
	return Response.ok().header("Allow", "OPTIONS").header("Allow", "PUT").build();
    }

    @GET
    @Path("/uploads")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses(value = {
	    @ApiResponse(code = 200, message = "OK", response = UploadStatusResponse.class, responseContainer = "List"),
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

    @GET
    @Path("/download/{sessionId}")
    @Produces(MediaType.MULTIPART_FORM_DATA)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"), @ApiResponse(code = 404, message = "Not found"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    @ApiOperation(value = "downloads a previously uploaded file")
    public Response downloadFile(
	    @ApiParam(value = "File ID", required = true) @PathParam("sessionId") String sessionId) {

	final Session session = sessionStore.getSession(sessionId);

	if (session == null) {
	    return Response.status(404).build();
	}

	StreamingOutput stream = new StreamingOutput() {
	    @Override
	    public void write(OutputStream out) throws IOException, WebApplicationException {

		for (int i = 1; i <= session.getTotalNumberOfChunks(); i++) {
		    out.write(session.getChunkContent(i));
		}

		out.flush();

	    }
	};
	return Response.ok(stream, MediaType.MULTIPART_FORM_DATA).header("Content-Length", session.getFileSize())
		.header("Content-Disposition", "attachment; filename=\"" + session.getFileName() + "\"").build();
    }
}