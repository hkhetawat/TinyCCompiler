import java.io.FileReader;
import java.util.LinkedList;
import java.io.IOException;
import java.io.FileNotFoundException;

public class Scanner
{
	private FileReader inputFile;
	private StringBuilder token;
	private char nextChar;
	private LinkedList<String> symbolList;
	private LinkedList<String> reservedList;
	
	private void prepareSymbolList()
	{
		//Creating a list of all the possible/legal symbols
		symbolList = new LinkedList<String>();
		symbolList.add("(");
		symbolList.add(")");
		symbolList.add("{");
		symbolList.add("}");
		symbolList.add("[");
		symbolList.add("]");
		symbolList.add(",");
		symbolList.add(";");
		symbolList.add("+");
		symbolList.add("-");
		symbolList.add("*");
		symbolList.add("/");
		symbolList.add("==");
		symbolList.add("!=");
		symbolList.add(">");
		symbolList.add(">=");
		symbolList.add("<");
		symbolList.add("<=");
		symbolList.add("=");
		symbolList.add("&&");
		symbolList.add("||");
		symbolList.add("|");
		symbolList.add("&");
	}
	
	private void prepareReservedList()
	{
		//Creating a list of all reserved words
		reservedList = new LinkedList<String>();
		reservedList.add("int");
		reservedList.add("void");
		reservedList.add("if");
		reservedList.add("while");
		reservedList.add("return");
		reservedList.add("read");
		reservedList.add("write");
		reservedList.add("print");
		reservedList.add("continue");
		reservedList.add("break");
		reservedList.add("binary");
		reservedList.add("decimal");
	}
	
	public Scanner(String sourceFile) throws Exception
	{
		//Initiates the scanner by creating symbol list, reserved word list and opening the input file
		prepareSymbolList();
		prepareReservedList();
		inputFile = new FileReader(sourceFile);
		nextChar = (char)inputFile.read();
	}
	
	public boolean HasMoreTokens()
	{
		if((int)nextChar == 65535)
		{
			return false;
		}
		else return true;
	}
	
	public Token GetNextToken() throws Exception
	{
		try
		{
			token = new StringBuilder();
			while(Character.isWhitespace(nextChar))
				nextChar = (char)inputFile.read();
			if((int)nextChar == 65535)
				return new Token("", TokenType.EOF);
			
			//Checking for meta statements
			while(nextChar == '#')
			{
				token.append(nextChar);
				nextChar = (char)inputFile.read();
				while(nextChar != '\n' && nextChar != -1)
				{
					token.append(nextChar);
					nextChar = (char)inputFile.read();
				}
				return new Token(token.toString(), TokenType.META);
			}
			
			switch(nextChar)
			{
				//Checking for symbols
				case '=':
				case '<':
				case '>':
				case '!':
				    token.append(nextChar);
					nextChar = (char)inputFile.read();
					if(nextChar == '=' || nextChar == '<' || nextChar == '>' || nextChar == '!')
					{
						token.append(nextChar);
						nextChar = (char)inputFile.read();
					}
					if(!symbolList.contains(token.toString()))
					{
						return new Token(token.toString(), TokenType.ERROR);
					}
					return new Token(token.toString(), TokenType.SYMBOL);
				case '&':
				case '|':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					if(nextChar == '&' || nextChar == '|')
					{
						token.append(nextChar);
						nextChar = (char)inputFile.read();
					}
					if(!symbolList.contains(token.toString()))
					{
						return new Token(token.toString(), TokenType.ERROR);
					}
					return new Token(token.toString(), TokenType.SYMBOL);
				case '(':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case ')':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case '{':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case '}':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case '[':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case ']':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case ',':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case ';':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case '+':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case '-':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case '*':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.SYMBOL);
				case '/':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					if(nextChar == '/')
					{
						token.append(nextChar);
						nextChar=(char)inputFile.read();
						while(nextChar != '\n' && (int)nextChar != 65535)
						{
							token.append(nextChar);
							nextChar = (char)inputFile.read();
						}
						nextChar = (char)inputFile.read();
						return new Token(token.toString(), TokenType.META);
					}
					return new Token(token.toString(), TokenType.SYMBOL);
				//Checking for strings
				case '"':
					token.append(nextChar);
					nextChar = (char)inputFile.read();
					while(nextChar != '"')
					{
						if(nextChar == -1)
						{
							return new Token(token.toString(), TokenType.ERROR);
						}
						token.append(nextChar);
						nextChar = (char)inputFile.read();
					}
					token.append('"');
					nextChar = (char)inputFile.read();
					return new Token(token.toString(), TokenType.STRING);
			}
			//Checking for numbers
			if(Character.isDigit(nextChar))
			{
				token.append(nextChar);
				nextChar = (char)inputFile.read();
				while(Character.isDigit(nextChar))
				{
					token.append(nextChar);
					nextChar = (char)inputFile.read();
				}
				return new Token(token.toString(), TokenType.NUMBER);
			}
			//Checking for identifiers and reserved words
			if(Character.isLetter(nextChar) || nextChar == '_')
			{
				token.append(nextChar);
				nextChar = (char)inputFile.read();
				while(Character.isLetter(nextChar) || Character.isDigit(nextChar) || nextChar == '_')
				{
					token.append(nextChar);
					nextChar = (char)inputFile.read();
				}
				if(reservedList.contains(token.toString()))
				{
					return new Token(token.toString(), TokenType.RESERVED);
				}
				return new Token(token.toString(), TokenType.IDENTIFIER);
			}
			token.append(nextChar);
			nextChar = (char)inputFile.read();
			return new Token(token.toString(), TokenType.ERROR);
		}
		catch(IOException e)
		{
			throw e;
		}
	}
}