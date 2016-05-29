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
        ARRAY,
        FROM_DEPENDENCIES;
    }

    /** Type of data*/
    private Type type;
    private Data subData = null;
    private ArrayList<Data> dependencies;

    /** Constructor for integers */
    Data(Type type) { assert type != Type.ARRAY; this.type = type; }

    /** Constructor for integers */
    Data(Type type, Data subData) { assert subData != null; this.type = type; this.subData = subData; }

    /** Constructor for void data */
    Data() {type = Type.VOID; }

    /** Copy constructor */
    Data(Data d) { setData(d); }

    /** Returns the type of data */
    public Type getType() { return type; }
    public void setData(Data d) {
        if (d == null) {
            type = Type.VOID;
            subData = null;
            dependencies = null;
            return;
        }
        type = d.type;
        subData = new Data(d.subData);
        if (d.dependencies != null) {
            dependencies = new ArrayList<Data>();
            for (Data dep : d.dependencies) {
                dependencies.add(new Data(dep));
            }
        }
    }
    public Data getSubData() { return subData; }

    public boolean hasDependencies() {
        return dependencies != null;
    }

    public void resolve() {
        if (dependencies == null) return;
        ArrayList<Data> newDeps = null;
        Data newData = null;
        for (Data dep : dependencies) {
            if (newData == null) {
                newData = dep;
                continue;
            }

            if (dep.hasDependencies()) {
                if (newDeps == null) {
                    newDeps = new ArrayList<Data>();
                }
                newDeps.add(dep);
            } else {
                newData = max(newData, dep);
            }
        }
        if (newDeps == null) {
            dependencies = null;
            setData(newData);
        } else {
            dependencies = newDeps;
            type = Type.FROM_DEPENDENCIES;
            dependencies.add(newData);
        }
    }

    public void addDependency(Data dependecy) {
        ArrayList<Data> deps = new ArrayList<Data>();
        if (type != Type.FROM_DEPENDENCIES) {
            deps.add(new Data(this));
            type = Type.FROM_DEPENDENCIES;
        }
        deps.add(dependecy);
        dependencies = deps;
        resolve();
    }

    public String typeToString() {
        switch(type) {
            case VOID:
                return "void";
            case INT:
                return "int";
            case FLOAT:
                return "float";
            case CHAR:
                return "char";
            case BOOL:
                return "int";
            case ARRAY:
                return subData.typeToString() + "*";
            default:
                return "unknown";
        }
    }

    static public Data max(Data d1, Data d2) {
        Data.Type t1 = d1.getType();
        Data.Type t2 = d2.getType();
        if (t1 == Data.Type.VOID) return d2;
        if (t2 == Data.Type.VOID) return d1;
        if (t1 == Data.Type.FLOAT) return d1;
        if (t2 == Data.Type.FLOAT) return d2;
        if (t1 == Data.Type.FROM_DEPENDENCIES || t2 == Data.Type.FROM_DEPENDENCIES) {
            Data data = new Data(Data.Type.FROM_DEPENDENCIES);
            data.addDependency(d1);
            data.addDependency(d2);
        }
        if (t1 != t2) return new Data(Data.Type.INT);
        if (t1 == Data.Type.ARRAY && t2 == Data.Type.ARRAY && d1.getSubData().getType() != d2.getSubData().getType()) return new Data(Type.ARRAY, new Data(Type.VOID));
        return d1;
    }
}
