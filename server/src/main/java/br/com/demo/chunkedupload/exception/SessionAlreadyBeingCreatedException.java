package br.com.demo.chunkedupload.exception;

@SuppressWarnings("serial")
public class SessionAlreadyBeingCreatedException extends ApiException {
    public SessionAlreadyBeingCreatedException(String msg) {
	super(202, msg);
    }
}
