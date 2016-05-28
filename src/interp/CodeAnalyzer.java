/** * Copyright (c) 2016, Alessio Linares and Guillermo Ojeda
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
        } catch (AplException e) {
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
        } catch (AplException e) {
            print(e.getMessage());
            return false;
        }

        int numParams = node.getChild(1).getChildCount();
        String name = node.getChild(0).getText();
        if (name.equals("main")) numParams = 2;

        FunctionNode function = new FunctionNode(name, numParams, stack.getCurrentAR());
        funcTable.add(signature, function);

        AplTree listInstr = node.getChild(2);

        try {
            for (int i = 0; i < listInstr.getChildCount(); ++i) {
                AplTree instr = listInstr.getChild(i);
                CodeNode instrNode = parseInstruction(instr, function);
                if (instrNode != null) {
                    function.appendChild(instrNode);
                }
            }
        } catch (AplException e) {
            print(e.getMessage());
            return false;
        }
        function.appendChild(new FreeNode());

        // Obtain type

        return true;
    }

    protected String getFuncSignature(AplTree func) throws AplException {
        if (func.getType() != AplLexer.FUNC) {
            print("Node isn't a function root node. Can't get function signature from it");
            return "";
        }

        String funcName = func.getChild(0).getText();
        StringBuilder signatureBuild = new StringBuilder();
        signatureBuild.append(funcName);
        signatureBuild.append(" ( ");

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

        signatureBuild.append(" )");

        return signatureBuild.toString();
    }

    protected AplTree findFunction(String name) throws AplException {
        for (int i = 0; i < root.getChildCount(); ++i) {
            if (root.getChild(i).getChild(0).getText().equals(name)) return root.getChild(i);
        }
        throw new AplException("Function " + name + " not defined.");
    }

    protected CodeNode parseInstruction(AplTree node, FunctionNode function) throws AplException {
        CodeNode retval = null;
        switch(node.getType()) {
            case AplLexer.ASSIGN:
                {
                    String varname;
                    int varID;

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        varname = node.getChild(0).getChild(0).getText();
                    } else {
                        varname = node.getChild(0).getText();
                    }

                    ExpressionNode expr = parseExpression(node.getChild(1));
                    Data data = expr.getData();

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        varID = stack.setArrayElement(varname, data);
                    } else {
                        varID = stack.defineVariable(varname, data);
                    }

                    Data varData = stack.getVariable(varID);
                    CodeNode var;

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        ExpressionNode delta = parseExpression(node.getChild(0).getChild(1));
                        var = new ArrayAccessNode(varID, varData.getSubData(), delta);
                    } else {
                        var = new VariableNode(varID, varData);
                    }
                    retval = new AssignNode(var, expr);
                }
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
                {
                    String varname;
                    int varID;

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        varname = node.getChild(0).getChild(0).getText();
                    } else {
                        varname = node.getChild(0).getText();
                    }

                    try {
                        varID = stack.getVariableID(varname);
                    } catch (AplException e) {
                        print("Warning: variable with name `" + varname + "` not defined before reading. Assuming int.");
                        varID = stack.defineVariable(varname, new Data(Data.Type.INT));
                    }

                    Data varData = stack.getVariable(varID);
                    CodeNode var;

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        ExpressionNode delta = parseExpression(node.getChild(0).getChild(1));
                        var = new ArrayAccessNode(varID, varData.getSubData(), delta);
                    } else {
                        var = new VariableNode(varID, varData);
                    }

                    retval = new ReadNode(var);
                }
                break;
            case AplLexer.WRITE:
                {
                    ExpressionNode expr = parseExpression(node.getChild(0));
                    retval = new WriteNode(expr);
                }
                break;
            case AplLexer.RETURN:
                {
                    ExpressionNode expr = null;
                    if (node.getChildCount() == 1) {
                        expr = parseExpression(node.getChild(0));
                        function.getData().addDependency(expr.getData());
                    }
                    retval = new ReturnNode(expr);
                    function.appendChild(new FreeNode());
                }
                break;
            case AplLexer.FUNCALL:
        }
        return retval;
    }

    protected ExpressionNode parseExpression(AplTree expression) throws AplException {
        ExpressionNode expr = new ExpressionNode();
        int id;
        switch (expression.getType()) {
            case AplLexer.INT:
            case AplLexer.FLOAT:
            case AplLexer.BOOLEAN:
            case AplLexer.STRING:
            case AplLexer.CHAR:
                expr.appendChild(new ConstantNode(expression));
                break;
            case AplLexer.ID:
                id = stack.getVariableID(expression.getText());
                expr.appendChild(new VariableNode(id, stack.getVariable(id)));
                break;
            case AplLexer.IDARR:
                Data data;
                String name = expression.getChild(0).getText();
                ExpressionNode accessExpr = parseExpression(expression.getChild(1));
                if (name.equals("int")) {
                    data = new Data(Data.Type.INT);
                } else if (name.equals("float")) {
                    data = new Data(Data.Type.FLOAT);
                } else if (name.equals("char")) {
                    data = new Data(Data.Type.CHAR);
                } else if (name.equals("bool")) {
                    data = new Data(Data.Type.BOOL);
                } else {
                    id = stack.getVariableID(expression.getChild(0).getText());
                    expr.appendChild(new ArrayAccessNode(id, stack.getVariable(id).getSubData(), accessExpr));
                    break;
                }
                expr.appendChild(new ArrayNode(new Data(Data.Type.ARRAY, data), accessExpr));
                break;
            default:
                OperatorNode op = new OperatorNode(expression.getText());
                expr.appendChild(op);
                for (int i = 0; i < expression.getChildCount(); ++i) {
                    AplTree childExpression = expression.getChild(i);
                    ExpressionNode childExpr = parseExpression(childExpression);
                    expr.appendChild(childExpr);
                }
        }
        return expr;
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
