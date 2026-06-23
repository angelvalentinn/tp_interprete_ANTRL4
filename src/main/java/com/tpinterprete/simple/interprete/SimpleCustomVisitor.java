package com.tpinterprete.simple.interprete;

import java.util.HashMap;
import java.util.Map;

//Clase donde se aplica la gramatica de atributos a cada nodo del arbol, usando la tabla de simbolos
public class SimpleCustomVisitor extends SimpleBaseVisitor<Object> {

    private HashMap<String, Object> symbolTable = new HashMap<String, Object>();
    private Map<String, String> typeTable = new HashMap<>();

    @Override
    public Object visitVar_decl(SimpleParser.Var_declContext ctx) {

        if(symbolTable.containsKey(ctx.ID().getText()))
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Redeclaración de variable, estas intentando definir una variable ya definida.");

        String variableName = ctx.ID().getText();

        this.symbolTable.put(variableName, 0);
        this.typeTable.put(variableName, "entero");
        return null;
    }

    @Override
    public Object visitVar_assign(SimpleParser.Var_assignContext ctx) {

        if(!symbolTable.containsKey(ctx.ID().getText()))
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Declaración de variable, estas intentando asignar un valor a una variable no definida.");

        String id = ctx.ID().getText();
        Object value = visit(ctx.expression());

        String currentType = typeTable.get(id);
        String valueType = getType(value);

        if (currentType != null && !currentType.equals(valueType)){
            throw new RuntimeException("[Linea: "+ ctx.getStart().getLine()+"] " + "ERROR: Incompatibilidad de tipos.");
        }

        if(currentType==null){
            typeTable.put(id, valueType);
        }

        symbolTable.put(id, value);

        return value;

    }

    @Override
    public Object visitVar_assign_no_semicolon(SimpleParser.Var_assign_no_semicolonContext ctx) {
        if(!symbolTable.containsKey(ctx.ID().getText()))
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Declaración de variable, estas intentando asignar un valor a una variable no definida.");

        String id = ctx.ID().getText();
        Object value = visit(ctx.expression());

        String currentType = typeTable.get(id);
        String valueType = getType(value);

        if (currentType != null && !currentType.equals(valueType)){
            throw new RuntimeException("[Linea: "+ ctx.getStart().getLine()+"] " + "ERROR: Incompatibilidad de tipos.");
        }

        if(currentType==null){
            typeTable.put(id, valueType);
        }

        symbolTable.put(id, value);

        return value;
    }

    @Override
    public Object visitPrint(SimpleParser.PrintContext ctx) {

        Object res = visit(ctx.expression());

        if(res == null) throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                "ERROR: Variable sin declarar.");

        if(res instanceof Boolean) {
            boolean valor = (Boolean) res;
            System.out.println(valor ? "verdadero" : "falso");

        } else if(res instanceof Integer || res instanceof Double || res instanceof Float) {
            System.out.println(res);
        } else {

            String resWithoutquotation = res.toString().substring(1, res.toString().length() - 1);

            if(resWithoutquotation.equals("\\n")) {
                System.out.println();
            } else {
                System.out.println(resWithoutquotation);
            }
        }

