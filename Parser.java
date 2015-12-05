import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Stack;

//This class contains the implementation of the recursive descent parser. Each function is representative of a non-terminal.
public class Parser
{
	public boolean status;
	public Token currentToken;
	public int variableCount, functionCount, statementCount;
	Scanner scanner;
	PrintWriter newFile;
	HashMap<String, Integer> localMap;
	int localCount;
	HashMap<String, Integer> globalMap;
	String currrentGlobalID;
	int globalCount;
	String localFunction;
	boolean declareFlag, inFunction, globalFlag;
	int labelCount, startLabel, endLabel, condLabel;

	//Function to evaluate an infix expression to the two operand mode.
	public String evaluate(ArrayList<String> expression)
    {
        Stack<String> Values = new Stack<String>(), Operations = new Stack<String>();

        for (int i = 0; i < expression.size(); i++)
		{
			if (expression.get(i).equals("("))
                Operations.push(expression.get(i));
            else if (expression.get(i).equals(")"))
            {
                while (Operations.peek().equals("("))
                  Values.push(apply(Operations.pop(), Values.pop(), Values.pop()));
                Operations.pop();
            }
            else if (expression.get(i).equals("+") || expression.get(i).equals("-") || expression.get(i).equals("*") || expression.get(i).equals("/"))
            {
                while (!Operations.empty() && precedence(expression.get(i), Operations.peek()))
                  Values.push(apply(Operations.pop(), Values.pop(), Values.pop()));
                Operations.push(expression.get(i));
            }
			else
			{
				Values.push(expression.get(i));
			}
        }
        while (!Operations.empty())
            Values.push(apply(Operations.pop(), Values.pop(), Values.pop()));
        return Values.pop();
    }

    public boolean precedence(String o1, String o2)
    {
        if (o2.equals("(") || o2.equals(")"))
            return false;
        if ((o1.equals("*") || o1.equals("/")) && (o2.equals("+") || o2.equals("-")))
            return false;
        else
            return true;
    }

    public String apply(String o, String b, String a)
    {
        switch (o)
        {
        case "+":
            localFunction += "local[" + localCount + "] = " + a + " + " + b +";\n";
			return "local[" + localCount++ + "]";
        case "-":
            localFunction += "local[" + localCount + "] = " + a + " - " + b +";\n";
			return "local[" + localCount++ + "]";
        case "*":
        	localFunction += "local[" + localCount + "] = " + a + " * " + b +";\n";
			return "local[" + localCount++ + "]";
        case "/":
        	localFunction += "local[" + localCount + "] = " + a + " / " + b +";\n";
			return "local[" + localCount++ + "]";
        }
        return null;
    }


	//This function gets the next token from the scanner, checks if it is not meta or error.
	private boolean UpdateToken()
	{
		try
		{
			Token tempToken = scanner.GetNextToken();
			if(tempToken.GetTokenType() == TokenType.ERROR)
			{
				currentToken = tempToken;
				return false;
			}
			if(tempToken.GetTokenType() == TokenType.META)
			{
				newFile.write(tempToken.GetTokenName() + "\n");
				return UpdateToken();
			}
			currentToken = tempToken;
		}
		catch(Exception e)
		{
			
		}
		return true;
	}
	
	//Initializes the parser and the scanner.
	public Parser(String inputFile, PrintWriter newFile)
	{
		try
		{
			this.scanner = new Scanner(inputFile);
			this.newFile = newFile;
			variableCount = functionCount = statementCount = labelCount = startLabel = endLabel = condLabel = globalCount = localCount = 0;
			globalFlag = true;
			declareFlag = true;
			inFunction = true; 
			globalMap = new HashMap<String, Integer>();
			status = false;
			UpdateToken();
		}
		catch(Exception e)
		{
			
		}
	}
	
