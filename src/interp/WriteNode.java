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
import java.lang.StringBuilder;

public class WriteNode extends CodeNode {
    ExpressionNode expr;

    public WriteNode(ExpressionNode expr) {
        super(null);
        this.expr = expr;
        appendChild(expr);
        this.data = new Data(Data.Type.INT);
    }

    @Override
    public String toC() throws AplException {
        StringBuilder str = new StringBuilder();

        if (getNumChilds() == 1) {
            str.append("printf(\"");
        } else {
            str.append("sprintf(");
            getChild(1).getData().resolve();
            if (getChild(1).getData().getType() != Data.Type.ARRAY
                && getChild(1).getData().getSubData().getType() != Data.Type.CHAR) {
                throw new AplException("Writing to a variable that is not a string.");
            }
            str.append(getChild(1).toC());
            str.append(", \"%s");
        }

        switch(this.expr.getData().getType()) {
            case VOID:
                str.append("%s");
                break;
            case CHAR:
                str.append("%c");
                break;
            case BOOL:
                str.append("%s");
                break;
            case INT:
                str.append("%i");
                break;
            case FLOAT:
                str.append("%f");
                break;
            case ARRAY:
                if (this.expr.getData().getSubData().getType() == Data.Type.CHAR) {
                    str.append("%s");
                } else {
                    str.append("%i");
                }
                break;
        }

        if (getNumChilds() == 1) {
            str.append("\\n\", ");
        } else {
            str.append("\", ");
            str.append(getChild(1).toC());
            str.append(", ");
        }

        switch(this.expr.getData().getType()) {
            case VOID:
                str.append("\"void\"");
                break;
            case CHAR:
                str.append(this.expr.toC());
                break;
            case BOOL:
                str.append(this.expr.toC());
                str.append(" ? \"true\" : \"false\"");
                break;
            case INT:
                str.append(this.expr.toC());
                break;
            case FLOAT:
                str.append(this.expr.toC());
                break;
            case ARRAY:
                if (this.expr.getData().getSubData().getType() == Data.Type.CHAR) {
                    str.append(this.expr.toC());
                } else {
                    str.append("(int)");
                    str.append(this.expr.toC());
                }
                break;
        }
        str.append(")");
        return str.toString();
    }
}
