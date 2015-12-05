import java.io.PrintWriter;

//This class uses the Parser class to demo how the Parser class works.
public class IntraDemo
{
	public static void main(String args[])
	{
		try
		{
			if(args.length<1)
			{
				System.out.println("Please give input file name.");
				return;
			}
			PrintWriter newFile = new PrintWriter(args[0].split("\\.")[0] + "_gen." + args[0].split("\\.")[1], "UTF-8");
			Parser p = new Parser(args[0], newFile);
			p.parse();
			newFile.close();
			System.out.println(!p.status?"Error":"Pass");
			if(!p.status)
			{
				System.out.println("Unexpected token: " + p.currentToken.GetTokenName());
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}