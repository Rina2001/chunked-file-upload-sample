package br.com.demo.chunkedupload.resource;

import static br.com.demo.chunkedupload.model.ApiResponse.ERROR;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class FileResource {
    private static final int CHUNK_LIMIT = 1024 * 1024;

    private Logger LOG = LoggerFactory.getLogger(FileResource.class);

    private static SessionStore sessionStore;

    static {
	sessionStore = new SessionStore();
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

	    if (StringUtils.isEmpty(fileName))
		return badRequest("File name missing");

	    if (userId == null)
		return badRequest("User ID missing");

	    if (chunkSize > CHUNK_LIMIT)
		return badRequest(String.format("Maximum chunk size is {} bytes", CHUNK_LIMIT));

	    if (chunkSize < 1)
		return badRequest("Chunk size must be greater than zero");

	    if (chunkSize < 1)
		return badRequest("Total size must be greater than zero");

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

    private Response badRequest(String message) {
	return Response.status(400).entity(new br.com.demo.chunkedupload.model.ApiResponse(ERROR, message)).build();
    }

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
	Session session = null;

	try {

	    if (userId == null)
		return badRequest("User missing");

	    if (StringUtils.isEmpty(sessionId))
		return badRequest("Session ID is missing");

	    if (chunkNumber < 1)
		return badRequest("Invalid chunk number");

	    session = sessionStore.getSession(sessionId);

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

	    session.persistBlock(chunkNumber, IOUtils.toByteArray(inputStream));

	    LOG.debug(String.format("Chunk number {0} saved. Session id: {1}", chunkNumber, session.getId()));

	    return Response.status(200).build();
	} catch (Exception e) {
	    if (session != null)
		session.maskAsFailed();

	    return new SampleExceptionMapper().toResponse(e);
	}
    }

    @GET
    @Path("/upload/{sessionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = UploadStatusResponse.class),
	    @ApiResponse(code = 404, message = "Not found"),
	    @ApiResponse(code = 500, message = "Internal server error") })
    @ApiOperation(value = "gets the status of a single upload", response = UploadStatusResponse.class)
    public Response getUploadStatus(
	    @ApiParam(value = "Session ID", required = true) @PathParam("sessionId") String sessionId) {
	try {
	    Session session = sessionStore.getSession(sessionId);

	    if (session == null) {
		return Response.status(404).build();
	    }

	    return Response.status(200).entity(UploadStatusResponse.fromSession(session)).build();

	} catch (Exception e) {
	    return new SampleExceptionMapper().toResponse(e);
	}
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

	return Response.ok(session.getContentStream(), MediaType.MULTIPART_FORM_DATA)
		.header("Content-Length", session.getFileSize())
		.header("Content-Disposition", "attachment; filename=\"" + session.getFileName() + "\"").build();
    }
}
