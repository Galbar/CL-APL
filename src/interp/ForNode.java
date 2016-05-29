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

import parser.*;
import java.util.ArrayList;
import java.lang.StringBuilder;

public class ForNode extends CodeNode {
    int type;

    public ForNode(int type) {
        super(null);
        this.type = type;
    }

    @Override
    public String toC() {
        StringBuilder str = new StringBuilder();

        int depth = 0;
        CodeNode curr = this;
        while (curr.getParent() != null) {
            curr = curr.getParent();
            ++depth;
        }

        if (type == AplLexer.PFOR) {
            str.append("#pragma parallel HIHI");
        }

        String it, init, size;
        str.append("for (");
        if (getNumChilds() == 4) {
            it = getChild(0).toC();
            init = getChild(1).toC();
            size = getChild(2).toC();
        } else {
            str.append("int ");
            it = "i" + Integer.toString(depth);
            init = "0";
            size = "sizeof(" + getChild(1).toC() + ")/sizeof(" 
                + getChild(1).getData().getSubData().typeToString() + ")";
        }

        str.append(it);
        str.append(" = ");
        str.append(init);
        str.append("; ");
        str.append(it);
        str.append(" < ");
        str.append(size);
        str.append("; ++");
        str.append(it);
        str.append(")\n");
        str.append(getChild(getNumChilds()-1).toC().replaceAll("\\{it\\}", it));

        return str.toString();
    }
}