	private boolean block_statements()
	{
		if(!currentToken.GetTokenName().equals("{"))
		{
			return false;
		}
		UpdateToken();
		if(!statements())
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals("}"))
		{
			return false;
		}
		UpdateToken();
		return true;
	}
	
	private boolean statements()
	{
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER) && !currentToken.GetTokenName().equals("if") && !currentToken.GetTokenName().equals("while") && !currentToken.GetTokenName().equals("return") && !currentToken.GetTokenName().equals("break") && !currentToken.GetTokenName().equals("continue") && !currentToken.GetTokenName().equals("read") && !currentToken.GetTokenName().equals("write") && !currentToken.GetTokenName().equals("print"))
		{
			return true;
		}
		if(!statement())
		{
			return false;
		}
		statementCount++;
		if(!statements())
		{
			return false;
		}
		return true;
	}
	
	private boolean statement()
	{
		if(currentToken.GetTokenType() == TokenType.IDENTIFIER)
		{
			return statement_dash();
		}
		if(currentToken.GetTokenName().equals("if"))
		{
			return if_statement();
		}
		if(currentToken.GetTokenName().equals("while"))
		{
			return while_statement();
		}
		if(currentToken.GetTokenName().equals("return"))
		{
			return return_statement();
		}
		if(currentToken.GetTokenName().equals("break"))
		{
			return break_statement();
		}
		if(currentToken.GetTokenName().equals("continue"))
		{
			return continue_statement();
		}
		if(currentToken.GetTokenName().equals("read"))
		{
			localFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!currentToken.GetTokenName().equals("("))
			{
				return false;
			}
			localFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
			{
				return false;
			}
			if(localMap.get(currentToken.GetTokenName()) != null)
			{
				localFunction += "local[" + localMap.get(currentToken.GetTokenName()) + "]";
			}
			else
			{
				localFunction += "global[" + globalMap.get(currentToken.GetTokenName()) + "]";
			}
			UpdateToken();
			if(!currentToken.GetTokenName().equals(")"))
			{
				return false;
			}
			localFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!currentToken.GetTokenName().equals(";"))
			{
				return false;
			}
			localFunction += currentToken.GetTokenName() + "\n";
			UpdateToken();
			
			return true;
		}
		if(currentToken.GetTokenName().equals("write"))
		{
			String writeFunction = "";
			writeFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!currentToken.GetTokenName().equals("("))
			{
				return false;
			}
			writeFunction += currentToken.GetTokenName();
			UpdateToken();
			ArrayList<String> expression = new ArrayList<String>();
			if(!expression(expression))
			{
				return false;
			}
			if(expression.size() > 0)
				writeFunction += evaluate(expression);
			if(!currentToken.GetTokenName().equals(")"))
			{
				return false;
			}
			writeFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!currentToken.GetTokenName().equals(";"))
			{
				return false;
			}
			writeFunction += currentToken.GetTokenName();
			localFunction += writeFunction + "\n";
			UpdateToken();
			
			return true;
		}
		if(currentToken.GetTokenName().equals("print"))
		{
			localFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!currentToken.GetTokenName().equals("("))
			{
				return false;
			}
			localFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!(currentToken.GetTokenType() == TokenType.STRING))
			{
				return false;
			}
			localFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!currentToken.GetTokenName().equals(")"))
			{
				return false;
			}
			localFunction += currentToken.GetTokenName();
			UpdateToken();
			if(!currentToken.GetTokenName().equals(";"))
			{
				return false;
			}
			localFunction += currentToken.GetTokenName() + "\n";
			UpdateToken();
			
			return true;
		}
		return false;
	}
	
	private boolean statement_dash()
	{
		boolean globalVariable = false;
		StringBuilder stateFunction = new StringBuilder();
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
		{
			return false;
		}
		String tempAss;
		if(localMap.get(currentToken.GetTokenName()) != null)
		{
			tempAss = "local[" + localMap.get(currentToken.GetTokenName()) + "]";
		}
		else
		{
			tempAss = "global[" + globalMap.get(currentToken.GetTokenName()) + "]";
			globalVariable = true;
		}
		String IDName = currentToken.GetTokenName();
		int arrCount = -1;
		UpdateToken();
		if(currentToken.GetTokenName().equals("["))
		{
			if(localMap.get(IDName) != null)
			{
				stateFunction.append("local[" + localCount++ + "] = " + localMap.get(IDName) + " + ");
				arrCount = localCount - 1;
			}
			else
			{
				stateFunction.append("local[" + localCount++ + "] = " + globalMap.get(IDName) + " + ");
				arrCount = localCount - 1;
				globalVariable = true;
			}
		}
		else if(currentToken.GetTokenName().equals("("))
		{
			stateFunction.append(IDName);
			boolean retValues = func_call(stateFunction);
			localFunction += stateFunction;
			return retValues;
		}
		else
		{
			stateFunction.append(tempAss);
		}
		boolean retValues =  assignment(stateFunction, "local[" + arrCount + "]", globalVariable);
		localFunction += stateFunction;
		return retValues;
	}
	
	private boolean assignment(StringBuilder assignFunction, String variable, Boolean globalVariable)
	{
		if(!id_dash(assignFunction, variable, globalVariable))
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals("="))
		{
			return false;
		}
		assignFunction.append(currentToken.GetTokenName());
		UpdateToken();
		ArrayList<String> expression = new ArrayList<String>();
		if(!expression(expression))
		{
			return false;
		}
		if(expression.size() > 0)
			assignFunction.append(evaluate(expression));
		if(!currentToken.GetTokenName().equals(";"))
		{
			return false;
		}
		assignFunction.append(currentToken.GetTokenName() + "\n");
		UpdateToken();
		return true;
	}
	
	private boolean func_call(StringBuilder funcFunction)
	{
		if(!currentToken.GetTokenName().equals("("))
		{
			return false;
		}
		funcFunction.append(currentToken.GetTokenName());
		UpdateToken();
		if(!expr_list(funcFunction))
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals(")"))
		{
			return false;
		}
		funcFunction.append(currentToken.GetTokenName());
		UpdateToken();
		if(!currentToken.GetTokenName().equals(";"))
		{
			return false;
		}
		funcFunction.append(currentToken.GetTokenName() + "\n");
		UpdateToken();
		return true;
	}
	
	private boolean expr_list(StringBuilder funcFunction)
	{
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER) && !(currentToken.GetTokenType() == TokenType.NUMBER) && !currentToken.GetTokenName().equals("(") && !currentToken.GetTokenName().equals("-"))
		{
			return true;
		}
		if(!non_empty_expr_list(funcFunction))
		{
			return false;
		}
		return true;
	}
	
	private boolean non_empty_expr_list(StringBuilder funcFunction)
	{
		ArrayList<String> expression = new ArrayList<String>();
		if(!expression(expression))
		{
			return false;
		}
		funcFunction.append(evaluate(expression));
		if(!non_empty_expr_list_dash(funcFunction))
		{
			return false;
		}
		return true;
	}
	
	private boolean non_empty_expr_list_dash(StringBuilder funcFunction)
	{
		if(!currentToken.GetTokenName().equals(","))
		{
			return true;
		}
		funcFunction.append(currentToken.GetTokenName());
		UpdateToken();
		ArrayList<String> expression = new ArrayList<String>();
		if(!expression(expression))
		{
			return false;
		}
		funcFunction.append(evaluate(expression));
		if(!non_empty_expr_list_dash(funcFunction))
		{
			return false;
		}
		return true;
	}
	
	private boolean if_statement()
	{
		StringBuilder ifFunction = new StringBuilder();
		ifFunction.append(currentToken.GetTokenName());
		UpdateToken();
		if(!currentToken.GetTokenName().equals("("))
		{
			return false;
		}
		ifFunction.append(currentToken.GetTokenName());
		UpdateToken();
		if(!condition_expression(ifFunction))
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals(")"))
		{
			return false;
		}
		ifFunction.append(currentToken.GetTokenName() + "\n");
		int ifEndLabel = labelCount++;
		int ifStartLabel = labelCount++;
		ifFunction.append("goto L" + ifStartLabel + ";\n" + "goto L" + ifEndLabel + ";\n");
		UpdateToken();
		localFunction += ifFunction;
		localFunction += "L" + ifStartLabel + ":;\n";
		if(!block_statements())
		{
			return false;
		}
		localFunction += "L" + ifEndLabel + ":;\n";
		return true;
	}
	
	private boolean condition_expression(StringBuilder ifFunction)
	{
		if(!condition(ifFunction))
		{
			return false;
		}
		if(!condition_expression_dash(ifFunction))
		{
			return false;
		}
		return true;
	}
	
	private boolean condition_expression_dash(StringBuilder ifFunction)
	{
		if(!currentToken.GetTokenName().equals("||") && !currentToken.GetTokenName().equals("&&"))
		{
			return true;
		}
		if(!condition_op(ifFunction))
		{
			return false;
		}
		if(!condition(ifFunction))
		{
			return false;
		}
		return true;
	}
	
	private boolean condition_op(StringBuilder ifFunction)
	{
		if(!currentToken.GetTokenName().equals("||") && !currentToken.GetTokenName().equals("&&"))
		{
			return false;
		}
		ifFunction.append(currentToken.GetTokenName());
		UpdateToken();
		return true;
	}
	
	private boolean condition(StringBuilder ifFunction)
	{
		ArrayList<String> expression = new ArrayList<String>();
		if(!expression(expression))
		{
			return false;
		}
		ifFunction.append(evaluate(expression));
		if(!comparison_op(ifFunction))
		{
			return false;
		}
		ArrayList<String> expression2 = new ArrayList<String>();
		if(!expression(expression2))
		{
			return false;
		}
		ifFunction.append(evaluate(expression2));
		return true;
	}
	
	private boolean comparison_op(StringBuilder ifFunction)
	{
		if(!currentToken.GetTokenName().equals("==") && !currentToken.GetTokenName().equals("!=") && !currentToken.GetTokenName().equals(">") && !currentToken.GetTokenName().equals(">=") && !currentToken.GetTokenName().equals("<") && !currentToken.GetTokenName().equals("<="))
		{
			return false;
		}
		ifFunction.append(currentToken.GetTokenName());
		UpdateToken();
		return true;
	}
	
	private boolean while_statement()
	{
		StringBuilder whileFunction = new StringBuilder();
		int whileStartLabel = labelCount++;
		int whileEndLabel = labelCount++;
		int whileCondLabel = labelCount++;
		startLabel = whileStartLabel;
		endLabel = whileEndLabel;
		condLabel = whileCondLabel;
		whileFunction.append("L" + whileCondLabel + ":;\n");
		whileFunction.append("if");
		UpdateToken();
		if(!currentToken.GetTokenName().equals("("))
		{
			return false;
		}
		whileFunction.append(currentToken.GetTokenName());
		UpdateToken();
		if(!condition_expression(whileFunction))
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals(")"))
		{
			return false;
		}
		whileFunction.append(currentToken.GetTokenName() + "\n");
		whileFunction.append("goto L" + whileStartLabel + ";\n");
		whileFunction.append("goto L" + whileEndLabel + ";\n");
		UpdateToken();
		whileFunction.append("L" + whileStartLabel + ":;\n");
		localFunction += whileFunction;
		if(!block_statements())
		{
			return false;
		}
		whileFunction = new StringBuilder();
		whileFunction.append("goto L" + whileCondLabel + ";\n");
		whileFunction.append("L" + whileEndLabel + ":;\n");
		localFunction += whileFunction;
		return true;
	}
	
	private boolean return_statement()
	{
		String returnFunction = "";
		UpdateToken();
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER) && !(currentToken.GetTokenType() == TokenType.NUMBER) && !currentToken.GetTokenName().equals("(") && !currentToken.GetTokenName().equals(")") && !currentToken.GetTokenName().equals("-"))
		{
			if(currentToken.GetTokenName().equals(";"))
			{
				returnFunction += "return " + currentToken.GetTokenName() + "\n";
				UpdateToken();
				localFunction += returnFunction;
				return true;
			}
		}
		int returnValuesue = localCount++;
		returnFunction += "local[" + returnValuesue + "] = ";
		ArrayList<String> expression = new ArrayList<String>();
		if(!expression(expression))
		{
			return false;
		}
		returnFunction += evaluate(expression);
		if(!currentToken.GetTokenName().equals(";"))
		{
			return false;
		}
		returnFunction += ";\n";
		returnFunction += "return local[" + returnValuesue + "]" + currentToken.GetTokenName() + "\n";
		UpdateToken();
		localFunction += returnFunction;
		return true;
	}
	
	private boolean break_statement()
	{
		UpdateToken();
		if(!currentToken.GetTokenName().equals(";"))
		{
			return false;
		}
		localFunction += "goto L" + endLabel + ";\n";
		UpdateToken();
		return true;
	}
	
	private boolean continue_statement()
	{
		UpdateToken();
		if(!currentToken.GetTokenName().equals(";"))
		{
			return false;
		}
		localFunction += "goto L" + condLabel + ";\n";
		UpdateToken();
		return true;
	}
	
	private boolean expression(ArrayList<String> expression)
	{
		if(!term(expression))
		{
			return false;
		}
		if(!expression_dash(expression))
		{
			return false;
		}
		return true;
	}
	
	private boolean expression_dash(ArrayList<String> expression)
	{
		if(!currentToken.GetTokenName().equals("+") && !currentToken.GetTokenName().equals("-"))
		{
			return true;
		}
		if(!addop(expression))
		{
			return false;
		}
		if(!term(expression))
		{
			return false;
		}
		if(!expression_dash(expression))
		{
			return false;
		}
		return true;
	}
	
	private boolean addop(ArrayList<String> expression)
	{
		if(!currentToken.GetTokenName().equals("+") && !currentToken.GetTokenName().equals("-"))
		{
			return false;
		}
		expression.add(currentToken.GetTokenName());
		UpdateToken();
		return true;
	}
	
	private boolean term(ArrayList<String> expression)
	{
		if(!factor(expression))
		{
			return false;
		}
		if(!term_dash(expression))
		{
			return false;
		}
		return true;
	}
	
	private boolean term_dash(ArrayList<String> expression)
	{
		if(!currentToken.GetTokenName().equals("*") && !currentToken.GetTokenName().equals("/"))
		{
			return true;
		}
		if(!mulop(expression))
		{
			return false;
		}
		if(!factor(expression))
		{
			return false;
		}
		if(!term_dash(expression))
		{
			return false;
		}
		return true;
	}
	
	private boolean mulop(ArrayList<String> expression)
	{
		if(!currentToken.GetTokenName().equals("*") && !currentToken.GetTokenName().equals("/"))
		{
			return false;
		}
		expression.add(currentToken.GetTokenName());
		UpdateToken();
		return true;
	}
	
	private boolean factor(ArrayList<String> expression)
	{
		if(currentToken.GetTokenType() == TokenType.IDENTIFIER)
		{
			String variable = "";
			StringBuilder factorFunction = new StringBuilder();
			String IDName = currentToken.GetTokenName();
			boolean normalIDFlag = false;
			UpdateToken();
			if(currentToken.GetTokenName().equals("("))
			{
				factorFunction.append("local[" + localCount++ + "] = " + IDName);
				variable = "local[" + (localCount - 1) + "]";
			}
			else if(currentToken.GetTokenName().equals("["))
			{
				if(localMap.get(IDName) != null)
				{
					factorFunction.append("local[" + localCount++ + "] = " + localMap.get(IDName) + " + ");
					variable = "local[local[" + (localCount - 1) + "]]";
				}
				else
				{
					factorFunction.append("local[" + localCount++ + "] = " + globalMap.get(IDName) + " + ");
					variable = "global[local[" + (localCount - 1) + "]]";
				}
			}
			else
			{
				normalIDFlag = true;
				if(localMap.get(IDName) != null)
				{
					variable = "local[" + localMap.get(IDName) + "]";
				}
				else
				{
					variable = "global[" + globalMap.get(IDName) + "]";
				}
			}
			boolean retValues = factor_dash(factorFunction);
			factorFunction.append(";\n");
			expression.add(variable);
			if(!normalIDFlag)
				localFunction += factorFunction;
			return retValues;
		}
		if(currentToken.GetTokenType() == TokenType.NUMBER)
		{
			localFunction += "local[" + localCount++ + "] = " + currentToken.GetTokenName() + ";\n";
			expression.add("local[" + (localCount -1) + "]");
			UpdateToken();
			return true;
		}
		if(currentToken.GetTokenName().equals("-"))
		{
			UpdateToken();
			if(!(currentToken.GetTokenType() == TokenType.NUMBER))
			{
				return false;
			}
			localFunction += "local[" + localCount++ + "] = -" + currentToken.GetTokenName() + ";\n";
			expression.add("local[" + (localCount -1) + "]");
			UpdateToken();
			return true;
		}
		if(currentToken.GetTokenName().equals("("))
		{
			ArrayList<String> newExpression = new ArrayList<String>();
			UpdateToken();
			if(!expression(newExpression))
			{
				return false;
			}
			expression.add(evaluate(newExpression));
			if(!currentToken.GetTokenName().equals(")"))
			{
				return false;
			}
			UpdateToken();
			return true;
		}
		return false;
	}
	
	private boolean factor_dash(StringBuilder factorFunction)
	{
		if(currentToken.GetTokenName().equals("["))
		{
			ArrayList<String> expression = new ArrayList<String>();
			UpdateToken();
			if(!expression(expression))
			{
				return false;
			}
			factorFunction.append(evaluate(expression));
			if(!currentToken.GetTokenName().equals("]"))
			{
				return false;
			}
			UpdateToken();
			return true;
		}
		if(currentToken.GetTokenName().equals("("))
		{
			factorFunction.append(currentToken.GetTokenName());
			UpdateToken();
			if(!expr_list(factorFunction))
			{
				return false;
			}
			if(!currentToken.GetTokenName().equals(")"))
			{
				return false;
			}
			factorFunction.append(currentToken.GetTokenName());
			UpdateToken();
			return true;
		}
		return true;
	}
	
	private boolean non_empty_list()
	{
		if(!type_name(new StringBuilder()))
		{
			return false;
		}
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
		{
			return false;
		}
		
		localMap.put(currentToken.GetTokenName(), localCount++);
		
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!non_empty_list_dash())
		{
			return false;
		}
		return true;
	}
	
	private boolean non_empty_list_dash()
	{
		if(!currentToken.GetTokenName().equals(","))
		{
			return true;
		}
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!type_name(new StringBuilder()))
		{
			return false;
		}
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
		{
			return false;
		}
		
		localMap.put(currentToken.GetTokenName(), localCount++);
		
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!non_empty_list_dash())
		{
			return false;
		}
		return true;
	}
	
	private boolean parameter_list_dash()
	{
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
		{
			return true;
		}
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!non_empty_list_dash())
		{
			return false;
		}
		return true;
	}
	
	private boolean parameter_list()
	{
		if(!currentToken.GetTokenName().equals("int") && !currentToken.GetTokenName().equals("void") && !currentToken.GetTokenName().equals("binary") && !currentToken.GetTokenName().equals("decimal"))
		{
			return true;
		}
		if(currentToken.GetTokenName().equals("void"))
		{
			newFile.write(currentToken.GetTokenName());
			UpdateToken();
			if(!parameter_list_dash())
			{
				return false;
			}
			return true;
		}
		if(!non_empty_list())
		{
			return false;
		}
		return true;
	}
	
	private boolean func_decl()
	{
		if(!type_name(new StringBuilder()))
		{
			return false;
		}
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
		{
			return false;
		}
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!currentToken.GetTokenName().equals("("))
		{
			return false;
		}
		
		localMap = new HashMap<String, Integer>();
		localCount = 0;
		localFunction = "";
		declareFlag = true;
		
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!parameter_list())
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals(")"))
		{
			return false;
		}
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		return true;
	}
	
	private boolean func_dash()
	{
		if(currentToken.GetTokenName().equals(";"))
		{
			newFile.write(currentToken.GetTokenName() + "\n");
			UpdateToken();
			return true;
		}
		
		Iterator it = localMap.entrySet().iterator();
		while(it.hasNext())
		{
			Entry pair = (Entry)it.next();
			localFunction += "local[" + pair.getValue() + "]" + "=" + pair.getKey() + ";\n";
		}
		
		if(!currentToken.GetTokenName().equals("{"))
		{
			return false;
		}
		
		inFunction = true;
		
		newFile.write("\n" + currentToken.GetTokenName() + "\n");
		UpdateToken();
		if(!data_decls())
		{
			return false;
		}
		
		declareFlag = false;
		
		if(!statements())
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals("}"))
		{
			return false;
		}
		
		inFunction = false;
		
		newFile.write("int local[" + localCount + "];\n");
		newFile.write(localFunction);
		
		newFile.write("\n" + currentToken.GetTokenName() + "\n");
		UpdateToken();
		return true;
	}
	
	private boolean func()
	{
		if(!func_decl())
		{
			return false;
		}
		if(!func_dash())
		{
			return false;
		}
		return true;
	}
	
	private boolean func_list()
	{
		if(!currentToken.GetTokenName().equals("int") && !currentToken.GetTokenName().equals("void") && !currentToken.GetTokenName().equals("binary") && !currentToken.GetTokenName().equals("decimal"))
		{
			return true;
		}
		if(!func())
		{
			return false;
		}
		functionCount++;
		if(!func_list())
		{
			return false;
		}
		return true;
	}
	
	private boolean type_name(StringBuilder typeNameFunction)
	{
		if(currentToken.GetTokenName().equals("int"))
		{
			if(!inFunction)
			{
				newFile.write(currentToken.GetTokenName() + " ");
			}
			else
			{
				typeNameFunction.append(currentToken.GetTokenName() + " ");
			}
			UpdateToken();
			return true;
		}
		if(currentToken.GetTokenName().equals("void"))
		{
			if(!inFunction)
			{
				newFile.write(currentToken.GetTokenName() + " ");
			}
			else
			{
				typeNameFunction.append(currentToken.GetTokenName() + " ");
			}
			UpdateToken();
			return true;
		}
		if(currentToken.GetTokenName().equals("binary"))
		{
			if(!inFunction)
			{
				newFile.write(currentToken.GetTokenName() + " ");
			}
			else
			{
				typeNameFunction.append(currentToken.GetTokenName() + " ");
			}
			UpdateToken();
			return true;
		}
		if(currentToken.GetTokenName().equals("decimal"))
		{
			if(!inFunction)
			{
				newFile.write(currentToken.GetTokenName() + " ");
			}
			else
			{
				typeNameFunction.append(currentToken.GetTokenName() + " ");
			}
			UpdateToken();
			return true;
		}
		return false;
	}
	
	private boolean id_list()
	{
		if(!id())
		{
			return false;
		}
		if(!id_list_dash())
		{
			return false;
		}
		return true;
	}
	
	private boolean id_dash(StringBuilder iddashFunction, String variable, boolean globalVariable)
	{
		if(!currentToken.GetTokenName().equals("["))
		{
			return true;
		}
		UpdateToken();
		if(declareFlag)
		{
			if(!(currentToken.GetTokenType() == TokenType.NUMBER))
			{
				return false;
			}
			int arrSize = Integer.parseInt(currentToken.GetTokenName());
			if(globalFlag)
			{
				globalCount += arrSize - 1;
			}
			else
			{
				localCount += arrSize - 1;
			}
			UpdateToken();
		}
		else
		{
			ArrayList<String> expression = new ArrayList<String>();
			if(!expression(expression))
			{
				return false;
			}
			iddashFunction.append(evaluate(expression));
			if(globalVariable)
			{
				iddashFunction.append(";\nglobal[" + variable + "] ");
			}
			else
			{
				iddashFunction.append(";\nlocal[" + variable + "] ");
			}
		}
		if(!currentToken.GetTokenName().equals("]"))
		{
			return false;
		}
		UpdateToken();
		return true;
	}
	
	private boolean id_list_dash()
	{
		if(!currentToken.GetTokenName().equals(","))
		{
			return true;
		}
		UpdateToken();
		if(!id())
		{
			return false;
		}
		if(!id_list_dash())
		{
			return false;
		}
		return true;
	}
	
	private boolean id()
	{
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
		{
			return false;
		}
		if(globalFlag)
		{
			globalMap.put(currentToken.GetTokenName(), globalCount++);
			currrentGlobalID = currentToken.GetTokenName();
		}
		else
		{
			localMap.put(currentToken.GetTokenName(), localCount++);
		}
		UpdateToken();
		variableCount++;
		if(!id_dash(new StringBuilder(), null, false))
		{
			return false;
		}
		return true;
	}
	
	private boolean data_decls()
	{
		if(!type_name(new StringBuilder()))
		{
			return true;
		}
		if(!id_list())
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals(";"))
		{
			return false;
		}
		UpdateToken();
		if(!data_decls())
		{
			return false;
		}
		return true;
	}
	
	private boolean program_dash(StringBuilder programDashFunction)
	{
		if(currentToken.GetTokenName().equals(",") || currentToken.GetTokenName().equals("["))
		{
			globalMap.put(currrentGlobalID, globalCount++);
			variableCount++;
			if(!id_dash(new StringBuilder(), null, false))
			{
				return false;
			}
			if(!id_list_dash())
			{
				return false;
			}
			if(!currentToken.GetTokenName().equals(";"))
			{
				return false;
			}
			UpdateToken();
			if(!program_start())
			{
				return false;
			}
			return true;
		}
		if(currentToken.GetTokenName().equals(";"))
		{
			globalMap.put(currrentGlobalID, globalCount++);
			UpdateToken();
			if(!program_start())
			{
				return false;
			}
			variableCount++;
			return true;
		}
		newFile.write("int global[" + globalCount +"];\n");
		newFile.write(programDashFunction.toString());
		if(!currentToken.GetTokenName().equals("("))
		{
			return false;
		}

		localMap = new HashMap<String, Integer>();
		localCount = 0;
		localFunction = "";
		declareFlag = true;
		globalFlag = false;
		
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!parameter_list())
		{
			return false;
		}
		if(!currentToken.GetTokenName().equals(")"))
		{
			return false;
		}
		newFile.write(currentToken.GetTokenName());
		UpdateToken();
		if(!func_dash())
		{
			return false;
		}
		if(!func_list())
		{
			return false;
		}
		functionCount++;
		return true;
	}
	
	private boolean program_start()
	{
		StringBuilder programStartFunction = new StringBuilder();
		if(currentToken.GetTokenType() == TokenType.EOF)
		{
			return true;
		}
		if(!type_name(programStartFunction))
		{
			return false;
		}
		if(!(currentToken.GetTokenType() == TokenType.IDENTIFIER))
		{
			return false;
		}
		currrentGlobalID = currentToken.GetTokenName();
		programStartFunction.append(currentToken.GetTokenName());
		UpdateToken();
		if(!program_dash(programStartFunction))
		{
			return false;
		}
		return true;
	}
	
	private boolean program()
	{
		if(!program_start())
		{
			return false;
		}
		return true;
	}
	
	//Entry for the start of the parsing process
	public void parse()
	{
		try
		{
			status = program();
		}
		catch(Exception e)
		{
			
		}
	}
}