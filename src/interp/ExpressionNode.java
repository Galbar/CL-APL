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

import java.lang.StringBuilder;


public class ExpressionNode extends CodeNode {
    // TODO: getData que se deduce de los hijos

    public ExpressionNode()
    {
        super(null);
    }

    public Data getData() {
        if (data.getType() != Data.Type.VOID) { return data; }

        if (getNumChilds() == 1) {
            data = getChild(0).getData();
            return data;
        } else {
            String op = getChild(0).toC();
            if (op.equals("==") || op.equals("!=")
                || op.equals("<=") || op.equals(">=")
                || op.equals("<") || op.equals(">")
                || op.equals("||") || op.equals("&&")
                || op.equals("!")) {
                data = new Data(Data.Type.BOOL);
                return data;
            } else if (getNumChilds() == 2) {
                data = getChild(1).getData();
                return data;
            } else {
                data = Data.max(getChild(1).getData(), getChild(2).getData());
                return data;
            }
        }
    }

    @Override
    public String toC() {
        StringBuilder str = new StringBuilder();
        switch (getNumChilds()) {
            case 1:
                str.append(getChild(0).toC());
                break;
            case 2:
                str.append(getChild(0).toC());
                str.append(" ");
                str.append(getChild(1).toC());
                break;
            default:
                str.append(getChild(1).toC());
                str.append(" ");
                str.append(getChild(0).toC());
                str.append(" ");
                str.append(getChild(2).toC());
        }
        return str.toString();
    }
}
