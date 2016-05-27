/**
 * Copyright (c) 2011, Alessio Linares & Guillermo Ojeda
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
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  */

package interp;

/**
 * Class to represent data in the interpreter.
 * Each data item has a type and a value. The type can be integer
 * or Boolean. Each operation asserts that the operands have the
 * appropriate types.
 * All the arithmetic and Boolean operations are calculated in-place,
 * i.e., the result is stored in the same data.
 * The type VOID is used to represent void values on function returns.
 */

import parser.*;
import java.util.ArrayList;

public class Data {

    /** Types of data */
    public enum Type {
        VOID,
        CHAR,
        BOOL,
        INT,
        FLOAT,
        ARRAY_CHAR,
        ARRAY_BOOL,
        ARRAY_INT,
        ARRAY_FLOAT,
        FROM_SIGNATURE;
    }

    /** Type of data*/
    private Type type;
    private String funcSignature;

    /** Constructor for integers */
    Data(Type type) { this.type = type; }

    /** Constructor for void data */
    Data() {type = Type.VOID; }

    /** Copy constructor */
    Data(Data d) {
        type = d.type;
        funcSignature = d.funcSignature;
    }

    /** Returns the type of data */
    public Type getType() { return type; }

    /** Defines a Boolean value for the data */
    public void setFuncSignature(String sig) {
        assert isFromSignature();
        funcSignature = sig;
    }

    /** Returns the type of array */
    public String getFuncSignature() {
        assert isFromSignature();
        return funcSignature;
    }

    /** Indicates whether the data is Boolean */
    public boolean isFromSignature() { return type == Type.FROM_SIGNATURE; }

    public String typeToString() {
        switch(type) {
            case Type.VOID:
                return "void";
                break;
            case Type.INT:
                return "int";
                break;
            case Type.FLOAT:
                return "float";
                break;
            case Type.CHAR:
                return "char";
                break;
            case Type.BOOL:
                return "bool";
                break;
            case Type.ARRAY_INT:
                return "int*";
                break;
            case Type.ARRAY_FLOAT:
                return "float*";
                break;
            case Type.ARRAY_CHAR:
                return "char*";
                break;
            case Type.ARRAY_BOOL:
                return "bool*";
                break;
            default:
                return "unkown";
        }
    }
}
