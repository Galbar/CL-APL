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

public class IfNode extends CodeNode {

    public IfNode() {
        super(null);
    }

    @Override
    public String toC() throws AplException {
        StringBuilder str = new StringBuilder();

        str.append("if (");
        str.append(getChild(0).toC());
        str.append(")\n");
        str.append(getChild(1).toC());

        int numBlocks, i;
        if (getNumChilds()%2 == 0) numBlocks = getNumChilds()/2;
        else numBlocks = (getNumChilds()-1)/2;

        for (i = 1; i < numBlocks; ++i) {
            str.append("else if (");
            str.append(getChild(2*i).toC());
            str.append(")\n");
            str.append(getChild(2*i+1).toC());
        }

        i = 2*i;
        if (i < getNumChilds()) {
            str.append("else\n");
            str.append(getChild(i).toC());
        }

        return str.toString();
    }
}
