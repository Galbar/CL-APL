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

package interp;

import org.antlr.runtime.tree.*;
import org.antlr.runtime.Token;

import java.io.OutputStream;
import java.io.IOException;
import java.lang.StringBuilder;

import parser.AplLexer;

public class CodeAnalyzer {
    private AplTree root;
    private Stack stack;
    private int linenumber;
    private FunctionTable funcTable;

    public CodeAnalyzer(AplTree root) {
        this.root = root;
        stack = new Stack();
        funcTable = new FunctionTable();
    }

    public boolean parse() {
        // Treat all the functions
        AplTree mainNode;
        try {
            mainNode = findFunction("main");
        } catch (Exception e) {
            print(e.getMessage());
            return false;
        }

        setLineNumber(mainNode);
        stack.pushActivationRecord("main", lineNumber());
        stack.defineVariable("argc", new Data(Data.Type.INT));
        stack.defineVariable("argv", new Data(Data.Type.ARRAY, new Data(Data.Type.ARRAY, new Data(Data.Type.CHAR))));

        boolean ret = parseFunction(mainNode);
        stack.popActivationRecord();

        return ret;
    }

    public boolean parseFunction(AplTree node) {
        String signature;

        try {
            signature = getFuncSignature(node);
        } catch (Exception e) {
            print(e.getMessage());
            return false;
        }

        int numParams = node.getChild(1).getChildCount();
        String name = node.getChild(0).getText();
        if (name.equals("main")) numParams = 2;

        FunctionNode function = new FunctionNode(name, numParams, stack.getCurrentAR());
        funcTable.add(signature, function);

        AplTree listInstr = node.getChild(2);

        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            AplTree instr = listInstr.getChild(i);
            parseInstruction(instr);
        }

        // Obtain type

        return true;
    }

    protected String getFuncSignature(AplTree func) throws Exception {
        if (func.getType() != AplLexer.FUNC) {
            print("Node isn't a function root node. Can't get function signature from it");
            return "";
        }

        String funcName = func.getChild(0).getText();
        StringBuilder signatureBuild = new StringBuilder();
        signatureBuild.append(funcName);
        signatureBuild.append(" ( ");

        try {
            AplTree params = func.getChild(1);
            int numParams = params.getChildCount();
            if (funcName.equals("main")) numParams = 2;
            for (int i = 0; i < numParams; ++i) {
                Data param = stack.getVariable(i);
                if (i != 0) {
                    signatureBuild.append(", ");
                }
                signatureBuild.append(param.typeToString());
            }
        } catch (Exception e) {
            print(e.getMessage());
        }

        signatureBuild.append(" )");

        return signatureBuild.toString();
    }

    protected AplTree findFunction(String name) throws Exception {
        for (int i = 0; i < root.getChildCount(); ++i) {
            if (root.getChild(i).getChild(0).getText().equals(name)) return root.getChild(i);
        }
        throw new Exception("Function " + name + " not defined.");
    }

    protected CodeNode parseInstruction(AplTree node) {
        switch(node.getType()) {
            case AplLexer.ASSIGN:
                // TODO: Crear el ExpressionNode y obtener el tipo
                stack.defineVariable();
                VariableNode var = new VariableNode();
                break;
            case AplLexer.IF:
                break;
            case AplLexer.WHILE:
                break;
            case AplLexer.FOR:
                break;
            case AplLexer.PFOR:
                break;
            case AplLexer.READ:
                break;
            case AplLexer.WRITE:
                break;
            case AplLexer.FUNCALL:
                break;
        }
    }

    /**
     * Gets the current line number. In case of a runtime error,
     * it returns the line number of the statement causing the
     * error.
     */
    public int lineNumber() { return linenumber; }

    /** Defines the current line number associated to an AST node. */
    private void setLineNumber(AplTree t) { linenumber = t.getLine();}

    /** Defines the current line number with a specific value */
    private void setLineNumber(int l) { linenumber = l;}

    protected boolean onNodeEnd(AplTree node) {
        return true;
    }

    public void print(String message) {
        try {
            System.out.write((message + "\n").getBytes());
        }
        catch (IOException e){};
    }

    public FunctionTable getFunctionTable() { return funcTable; }

    Stack getContext() {
        return stack;
    }
}
