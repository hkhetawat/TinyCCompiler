*IR*
This project contains the definition of the the following classes:
1. Scanner
2. Token
3. TokenType (Enum)
4. Parser
5. IntraDemo

This intermediate representation generator uses the Scanner and Parser that I wrote as part of the earlier projects.
The Scanner and Parser classes here are the most important classes which contains the actual logic for the scanning and parsing of the input program to take place. In addition the parser class also generates the intermediate representation of the input program. The Token class contains the definition for each token, which includes the token string and the type of token. An object of this class is returned every time the GetNextToken function is called on an object of the Scanner class. The parsing process begins on execution of the parse() function in Parser class.
The IntraDemo class uses a Parser object to parse an input program and report whether the input program was syntactically correct or not. In case the parsing was a success, it also generates a file with “_gen” appended to the name of the file as the output.

*COMPILATION*
In order to compile this project, run the following command in the terminal:
make clean; make

This will generate the class files for each of the classes in the project.

*EXECUTION*
In order to execute the program, run the following command in the terminal:
java IntraDemo <INPUT-SOURCE-CODE>
A sample program binrep.c is provided in the tarball.


*OUTPUT*
If we input the file test.c:

#include <stdio.h>
#define read(x) scanf("%d\n", &x)
#define write(x) printf("%d\n", x)
void foo() { 
    int a; 
    read(a);
    write(a);
}

int main() { 
    foo(); 
}

The output will be a file test_gen.c:

#include <stdio.h>
#define read(x) scanf("%d\n", &x)
#define write(x) printf("%d\n", x)
int global[0];
void foo()
{
int local[1];
read(local[0]);
write(local[0]);

}
int main()
{
int local[0];
foo();

}


*KNOWN ISSUES*
None
