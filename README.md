# Cool-Interpreter 📚
Cool Interpreter is an unfinished compiler for the Cool programming language (Classroom Object-Oriented Language). It implements the front-end phases—like lexical and syntactic analysis—and also implements semantic checks. The final code generation stage was not completed, so this project runs as a basic interpreter rather than a full compiler.

# Overview
This project was started to build a Cool compiler, but it currently serves as an interpreter that can parse and partially execute Cool programs. It includes:  
	1. A lexical analyzer that tokenizes the source code.  
	2. A parser that builds an Abstract Syntax Tree (AST) from the tokens.  
	3. Semantic checks (type checking, scope analysis, etc.).  
	4. A basic interpreter that executes the AST up to the point possible.  
 #  Why “Interpreter” :question:
The original plan was to generate code for a target platform (MIPS), but the final code generation phase was never completed. As a result, the project currently interprets the AST rather than compiling it to native code.
#  Implementation Details :computer:
* Language: Written entirely in Java.  
*	ANTLR: Uses .g4 grammar files to generate the lexer and parser.  
*	Interpreter: A custom visitor processes the AST to perform evaluations.
    
Because it’s Java-based, you’ll need a Java Development Kit (JDK) installed. You can modify or extend the ANTLR grammars (.g4 files) to update the parsing logic.

# How does the semantic evaluation work ❔
This process starts from the representation generated during the syntactic analysis phase, in the form of an abstract syntax tree (AST). The result of this phase is an annotated version of the above-mentioned intermediate representation, which includes information about the symbols and types present in the program.
The current project will receive, as command-line parameters, the names of one or more files containing Cool programs and will print to standard error any semantic errors that occur, or nothing if the program is semantically correct. The test programs will be lexically and syntactically correct!  
Below, the functionality is illustrated using a simple Cool program as a starting point: 
```COOL
 -  prog.cl
class A inherits B {};
class B inherits A {};
```
For which we get the following output:  
```
$ java cool.compiler.Compiler prog.cl
"prog.cl", line 1:7, Semantic error: Inheritance cycle for class A
"prog.cl", line 2:7, Semantic error: Inheritance cycle for class B
Compilation halted