        return null;
    }

    @Override
    public Object visitConditional(SimpleParser.ConditionalContext ctx) {

        Object conditionObj = visit(ctx.expression());
        Boolean condition;

        if (conditionObj instanceof Boolean) {
            condition = (Boolean) conditionObj;
        } else if (conditionObj.toString().equals("verdadero")) {
            condition = true;
        } else if (conditionObj.toString().equals("falso")) {
            condition = false;
        } else {
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Operación invalida, el condicional debe resultar en verdadero o falso.");
        }

        Integer tam = ctx.if_block().sentence().size();

        if(condition) {

            for(int i=0; i<tam; i++) {
                visit( ctx.if_block().sentence(i) );
            }

        } else {

            for(int i=0; i<tam; i++) {
                visit( ctx.else_block().sentence(i) );
            }

        }

        return null;

    }

    @Override
    public Object visitFor_stmt(SimpleParser.For_stmtContext ctx) {

        visit(ctx.for_init());

        SimpleParser.For_conditionContext conditionCtx = ctx.for_condition();
        SimpleParser.For_updateContext updateCtx = ctx.for_update();
        SimpleParser.For_blockContext blockCtx = ctx.for_block();

        while (true) {
            Object conditionObj = visit(conditionCtx.expression());
            Boolean condition;

            if (conditionObj instanceof Boolean) {
                condition = (Boolean) conditionObj;
            } else if (conditionObj.toString().equals("verdadero")) {
                condition = true;
            } else if (conditionObj.toString().equals("falso")) {
                condition = false;
            } else {
                throw new RuntimeException("[Linea: " + ctx.getStart().getLine() + "] " +
                        "ERROR: La condición del for debe resultar en verdadero o falso.");
            }

            if (!condition) {
                break;
            }

            for (int i = 0; i < blockCtx.sentence().size(); i++) {
                visit(blockCtx.sentence(i));
            }

            visit(updateCtx);
        }

        return null;
    }

    @Override
    public Object visitNot(SimpleParser.NotContext ctx) {
        Object value = visit(ctx.expression());

        if (value instanceof Boolean) {
            return !(Boolean) value;
        } else if (value.toString().equals("verdadero")) {
            return "falso";
        } else if (value.toString().equals("falso")) {
            return "verdadero";
        } else {
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine() + "] " +
                    "ERROR: El operador NOT solo puede aplicarse a valores booleanos.");
        }
    }


    @Override
    public Object visitId(SimpleParser.IdContext ctx) {

        String id = ctx.ID().getText();
        if(symbolTable.containsKey(id)) return symbolTable.get(id);
        return null;

    }

    @Override
    public Object visitNumb(SimpleParser.NumbContext ctx) {

        if(ctx.number().getText().contains(".")) {
            return Double.parseDouble(ctx.number().getText());
        }
        return Integer.parseInt(ctx.number().getText());
    }

    @Override
    public Object visitStr(SimpleParser.StrContext ctx) {
        return ctx.STRING();
    }

    @Override
    public Object visitBool(SimpleParser.BoolContext ctx) {
        return ctx.BOOLEAN();
    }


    @Override
    public Object visitMulDiv(SimpleParser.MulDivContext ctx) {

        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        if( !(left instanceof Integer || left instanceof Double)
                ||!(right instanceof Integer || right instanceof Double)
        )
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Operación invalida, los operandos deben ser numericos.");

        Number leftNumber = (Number) left;
        Number rightNumber = (Number) right;

        if(rightNumber.doubleValue() == 0.0) throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                "ERROR: Operación invalida, no se puede dividir por 0 o nulo.");


        if(ctx.op.getType() == SimpleParser.DIV) {

            return leftNumber.doubleValue() / rightNumber.doubleValue();

        } else if(leftNumber instanceof Double || rightNumber instanceof Double) {

            return leftNumber.doubleValue() * rightNumber.doubleValue();

        }

        return leftNumber.intValue() * rightNumber.intValue();

    }

    @Override
    public Object visitAddSub(SimpleParser.AddSubContext ctx) {

        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        if( !(left instanceof Integer || left instanceof Double)
                ||!(right instanceof Integer || right instanceof Double)
        )
            throw new RuntimeException("[Linea: " + ctx.getStart().getLine()  + "] " +
                    "ERROR: Operación invalida, los operandos deben ser numericos.");

        Number leftNumber = (Number) left;
        Number rightNumber = (Number) right;

        if(leftNumber instanceof Double || right instanceof Double) {

            if(ctx.op.getType() == SimpleParser.PLUS) {
                return leftNumber.doubleValue() + rightNumber.doubleValue();
            } else {
                return leftNumber.doubleValue() - rightNumber.doubleValue();
            }

        }

        if(ctx.op.getType() == SimpleParser.PLUS) return leftNumber.intValue() + rightNumber.intValue();

        return leftNumber.intValue() - rightNumber.intValue();

    }

    @Override
    public Object visitComp(SimpleParser.CompContext ctx)   {

        Object left = visit(ctx.expression(0));
        Object right = visit(ctx.expression(1));

        if (left instanceof Boolean && right instanceof Boolean) {
            boolean bLeft = (Boolean) left;
            boolean bRight = (Boolean) right;

            if(ctx.op.getType() == SimpleParser.EQ) return bLeft == bRight;
            if(ctx.op.getType() == SimpleParser.NEQ) return bLeft != bRight;
        }

        if (left instanceof String && right instanceof String) {
            String sLeft = (String) left;
            String sRight = (String) right;

            if(ctx.op.getType() == SimpleParser.EQ) return sLeft.equals(sRight);
            if(ctx.op.getType() == SimpleParser.NEQ) return !sLeft.equals(sRight);
        }

        if( !(left instanceof Double || left instanceof Integer)
                || !(right instanceof Double || right instanceof Integer)
        ) {
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
        return visit(ctx.expression());
    }

    private String getType(Object obj) {
        if (obj instanceof Integer) return "entero";
        if (obj instanceof Double) return "real";
        if (obj instanceof Boolean) return "booleano";
        if (obj instanceof String) return "string";
        return "desconocido";
    }

}