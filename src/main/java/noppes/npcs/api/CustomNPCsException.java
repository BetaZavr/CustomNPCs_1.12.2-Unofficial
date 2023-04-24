package noppes.npcs.api;

public class CustomNPCsException
extends RuntimeException {
	
	private static final long serialVersionUID = 3L;

	public CustomNPCsException(Exception ex, String message, Object... obs) {
		super(String.format(message, obs), ex);
	}

	public CustomNPCsException(String message, Object... obs) {
		super(String.format(message, obs));
	}
	
}
