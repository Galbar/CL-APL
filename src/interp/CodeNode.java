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

public abstract class CodeNode {
    private CodeNode parent;
    private CodeNode down = null;
    private CodeNode right = null;
    private int childCount = 0;
    protected Data data = new Data(Data.Type.VOID);

    public CodeNode(CodeNode parent)
    {
        this.parent = parent;
    }

    public CodeNode getParent()
    {
        return parent;
    }

    public int getNumChilds()
    {
        return childCount;
    }

    public CodeNode getChild(int i)
    {
        CodeNode result = down;
        for (int j = 0; j < i; ++j) {
            result = result.right;
        }
        return result;
    }

    public void appendChild(CodeNode child)
    {
        if (down == null) {
            down = child;
        }
        else {
            CodeNode result = down;
            while (result.right != null) {
                result = result.right;
            }
            result.right = child;
        }

        child.parent = this;
        childCount++;
    }

    public Data getData() { return data; }
    public abstract String toC();
}

