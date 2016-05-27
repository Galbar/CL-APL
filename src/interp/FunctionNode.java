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

import java.util.ArrayList;
import java.lang.StringBuilder;

public class FunctionNode extends CodeNode {
    private String name;
    private ArrayList<Data> variables;
    private int numParams;
    private Data type;

    public FunctionNode(String name, int numParams, ArrayList<Data> variables)
    {
        super(null);
        this.name = name;
        this.numParams = numParams;
        this.variables = variables;
        this.type = new Data();
    }

    public Data getData() { return type; }

    @Override
    public String toC(FunctionTable table) {
        StringBuilder str = new StringBuilder();
        table.resolveType(type);
        str.append(type.typeToString());
        str.append(" ");
        str.append(name);
        str.append(" (");
        for (int i = 0; i < numParams; ++i) {
            Data dVar = variables.get(i);
            table.resolveType(dVar);
            if (i != 0) str.append(", ");
            str.append(dVar.typeToString());
            str.append(" var");
            str.append((new Integer(i)).toString());
        }
        str.append(")\n{\n");
        
        // Variable definitions
        for (int i = numParams; i < variables.size(); ++i) {
            Integer key = new Integer(i);
            Data value = variables.get(i);
            table.resolveType(value);
            str.append(value.typeToString());
            str.append(" var");
            str.append(key.toString());
            str.append(";\n");
        }

        // Intructions
        for (int i = 0; i < getNumChilds(); ++i) {
            str.append(getChild(i).toC(table));
        }

        str.append("}\n");

        return str.toString();
    }
}

