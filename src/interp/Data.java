/**
 * Copyright (c) 2011, Jordi Cortadella
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

    /** Default values for filling arrays */
    private static final Data default_bool = new Data(false);
    private static final Data default_int = new Data(0);

    /** Types of data */
    public enum Type {VOID, BOOLEAN, INTEGER, FLOAT, ARRAY;}

    /** Type of data*/
    private Type type;

    /** Type of array data*/
    private Type array_type;

    /** Value of the data */
    private float value;

    /** Value of the array data */
    private ArrayList<Data> array_value = null;

    /** Constructor for integers */
    Data(int v) { type = Type.INTEGER; value = v; }

    /** Constructor for integers */
    Data(float v) { type = Type.FLOAT; value = v; }

    /** Constructor for Booleans */
    Data(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; }

    /** Constructor for Array */
    Data(Data v, int position) {
        type = Type.ARRAY;
        array_type = v.type;
        array_value = new ArrayList<Data>();
        Data default_value;
        if (v.isBoolean()) {
            default_value = default_bool;
        } else if (v.isInteger()) {
            default_value = default_int;
        } else {
            throw new RuntimeException ("Invalid array type");
        }

        for (int i = 0; i < position; ++i) {
            array_value.add(default_value);
        }
        array_value.add(v);
    }

    /** Constructor for void data */
    Data() {type = Type.VOID; }

    /** Copy constructor */
    Data(Data d) {
        type = d.type;
        value = d.value;
        array_type = d.array_type;
        array_value = d.array_value;
    }

    /** Returns the type of data */
    public Type getType() { return type; }

    /** Returns the type of array */
    public Type getArrayType() {
        assert isArray();
        return array_type;
    }

    /** Indicates whether the data is Boolean */
    public boolean isBoolean() { return type == Type.BOOLEAN; }

    /** Indicates whether the data is integer */
    public boolean isInteger() { return type == Type.INTEGER; }

    /** Indicates whether the data is void */
    public boolean isVoid() { return type == Type.VOID; }

    /** Indicates whether the data is array */
    public boolean isArray() { return type == Type.ARRAY; }

    /**
     * Gets the value of an integer data. The method asserts that
     * the data is an integer.
     */
    public int getIntegerValue() {
        assert type == Type.INTEGER;
        return (int)value;
    }

    /**
     * Gets the value of a Boolean data. The method asserts that
     * the data is a Boolean.
     */
    public boolean getBooleanValue() {
        assert type == Type.BOOLEAN;
        return value == 1;
    }

    /**
     * Gets the value of an array data. The method asserts that
     * the data is an array.
     */
    public Data getArrayValue(int position) {
        assert type == Type.ARRAY;
        if (position >= array_value.size()) {
            throw new RuntimeException ("Index out-of-bounds");
        }
        return array_value.get(position);
    }

    /** Defines a Boolean value for the data */
    public void setValue(boolean b) { type = Type.BOOLEAN; value = b ? 1 : 0; array_value = null; }

    /** Defines an integer value for the data */
    public void setValue(int v) { type = Type.INTEGER; value = v; array_value = null; }

    /** Copies the value from another data */
    public void setData(Data d) {
        type = d.type;
        value = d.value;
        array_value = d.array_value;
        array_type = d.array_type;
    }

    /** Defines an integer value for the data */
    public void setArray(Data v, int position) {
        int size = 0;
        Data default_value;
        if (v.isBoolean()) {
            default_value = default_bool;
        } else if (v.isInteger()) {
            default_value = default_int;
        } else {
            throw new RuntimeException ("Invalid array type");
        }

        if (isArray() && array_type == v.type) {
            size = array_value.size();
        } else {
            type = Type.ARRAY;
            array_type = v.type;
            array_value = new ArrayList<Data>();
        }

        for (int i = size; i < position; ++i) {
            array_value.add(default_value);
        }
        array_value.add(position, v);
    }

    /** Returns a string representing the data in textual form. */
    public String toString() {
        if (isArray()) {
            String str = "[";
            for (int i = 0; i < array_value.size(); ++i) {
                if (i != 0) { str = str.concat(", "); }
                str = str.concat(array_value.get(i).toString());
            }
            return str.concat("]");
        }
        if (type == Type.BOOLEAN) return value == 1 ? "true" : "false";
        return Float.toString(value);
    }

    /**
     * Checks for zero (for division). It raises an exception in case
     * the value is zero.
     */
    private void checkDivZero(Data d) {
        if (d.value == 0) throw new RuntimeException ("Division by zero");
    }

    /**
     * Evaluation of arithmetic expressions. The evaluation is done
     * "in place", returning the result on the same data.
     * @param op Type of operator (token).
     * @param d Second operand.
     */

    public void evaluateArithmetic (int op, Data d) {
        assert type == Type.INTEGER && d.type == Type.INTEGER;
        switch (op) {
            case AplLexer.PLUS: value += d.value; break;
            case AplLexer.MINUS: value -= d.value; break;
            case AplLexer.MUL: value *= d.value; break;
            case AplLexer.DIV: checkDivZero(d); value /= d.value; break;
            case AplLexer.MOD: checkDivZero(d); value %= d.value; break;
            default: assert false;
        }
    }

    /**
     * Evaluation of expressions with relational operators.
     * @param op Type of operator (token).
     * @param d Second operand.
     * @return A Boolean data with the value of the expression.
     */
    public Data evaluateRelational (int op, Data d) {
        assert type != Type.VOID && type == d.type;
        switch (op) {
            case AplLexer.EQUAL: return new Data(value == d.value);
            case AplLexer.NOT_EQUAL: return new Data(value != d.value);
            case AplLexer.LT: return new Data(value < d.value);
            case AplLexer.LE: return new Data(value <= d.value);
            case AplLexer.GT: return new Data(value > d.value);
            case AplLexer.GE: return new Data(value >= d.value);
            default: assert false;
        }
        return null;
    }
}
