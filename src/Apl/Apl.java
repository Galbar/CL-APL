/**
 * Copyright (c) 2016, Alessio Linares and Guillermo Ojeda
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package Apl;

// Imports for ANTLR
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

// Imports from Java
import org.apache.commons.cli.*; // Command Language Interface
import java.io.*;
import java.lang.StringBuilder;
import java.util.Set;

// Parser and Interpreter
import parser.*;
import interp.*;

/**
 * The class <code>Apl</code> implement the main function of the
 * interpreter. It accepts a set of options to generate the AST in
 * dot format and avoid the execution of the program. To know about
 * the accepted options, run the command Apl -help.
 */

public class Apl{

    /** The file name of the program. */
    private static String infile = null;
    /** Name of the file representing the AST. */
    private static String astfile = null;
    /** Flag indicating that the AST must be written in dot format. */
    private static boolean dotformat = false;
    /** Name of the file storing the trace of the program. */
    private static String tracefile = null;
    /** Flag to indicate whether the program must be executed after parsing. */
    private static boolean execute = true;

    /** Main program that invokes the parser and the interpreter. */

    public static void main(String[] args) throws Exception {
        // Parser for command line options
        if (!readOptions (args)) System.exit(1);

        // Parsing of the input file

        CharStream input = null;
        try {
            input = new ANTLRFileStream(infile);
        } catch (IOException e) {
            System.err.println ("Error: file " + infile + " could not be opened.");
            System.exit(1);
        }

        // Creates the lexer
        AplLexer lex = new AplLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lex);

        // Creates and runs the parser. As a result, an AST is created
        AplParser parser = new AplParser(tokens);
        AplTreeAdaptor adaptor = new AplTreeAdaptor();
        parser.setTreeAdaptor(adaptor);
        AplParser.prog_return result = null;
        try {
            result = parser.prog();
        } catch (Exception e) {} // Just catch the exception (nothing to do)

        // Check for parsing errors
        int nerrors = parser.getNumberOfSyntaxErrors();
        if (nerrors > 0) {
            System.err.println (nerrors + " errors detected. " +
                                "The program has not been executed.");
            System.exit(1);
        }

        // Get the AST
        AplTree t = (AplTree)result.getTree();

        // Generate a file for the AST (option -ast file)
        if (astfile != null) {
            File ast = new File(astfile);
            BufferedWriter output = new BufferedWriter(new FileWriter(ast));
            if (dotformat) {
                DOTTreeGenerator gen = new DOTTreeGenerator();
                output.write(gen.toDOT(t).toString());
            } else {
                output.write(t.toStringTree());
            }
            output.close();
        }

        // Start interpretation (only if execution required)
        if (execute) {
            CodeAnalyzer CA = new CodeAnalyzer(t);
            CA.parse();
            FunctionTable table = CA.getFunctionTable();
            Set<String> signatures = table.getSignatures();
            StringBuilder str = new StringBuilder();
            str.append("#include <stdio.h>\n\n");
            for (String sig : signatures) {
                FunctionNode fn = table.get(sig);
                table.resolveType(fn.getData());
                str.append(fn.getData().typeToString());
                str.append(" ");
                str.append(sig);
                str.append(";\n");
            }

            str.append("\n");

            for (String sig : signatures) {
                FunctionNode fn = table.get(sig);
                str.append(fn.toC(table));
                str.append("\n");
            }

            System.out.print(str.toString());
        }
    }

    /**
     * Function to parse the command line. It defines some of
     * the attributes of the class. It returns true if the parsing
     * hass been successful, and false otherwise.
     */

    private static boolean readOptions(String[] args) {
        // Define the options
        Option help = new Option("help", "print this message");
        Option noexec = new Option("noexec", "do not execute the program");
        Option dot = new Option("dot", "dump the AST in dot format");
        Option ast = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("write the AST")
                        .create ("ast");
        Option trace = OptionBuilder
                        .withArgName ("file")
                        .hasArg()
                        .withDescription ("write a trace of function calls during the execution of the program")
                        .create ("trace");

        Options options = new Options();
        options.addOption(help);
        options.addOption(dot);
        options.addOption(ast);
        options.addOption(trace);
        options.addOption(noexec);
        CommandLineParser clp = new GnuParser();
        CommandLine line = null;

        String cmdline = "Apl [options] file";


        // Parse the options
        try {
            line = clp.parse (options, args);
        }
        catch (ParseException exp) {
            System.err.println ("Incorrect command line: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }

        // Option -help
        if (line.hasOption ("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }

        // Option -dot
        if (line.hasOption ("dot")) dotformat = true;

        // Option -ast dotfile
        if (line.hasOption ("ast")) astfile = line.getOptionValue ("ast");

        // Option -trace dotfile
        if (line.hasOption ("trace")) tracefile = line.getOptionValue ("trace");

        // Option -noexec
        if (line.hasOption ("noexec")) execute = false;

        // Remaining arguments (the input file)
        String[] files = line.getArgs();
        if (files.length != 1) {
            System.err.println ("Incorrect command line.");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp (cmdline, options);
            return false;
        }

        infile = files[0];
        return true;
    }
}

