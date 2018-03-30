package br.com.demo.chunkedupload.exception;

@SuppressWarnings("serial")
public class InvalidOperationException extends ApiException {
    public InvalidOperationException(String msg) {
	super(401, msg);
    }
}
