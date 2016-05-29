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
import java.util.ArrayList;

import parser.AplLexer;

public class CodeAnalyzer {
    private AplTree root;
    private Stack stack;
    private int linenumber;
    private ArrayList<FunctionNode> funcTable;

    public CodeAnalyzer(AplTree root) {
        this.root = root;
        stack = new Stack();
        funcTable = new ArrayList<FunctionNode>();
    }

    public boolean parse() {
        // Treat all the functions
        AplTree mainNode;
        try {
            mainNode = findFunction("main");
        } catch (AplException e) {
            print("Line " + Integer.toString(lineNumber()) + ": " + e.getMessage());
            return false;
        }

        setLineNumber(mainNode);
        stack.pushActivationRecord("main", lineNumber());

        boolean ret = parseFunction(mainNode);
        stack.popActivationRecord();
        
        if (!ret) print(stack.getStackTrace(lineNumber()));
        return ret;
    }

    public boolean parseFunction(AplTree node) {
        int numParams = node.getChild(1).getChildCount();
        String name = node.getChild(0).getText();
        if (name.equals("main")) {
            if (numParams >= 1) {
                stack.defineVariable("argc", new Data(Data.Type.INT));
            }
            if (numParams >= 2) {
                stack.defineVariable("argv", new Data(Data.Type.ARRAY, new Data(Data.Type.ARRAY, new Data(Data.Type.CHAR))));
            }
        }

        FunctionNode function = new FunctionNode(name, numParams, stack.getCurrentAR());
        funcTable.add(function);

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

        // Obtain type

        return true;
    }

    protected AplTree findFunction(String name) throws AplException {
        for (int i = 0; i < root.getChildCount(); ++i) {
            if (root.getChild(i).getChild(0).getText().equals(name)) return root.getChild(i);
        }
        throw new AplException("Function " + name + " not defined.");
    }

    protected CodeNode parseInstruction(AplTree node, FunctionNode function) throws AplException {
        CodeNode retval = null;
        setLineNumber(node);
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
                {
                    retval = new IfNode();

                    for (int k = 0; k < node.getChildCount(); ++k) {
                        if (node.getChild(k).getType() == AplLexer.LIST_INSTR) {
                            AplTree listInstr = node.getChild(k);
                            BlockInstrNode block = new BlockInstrNode();

                            for (int i = 0; i < listInstr.getChildCount(); ++i) {
                                AplTree instr = listInstr.getChild(i);
                                CodeNode instrNode = parseInstruction(instr, function);
                                if (instrNode != null) {
                                    block.appendChild(instrNode);
                                }
                            }

                            retval.appendChild(block);
                        } else {
                            ExpressionNode expr = parseExpression(node.getChild(k));

                            retval.appendChild(expr);
                        }
                    }
                }
                break;
            case AplLexer.WHILE:
                {
                    ExpressionNode expr = parseExpression(node.getChild(0));

                    AplTree listInstr = node.getChild(1);
                    BlockInstrNode block = new BlockInstrNode();

                    for (int i = 0; i < listInstr.getChildCount(); ++i) {
                        AplTree instr = listInstr.getChild(i);
                        CodeNode instrNode = parseInstruction(instr, function);
                        if (instrNode != null) {
                            block.appendChild(instrNode);
                        }
                    }

                    retval = new WhileNode(expr, block);
                }
                break;
            case AplLexer.PFOR:
            case AplLexer.FOR:
                {
                    retval = new ForNode(node.getType());
                    AplTree listInstr = node.getChild(node.getChildCount()-1);
                    BlockInstrNode block = new BlockInstrNode();

                    String varname;
                    int varID;

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        varname = node.getChild(0).getChild(0).getText();
                    } else {
                        varname = node.getChild(0).getText();
                    }

                    ExpressionNode init = parseExpression(node.getChild(1));
                    Data data = init.getData();
                    ExpressionNode size = parseExpression(node.getChild(2));

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

                    retval.appendChild(var);
                    retval.appendChild(init);
                    retval.appendChild(size);

                    for (int i = 0; i < listInstr.getChildCount(); ++i) {
                        AplTree instr = listInstr.getChild(i);
                        CodeNode instrNode = parseInstruction(instr, function);
                        if (instrNode != null) {
                            block.appendChild(instrNode);
                        }
                    }

                    retval.appendChild(block);
                }
                break;
            case AplLexer.READ:
            case AplLexer.WRITE:
                {
                    ExpressionNode expr = parseExpression(node);
                    expr.setInstruction();
                    retval = expr;
                }
                break;
            case AplLexer.FREE:
                {
                    String varname;
                    int varID;

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        throw new AplException("(Error) Line " + Integer.toString(node.getLine()) + ": Freed element must be an array.");
                    } else {
                        varname = node.getChild(0).getText();
                    }

