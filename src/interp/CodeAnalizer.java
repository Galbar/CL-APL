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

public class CodeAnalizer {

    Stack stack;

    public CodeAnalizer(AplTree root) {
        super(root);
        stack = new Stack();

        stack.defineVariable("argc", new Data(Data.Type.INT));
        stack.defineVariable("argv", new Data(Data.Type.ARRAY_CHAR));
    }

    @Override
    public boolean parse(AplTree node) {
        String signature = getFuncSignature(node);

        FunctionNode function = new FunctionNode(signature);

        return true;
    }

    protected String getFuncSignature(AplTree func) {
        if (func.getType() != AplLexer.FUNC) {
            print("Node isn't a function root node. Can't get function signature from it");
            return "";
        }

        String funcName = func.getChild(0).getText();
        StringBuilder signatureBuild = new StringBuilder();
        signatureBuild.append(funcName);
        signatureBuild.append(" ( ");

        AplTree params = node.getChild(1);
        for (int i = 0; i < params.getChildCount(); ++i) {
            Data param = stack.getVariable(params.getChild(i).getText());
            if (i != 0) {
                signatureBuild.append(", ");
            }
            signatureBuild.append(param.typeToString());
        }

        signatureBuild.append(" )");

        return signatureBuild.toString();
    }

    @Override
    protected boolean onNodeEnd(AplTree node) {
        return true;
    }

    public void print(String message) {
        try {
            output.write((message + "\n").getBytes());
        }
        catch (IOException e){};
    }

    Stack getContext() {
        return stack;
    }
}
