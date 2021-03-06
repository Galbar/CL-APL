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
import java.util.HashSet;

import parser.AplLexer;

public class CodeAnalyzer {
    private AplTree root;
    private Stack stack;
    private int linenumber;
    private ArrayList<FunctionNode> funcTable;
    private FunctionNode currentFunction;

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

        boolean ret = true;
        try {
            parseFunction(mainNode);
        } catch (AplException e) {
            print("Line " + Integer.toString(lineNumber()) + ": " + e.getMessage());
            ret = false;
        }
        stack.popActivationRecord();

        if (!ret) print(stack.getStackTrace(lineNumber()));
        return ret;
    }

    public FunctionNode parseFunction(AplTree node) throws AplException {
        FunctionNode prevFuncNode = currentFunction;
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

        currentFunction = function;

        AplTree listInstr = node.getChild(2);

        for (int i = 0; i < listInstr.getChildCount(); ++i) {
            AplTree instr = listInstr.getChild(i);
            CodeNode instrNode = parseInstruction(instr, function);
            if (instrNode != null) {
                function.appendChild(instrNode);
            }
        }

        currentFunction = prevFuncNode;
        return function;
    }

    protected AplTree findFunction(String name) throws AplException {
        for (int i = 0; i < root.getChildCount(); ++i) {
            if (root.getChild(i).getChild(0).getText().equals(name)) return root.getChild(i);
        }
        throw new AplException("Function " + name + " not defined.");
    }

    protected boolean inParallel(AplTree node) {
        while (node.getParent().getType() != AplLexer.FUNC && node.getParent().getType() != AplLexer.PARALLEL) {
            node = node.getParent();
        }
        return node.getParent().getType() == AplLexer.PARALLEL;
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
                    if (data.isReference()) {
                        data = new Data(data);
                    }

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        varID = stack.setArrayElement(varname, data);
                    } else {
                        varID = stack.defineVariable(varname, data);
                    }

                    Data varData = stack.getVariable(varID);
                    CodeNode var;

                    if (node.getChild(0).getType() == AplLexer.IDARR) {
                        ExpressionNode delta = parseExpression(node.getChild(0).getChild(1));
                        var = new ArrayAccessNode(new VariableNode(varID, varData), varData.getSubData(), delta);
                    } else {
                        var = new VariableNode(varID, varData);
                    }

                    if (stack.isShared(varID)) {
                        retval = new CriticalNode(new AssignNode(var, expr));
                    } else {
                        retval = new AssignNode(var, expr);
                    }
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
                    if (!inParallel(node) && node.getType() == AplLexer.PFOR) throw new AplException("It is prohibited to declare a parallel for outisde of a parallel block.");
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
                        var = new ArrayAccessNode(new VariableNode(varID, varData), varData.getSubData(), delta);
                    } else {
                        var = new VariableNode(varID, varData);
                    }

                    retval.appendChild(var);
                    retval.appendChild(init);
                    retval.appendChild(size);

                    ParallelReductionNode red = new ParallelReductionNode();
                    ArrayList<Boolean> state = new ArrayList<Boolean>();
                    if (node.getChildCount() == 5) {
                        AplTree redParams = node.getChild(3);

                        OperatorNode op = new OperatorNode(redParams.getChild(0).getText());
                        red.appendChild(op);

                        for (int i = 1; i < redParams.getChildCount(); ++i) {
                            int id = stack.getVariableID(redParams.getChild(i).getChild(0).getText());
                            if (stack.isShared(id)) state.add(new Boolean(true));
                            else state.add(new Boolean(false));
                            stack.setShared(id, new Boolean(false));
                            red.appendChild(new VariableNode(id, stack.getVariable(id)));
                        }
                    }

                    retval.appendChild(red);

                    for (int i = 0; i < listInstr.getChildCount(); ++i) {
                        AplTree instr = listInstr.getChild(i);
                        CodeNode instrNode = parseInstruction(instr, function);
                        if (instrNode != null) {
                            block.appendChild(instrNode);
                        }
                    }

                    retval.appendChild(block);

                    if (node.getChildCount() == 5) {
                        AplTree redParams = node.getChild(3);

                        for (int i = 1; i < redParams.getChildCount(); ++i) {
                            int id = stack.getVariableID(redParams.getChild(i).getChild(0).getText());
                            stack.setShared(id, state.get(i-1));
                        }
                    }
                }
                break;
            case AplLexer.READ:
            case AplLexer.WRITE:
            case AplLexer.FUNCALL:
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
            case AplLexer.PARALLEL:
                {
                    if (inParallel(node)) throw new AplException("It is prohibited to declare a parallel block inside another parallel block.");

                    retval = new ParallelNode();
                    AplTree sharedParams = null;

                    for (int k = 0; k < node.getChildCount()-1; ++k) {
                        AplTree currChild = node.getChild(k);

                        switch (currChild.getType()) {
                            case AplLexer.SHARED:
                                {
                                    sharedParams = currChild.getChild(0);
                                    ParallelDefNode sharedList = new ParallelDefNode("shared");
                                    for (int i = 0; i < sharedParams.getChildCount(); ++i) {
                                        int id = stack.getVariableID(sharedParams.getChild(i).getChild(0).getText());
                                        stack.setShared(id, new Boolean(true));
                                        sharedList.appendChild(new VariableNode(id, stack.getVariable(id)));
                                    }
                                    retval.appendChild(sharedList);
                                }
                                break;
                            case AplLexer.PRIVATE:
                                {
                                    AplTree privateParams = currChild.getChild(0);
                                    ParallelDefNode privateList = new ParallelDefNode("private");
                                    for (int i = 0; i < privateParams.getChildCount(); ++i) {
                                        int id = stack.getVariableID(privateParams.getChild(i).getChild(0).getText());
                                        privateList.appendChild(new VariableNode(id, stack.getVariable(id)));
                                    }
                                    retval.appendChild(privateList);
                                }
                                break;
                            case AplLexer.NUMTHREADS:
                                {
                                    ParallelDefNode numThreads = new ParallelDefNode("num_threads");
                                    numThreads.appendChild(parseExpression(currChild.getChild(0)));
                                    retval.appendChild(numThreads);
                                }
                        }
                    }

                    AplTree listInstr = node.getChild(node.getChildCount()-1);
                    BlockInstrNode block = new BlockInstrNode();

                    for (int i = 0; i < listInstr.getChildCount(); ++i) {
                        AplTree instr = listInstr.getChild(i);
                        CodeNode instrNode = parseInstruction(instr, function);
                        if (instrNode != null) {
                            block.appendChild(instrNode);
                        }
                    }

                    retval.appendChild(block);

                    if (sharedParams != null) {
                        for (int i = 0; i < sharedParams.getChildCount(); ++i) {
                            stack.setShared(stack.getVariableID(sharedParams.getChild(i).getChild(0).getText()), new Boolean(false));
                        }
                    }
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
        }
        return retval;
    }

    protected ExpressionNode parseExpression(AplTree expression) throws AplException {
        ExpressionNode expr = new ExpressionNode();
        int id;
        switch (expression.getType()) {
            case AplLexer.EXPRGROUP:
                expr = parseExpression(expression.getChild(0));
                expr.makeGroup();
                break;
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
                        var = new ArrayAccessNode(new VariableNode(varID, varData), varData.getSubData(), delta);
                    } else {
                        var = new VariableNode(varID, varData);
                    }

                    ReadNode retval = new ReadNode(var);

                    // from <string>
                    if (expression.getChildCount() == 2) {
                        retval.appendChild(parseExpression(expression.getChild(1)));
                    }

                    if (stack.isShared(varID)) {
                        expr.appendChild(new CriticalNode(retval));
                    } else {
                        expr.appendChild(retval);
                    }
                }
                break;
            case AplLexer.WRITE:
                {
                    CodeNode retval = new WriteNode(parseExpression(expression.getChild(0)));

                    // to <string>
                    if (expression.getChildCount() == 2) {
                        retval.appendChild(parseExpression(expression.getChild(1)));

                        if (expression.getChild(1).getType() != AplLexer.IDARR
                            && stack.isShared(stack.getVariableID(expression.getChild(1).getText()))) {
                            retval = new CriticalNode(retval);
                        }
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
                    } else if (stack.getVariable(stack.getVariableID(expression.getChild(0).getText())).getType() == Data.Type.ARRAY){
                        id = stack.getVariableID(expression.getChild(0).getText());
                        data = stack.getVariable(id);
                        data.resolve();
                        expr.appendChild(new ArrayAccessNode(new VariableNode(id, data), data.getSubData(), accessExpr));
                        break;
                    } else {
                        id = stack.getVariableID(expression.getChild(0).getText());
                        throw new AplException("Accessing an element in an array of a variable `" + expression.getChild(0).getText() + "` of type `" + stack.getVariable(id).typeToString() +"`");
                    }
                    expr.appendChild(new ArrayNode(new Data(Data.Type.ARRAY, data), accessExpr));
                    break;
                }
            case AplLexer.FUNCALL:
                {
                    String funcName = expression.getChild(0).getText();
                    AplTree params = expression.getChild(1);
                    if (params.getChildCount() == 0 && funcName.equals("get_num_threads")) {
                        expr.appendChild(new ConstantNode("omp_get_num_threads()", new Data(Data.Type.INT)));
                        break;
                    } else if (params.getChildCount() == 0 && funcName.equals("get_thread_num")) {
                        expr.appendChild(new ConstantNode("omp_get_thread_num()", new Data(Data.Type.INT)));
                        break;
                    }

                    AplTree func = findFunction(funcName);

                    if (params.getChildCount() != func.getChild(1).getChildCount()) {
                        throw new AplException("Calling function `" + funcName + "` with a wrong number of parameters.");
                    }

                    ArrayList<Data> paramData = new ArrayList<Data>();
                    ArrayList<ExpressionNode> exprs = new ArrayList<ExpressionNode>();
                    for (int i = 0; i < params.getChildCount(); ++i) {
                        ExpressionNode paramExpr = parseExpression(params.getChild(i));
                        paramExpr.getData().resolve();
                        paramData.add(paramExpr.getData());
                        exprs.add(paramExpr);
                    }

                    FunctionNode funcNode = currentFunction;
                    boolean found = false;
                    FunctionNode temp = new FunctionNode(funcName, paramData.size(), paramData);
                    String signature = temp.getSignature();

                    for (FunctionNode fn : funcTable) {
                        if (fn.getSignature().equals(signature)) {
                            funcNode = fn;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        stack.pushActivationRecord(funcName, lineNumber());

                        params = func.getChild(1);
                        for (int i = 0; i < params.getChildCount(); ++i) {
                            if (params.getChild(i).getType() == AplLexer.PREF) {
                                paramData.set(i, new Data(paramData.get(i)));
                                paramData.get(i).setReference(true);
                            }
                            stack.defineVariable(params.getChild(i).getChild(0).getText(), paramData.get(i));
                        }

                        funcNode = parseFunction(func);
                        stack.popActivationRecord();
                    }

                    expr.appendChild(new FunctionCallNode(funcNode, exprs));
                }
                break;
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
            System.err.write((message + "\n").getBytes());
        }
        catch (IOException e){};
    }

    public ArrayList<FunctionNode> getFunctionTable() { return funcTable; }

    Stack getContext() {
        return stack;
    }
}
