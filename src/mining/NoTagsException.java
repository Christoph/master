package mining;

public class NoTagsException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = -342110127438217741L;

	public NoTagsException() {}
	
	public NoTagsException(String message)
	{
		super(message);
	}
}
