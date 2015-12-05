//Token class that contains each pair of token string and token type
public class Token
{
	private String token;
	private TokenType type;
	
	public Token(String token, TokenType type)
	{
		this.token = token;
		this.type = type;
	}
	
	public TokenType GetTokenType()
	{
		return type;
	}
	
	public String GetTokenName()
	{
		return token;
	}
}