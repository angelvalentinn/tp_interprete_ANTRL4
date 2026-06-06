package com.tpinterprete.simple.interprete;

import java.util.HashMap;

//Clase donde se aplica la gramatica de atributos a cada nodo del arbol, usando la tabla de simbolos
public class SimpleCustomVisitor extends SimpleBaseVisitor<Object> {

    private HashMap<String, Object> symbolTable = new HashMap<String, Object>();

    @Override
    public Object visitVar_decl(SimpleParser.Var_declContext ctx) {

        if(symbolTable.containsKey(ctx.ID().getText()))
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Redeclaración de variable, estas intentando definir una variable ya definida.");

        String variableName = ctx.ID().getText(); //Guardo el nombre de la variable

        this.symbolTable.put(variableName, 0); //Lo guardo en la tabla de simbolos con valor 0 por defecto

        return null;
    }

    @Override
    public Object visitVar_assign(SimpleParser.Var_assignContext ctx) {

        if(!symbolTable.containsKey(ctx.ID().getText()))
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Declaración de variable, estas intentando asignar un valor a una variable no definida.");

        String id = ctx.ID().getText();
        Object value = visit(ctx.expression());

        symbolTable.put(id, value);

        return value;

    }

    @Override
    public Object visitPrint(SimpleParser.PrintContext ctx) {

        //Si recibe un NO terminal, se visita hasta que quede en un terminal
        Object res = visit(ctx.expression());

        if(res == null) throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                "ERROR: Variable sin declarar.");

        if(res instanceof Boolean) {
            boolean valor = (Boolean) res;
            System.out.println(valor ? "verdadero" : "falso");
        } else {
            System.out.println(res);

        }

        return null;
    }


    @Override
    public Object visitId(SimpleParser.IdContext ctx) {

        String id = ctx.ID().getText();
        if(symbolTable.containsKey(id)) return symbolTable.get(id);
        return null;

    }

    @Override
    public Object visitNumb(SimpleParser.NumbContext ctx) {

        //Este metodo se ejecuta cuando se llega al final del arbol para obtener el float o int
        if(ctx.number().getText().contains(".")) {
            return Double.parseDouble(ctx.number().getText());
        }
        return Integer.parseInt(ctx.number().getText());
    }

    @Override
    public Object visitStr(SimpleParser.StrContext ctx) {
        //Este metodo se ejecuta cuando se llega al final del arbol para obtener el terminal STRING
        return ctx.STRING();
    }

    @Override
    public Object visitBool(SimpleParser.BoolContext ctx) {
        //Este metodo se ejecuta cuando se llega al final del arbol para obtener el terminal BOOLEAN
        return ctx.BOOLEAN();
    }


    @Override
    public Object visitMulDiv(SimpleParser.MulDivContext ctx) {

        //Este metodo se ejecuta se quiere multiplicar/dividir expresiones, en caso de que las expresiones sean otras
        //expresiones, se visita recursivamente hasta obtener el terminal numero, luego se opera

        Number left = (Number) visit(ctx.expression(0)); //Si el valor que viene es Double, Number lo instancia como Double, sino Integer
        Number right = (Number) visit(ctx.expression(1)); //Integer y Double heredan de Number

        if(right.doubleValue() == 0.0) throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                "ERROR: Operación invalida, no se puede dividir por 0.");

        if(ctx.op.getType() == SimpleParser.DIV) {

            return left.doubleValue() / right.doubleValue();

        } else if(left instanceof Double || right instanceof Double) {

            return left.doubleValue() * right.doubleValue();

        }

        return left.intValue() * right.intValue();

    }

    @Override
    public Object visitAddSub(SimpleParser.AddSubContext ctx) {

        //Este metodo se ejecuta se quiere sumar/restar expresiones, en caso de que las expresiones sean otras
        //expresiones, se visita recursivamente hasta obtener el terminal numero, luego se opera
        Number left = (Number) visit(ctx.expression(0));
        Number right = (Number) visit(ctx.expression(1));

        if(left instanceof Double || right instanceof Double) {

            if(ctx.op.getType() == SimpleParser.PLUS) {
                return left.doubleValue() + right.doubleValue();
            } else {
                return left.doubleValue() - right.doubleValue();
            }

        }

        if(ctx.op.getType() == SimpleParser.PLUS) return left.intValue() + right.intValue();

        return left.intValue() - right.intValue();

    }

    @Override
    public Object visitComp(SimpleParser.CompContext ctx)   {

        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        if( !(left instanceof Double || left instanceof Integer)
            || !(right instanceof Double || right instanceof Integer)
        ) { //Si no son numeros lanzamos error
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: No coinciden los tipos, los operandos deben ser numericos");
        }

        Number leftNum = (Number) left;
        Number rightNum = (Number) right;

        if(left instanceof Double || right instanceof Double) {

            if(ctx.op.getType() == SimpleParser.GT) return leftNum.doubleValue() > rightNum.doubleValue();
            else if(ctx.op.getType() == SimpleParser.LT) return leftNum.doubleValue() < rightNum.doubleValue();
            else if(ctx.op.getType() == SimpleParser.GEQ) return leftNum.doubleValue() >= rightNum.doubleValue();
            else if(ctx.op.getType() == SimpleParser.LEQ) return leftNum.doubleValue() <= rightNum.doubleValue();
            else if(ctx.op.getType() == SimpleParser.EQ) return leftNum.doubleValue() == rightNum.doubleValue();
            else if(ctx.op.getType() == SimpleParser.NEQ) return leftNum.doubleValue() != rightNum.doubleValue();

        }

        if(ctx.op.getType() == SimpleParser.GT) return leftNum.intValue() > rightNum.intValue();
        else if(ctx.op.getType() == SimpleParser.LT) return leftNum.intValue() < rightNum.intValue();
        else if(ctx.op.getType() == SimpleParser.GEQ) return leftNum.intValue() >= rightNum.intValue();
        else if(ctx.op.getType() == SimpleParser.LEQ) return leftNum.intValue() <= rightNum.intValue();
        else if(ctx.op.getType() == SimpleParser.EQ) return leftNum.intValue() == rightNum.intValue();

        return leftNum.intValue() != rightNum.intValue();


    }

    @Override
    public Object visitAnd(SimpleParser.AndContext ctx) {

        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        if(  !( left.toString().equals("verdadero") || left.toString().equals("falso") )
                ||  !( right.toString().equals("verdadero") || right.toString().equals("falso") )
        )
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: No coinciden los tipos, los operandos deben ser lógicos.");

        if (left.toString().equals("verdadero") && right.toString().equals("verdadero")) {
            return "verdadero";
        }
        return "falso";

    }

    @Override
    public Object visitOr(SimpleParser.OrContext ctx) {

        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        if(  !( left.toString().equals("verdadero") || left.toString().equals("falso") )
            ||  !( right.toString().equals("verdadero") || right.toString().equals("falso") )
        )
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: No coinciden los tipos, los operandos deben ser lógicos.");

        if(left.toString().equals("verdadero") || right.toString().equals("verdadero")) {
            return "verdadero";
        }

        return "falso";

    }

    @Override
    public Object visitParens(SimpleParser.ParensContext ctx) {
        //Se resuelve lo de adentro del parentesis, se visita a la expresion hasta que quede en un terminal numero
        return visit(ctx.expression());
    }

}