                    try {
                        varID = stack.getVariableID(varname);
                    } catch (AplException e) {
                        throw new AplException("(Error) Line " + Integer.toString(node.getLine()) + ": Freed array `" + varname + "` is not defined.");
                    }
                    retval = new FreeNode(varID, stack.getVariable(varID));
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
                }
                break;
            case AplLexer.FUNCALL:
                {
                    String funcName = node.getChild(0).getText();
                    AplTree func = findFunction(funcName);
                    AplTree params = node.getChild(1);

                    ArrayList<Data> paramData = new ArrayList<Data>();
                    for (int i = 0; i < params.getChildCount(); ++i) {
                        ExpressionNode expr = parseExpression(params.getChild(i));
                        expr.getData().resolve();
                        paramData.add(expr.getData());
                    }   

                    stack.pushActivationRecord(funcName, lineNumber());
                    
                    params = func.getChild(1);
                    for (int i = 0; i < params.getChildCount(); ++i) {
                        stack.defineVariable(params.getChild(i).getChild(0).getText(), paramData.get(i));
                    }

                    boolean ret = parseFunction(func);
                    stack.popActivationRecord();
                }
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
            case AplLexer.READ:
                {
                    String varname;
                    int varID;

                    if (expression.getChild(0).getType() == AplLexer.IDARR) {
                        varname = expression.getChild(0).getChild(0).getText();
                    } else {
                        varname = expression.getChild(0).getText();
                    }

                    try {
                        varID = stack.getVariableID(varname);
                    } catch (AplException e) {
                        if (expression.getChild(0).getType() == AplLexer.IDARR) {
                            throw new AplException("Reading to an element of a not defined array.");
                        }
                        print("Warning: variable with name `" + varname + "` not defined before reading. Assuming int.");
                        varID = stack.defineVariable(varname, new Data(Data.Type.INT));
                    }

                    Data varData = stack.getVariable(varID);
                    CodeNode var;

                    if (expression.getChild(0).getType() == AplLexer.IDARR) {
                        ExpressionNode delta = parseExpression(expression.getChild(0).getChild(1));
                        var = new ArrayAccessNode(varID, varData.getSubData(), delta);
                    } else {
                        var = new VariableNode(varID, varData);
                    }

                    ReadNode retval = new ReadNode(var);

                    // from <string>
                    if (expression.getChildCount() == 2) {
                        retval.appendChild(parseExpression(expression.getChild(1)));
                    }

                    expr.appendChild(retval);
                }
                break;
            case AplLexer.WRITE:
                {
                    WriteNode retval = new WriteNode(parseExpression(expression.getChild(0)));

                    // to <string>
                    if (expression.getChildCount() == 2) {
                        retval.appendChild(parseExpression(expression.getChild(1)));
                    }
                    expr.appendChild(retval);
                }
                break;
            case AplLexer.IDARR:
                {
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
                }
            default:
                {
                    OperatorNode op = new OperatorNode(expression.getText());
                    expr.appendChild(op);
                    for (int i = 0; i < expression.getChildCount(); ++i) {
                        AplTree childExpression = expression.getChild(i);
                        ExpressionNode childExpr = parseExpression(childExpression);
                        expr.appendChild(childExpr);
                    }
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

    public ArrayList<FunctionNode> getFunctionTable() { return funcTable; }

    Stack getContext() {
        return stack;
    }
}
