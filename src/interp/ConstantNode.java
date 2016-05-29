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

public class ConstantNode extends CodeNode {
    String value;

    public ConstantNode(AplTree value)
    {
        super(null);
        this.value = value.getText();
        switch (value.getType()) {
            case AplLexer.INT:
                data = new Data(Data.Type.INT);
                break;
            case AplLexer.FLOAT:
                data = new Data(Data.Type.FLOAT);
                this.value = this.value + "f";
                break;
            case AplLexer.CHAR:
                data = new Data(Data.Type.CHAR);
                break;
            case AplLexer.STRING:
                data = new Data(Data.Type.ARRAY, new Data(Data.Type.CHAR));
                break;
            case AplLexer.BOOLEAN:
                data = new Data(Data.Type.BOOL);
                this.value = value.getText().equals("true") ? "1" : "0";
                break;
        }
    }

    public ConstantNode(String value, Data data) {
        super(null);
        this.value = value;
        this.data = data;
    }

    public void setConstant(String value, Data data) {
        this.value = value;
        this.data = data;
    }

    @Override
    public String toC() throws AplException { return value; }
}
