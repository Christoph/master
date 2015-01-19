package mining;

public class NoTagsException extends RuntimeException{
	public NoTagsException() {}
	
	public NoTagsException(String message)
	{
		super(message);
	}
}
