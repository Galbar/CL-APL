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

public abstract class AplTreeWalker {
    private AplTree root;
    private AplTree current;

    private OutputStream output;

    public AplTreeWalker(AplTree root) {
        assert root != null;
        this.root = root;
        this.output = System.err;

    }

    public AplTreeWalker(AplTree root, OutputStream out) {
        assert root != null;
        this.root = root;
        output = out;
    }

    public boolean walk() {
        boolean ret = walk(root);
        try {
            output.flush();
        }
        catch (IOException e){};
        return ret;
    }

    private boolean walk(AplTree current) {
        this.current = current;
        if (!onNodeStart(current))
            return false;
        for (int i = 0; i < current.getChildCount(); ++i) {
            if (!walk(current.getChild(i)))
                return false;
        }
        this.current = current;
        if (!onNodeEnd(current))
            return false;
        return true;
    }

    protected abstract boolean onNodeStart(AplTree node);
    protected abstract boolean onNodeEnd(AplTree node);

    public void print(String message) {
        try {
            String line = (new Integer(this.current.getLine())).toString();
            output.write((line + ": " + message + "\n").getBytes());
        }
        catch (IOException e){};
    }
}
